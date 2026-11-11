param(
    [string]$MysqlJar = "$env:USERPROFILE/.m2/repository/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar"
)
# Una compilacion, pruebas secuenciales y memoria limitada para un computador de clase.
$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot
if (!(Test-Path -LiteralPath $MysqlJar)) { throw 'Indique el conector JDBC mediante -MysqlJar.' }
$salida = Join-Path $raiz ('target/revision-completa/' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force $salida | Out-Null
$fuentes = @(Get-ChildItem (Join-Path $raiz 'src/main/java'),(Join-Path $raiz 'src/test/java') -Recurse -Filter '*.java' | ForEach-Object FullName)
& javac -J-Xmx128m -J-XX:ReservedCodeCacheSize=32m --release 17 -encoding UTF-8 -cp $MysqlJar -d $salida @fuentes
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Push-Location $raiz
try {
    foreach ($prueba in @('modelo.RevisionModelo','dao.PruebaDAO','servicio.PruebaServicio','servicio.PruebaInicializacion','controlador.PruebaControlador','vista.RevisionVentanas')) {
        $log = Join-Path $salida ($prueba + '.log')
        $sinVentanas = if ($prueba -eq 'vista.RevisionVentanas') { 'false' } else { 'true' }
        & java -Xmx128m -XX:ReservedCodeCacheSize=32m "-Djava.awt.headless=$sinVentanas" -cp "$salida;$MysqlJar" $prueba *> $log
        if ($LASTEXITCODE -ne 0) {
            Get-Content -LiteralPath $log -Tail 15
            throw "Fallo la verificacion de $prueba"
        }
        Get-Content -LiteralPath $log | Select-String '^RESULTADO'
    }
} finally { Pop-Location }
Write-Output 'Revision general finalizada. Las pruebas MySQL solo usaron esquemas temporales.'
