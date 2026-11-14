# Ejecuta las pruebas del modelo sin Maven, MySQL ni dependencias externas.
$ErrorActionPreference = 'Stop'
$raizProyecto = Split-Path -Parent $PSScriptRoot

# Se usa una carpeta nueva para evitar colisiones con archivos de otras ejecuciones.
$salidaPruebas = Join-Path $raizProyecto ('target/revision-modelo/' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force $salidaPruebas | Out-Null

# Compila exclusivamente el modelo, su excepcion de stock y las pruebas de consola.
$fuentesModelo = @(Get-ChildItem (Join-Path $raizProyecto 'src/main/java/modelo/*.java') |
    ForEach-Object FullName)
$excepcionStock = Join-Path $raizProyecto 'src/main/java/excepciones/StockInsuficienteException.java'
$pruebasModelo = Join-Path $raizProyecto 'src/test/java/modelo/RevisionModelo.java'
& javac -J-Xmx128m -J-XX:ReservedCodeCacheSize=32m --release 17 -encoding UTF-8 -Xlint:all -d $salidaPruebas @fuentesModelo $excepcionStock $pruebasModelo
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

# Cada caso informa su resultado y cualquier fallo produce un codigo de salida distinto de cero.
& java -Xmx128m -XX:ReservedCodeCacheSize=32m -cp $salidaPruebas modelo.RevisionModelo
exit $LASTEXITCODE
