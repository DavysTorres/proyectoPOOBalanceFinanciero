param(
    [string]$MysqlJar = "$env:USERPROFILE/.m2/repository/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar"
)

# Usa el conector JDBC declarado en el POM; no descarga ni instala librerias.
$ErrorActionPreference = 'Stop'
$raizProyecto = Split-Path -Parent $PSScriptRoot
foreach ($archivoJar in @($MysqlJar)) {
    if (!(Test-Path -LiteralPath $archivoJar)) {
        throw "No se encontro una dependencia del POM. Usa -MysqlJar para indicar sus rutas."
    }
}

# Compila modelo, excepciones, conexion y DAO en un directorio nuevo.
$salidaPruebas = Join-Path $raizProyecto ('target/revision-dao/' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force $salidaPruebas | Out-Null
$fuentes = @(Get-ChildItem (Join-Path $raizProyecto 'src/main/java') -Recurse -Filter '*.java' | ForEach-Object FullName)
$prueba = Join-Path $raizProyecto 'src/test/java/dao/PruebaDAO.java'
$soporte = Join-Path $raizProyecto 'src/test/java/soporte/BaseTemporal.java'
$classpath = "$MysqlJar"
& javac -J-Xmx128m -J-XX:ReservedCodeCacheSize=32m --release 17 -encoding UTF-8 -cp $classpath -d $salidaPruebas @fuentes $prueba $soporte
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

# La prueba usa .env sin mostrar credenciales y crea unicamente una base temporal.
Push-Location $raizProyecto
try {
    & java -Xmx128m -XX:ReservedCodeCacheSize=32m -cp "$salidaPruebas;$classpath" dao.PruebaDAO
    $resultadoPruebas = $LASTEXITCODE
} finally {
    Pop-Location
}
exit $resultadoPruebas
