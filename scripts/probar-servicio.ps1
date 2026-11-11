param(
    [switch]$SoloUnitarias,
    [string]$MysqlJar = "$env:USERPROFILE/.m2/repository/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar"
)

# Compila con las dependencias existentes; no instala librerias ni cambia el POM.
$ErrorActionPreference = 'Stop'
$raizProyecto = Split-Path -Parent $PSScriptRoot
foreach ($jar in @($MysqlJar)) {
    if (!(Test-Path -LiteralPath $jar)) { throw 'Indique las rutas de los JAR del POM mediante -MysqlJar.' }
}
$salida = Join-Path $raizProyecto ('target/revision-servicio/' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force $salida | Out-Null
$fuentes = @(Get-ChildItem (Join-Path $raizProyecto 'src/main/java') -Recurse -Filter '*.java' | ForEach-Object FullName)
$pruebas = @('servicio', 'soporte') | ForEach-Object {
    Get-ChildItem (Join-Path $raizProyecto "src/test/java/$_/*.java") | ForEach-Object FullName
}
$cp = "$MysqlJar"
& javac -J-Xmx128m -J-XX:ReservedCodeCacheSize=32m --release 17 -encoding UTF-8 -cp $cp -d $salida @fuentes @pruebas
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

# El mismo main permite comprobar formulas sin MySQL o simular la jornada completa.
Push-Location $raizProyecto
try {
    if ($SoloUnitarias) { & java -Xmx128m -XX:ReservedCodeCacheSize=32m -cp "$salida;$cp" servicio.PruebaServicio --sin-mysql }
    else { & java -Xmx128m -XX:ReservedCodeCacheSize=32m -cp "$salida;$cp" servicio.PruebaServicio }
    $resultado = $LASTEXITCODE
} finally { Pop-Location }
exit $resultado
