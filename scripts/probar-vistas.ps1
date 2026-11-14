# Compila todas las fuentes y ejecuta solamente pruebas locales, sin MySQL.
$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot
$salida = Join-Path $raiz 'target/formularios-completos'
New-Item -ItemType Directory -Force $salida | Out-Null
$fuentes = @(Get-ChildItem (Join-Path $raiz 'src/main/java'),(Join-Path $raiz 'src/test/java') -Recurse -Filter '*.java' | ForEach-Object FullName)
& javac --release 17 -encoding UTF-8 -d $salida @fuentes
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Push-Location $raiz
try {
    foreach ($prueba in @('modelo.RevisionModelo','servicio.RevisionServicio','servicio.RevisionEquilibrio','controlador.RevisionControlador','vista.RevisionVentanas','vista.RevisionResponsiva','vista.RevisionFormulariosAdjuntos','vista.RevisionNavegacionIntegrada')) {
        $log = Join-Path $salida ($prueba + '.log')
        & java -Xmx128m -cp $salida $prueba *> $log
        if ($LASTEXITCODE -ne 0) {
            Get-Content -LiteralPath $log -Tail 20
            throw "Fallo la verificacion de $prueba"
        }
        Get-Content -LiteralPath $log | Select-Object -Last 1
    }
} finally { Pop-Location }
