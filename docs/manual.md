# Manual de uso y decisiones del grupo

## Instalacion

Seguir README.md: JDK 17 o superior, MySQL, tablas del esquema, archivo .env y ejecucion de principal.Main. El esquema SQL debe prepararse antes del primer uso. La aplicacion no borra ni reconstruye tablas automaticamente.

## Primer acceso

Con usuarios vacios aparece el formulario de inicializacion. Completar datos del negocio y del administrador y guardar. Conservar la clave elegida; no hay una clave de fabrica. Si ya hay usuarios aparece el login.

Si existen datos antiguos con hashes ficticios, no se pueden autenticar como si esos textos fueran una clave. Restablecerlos mediante un administrador valido o utilizar una base de demostracion separada.

## Operacion diaria

1. El administrador configura el negocio, costos, usuarios y productos.
2. El cajero abre Registrar venta y busca por nombre o codigo. Selecciona una coincidencia e indica la cantidad; Agregar o Enter inicia la venta con el primer producto.
3. Repite la busqueda para agregar mas productos. Al agregar, el buscador se limpia y la cantidad vuelve a 1; no hay que cambiar de pestanas.
4. La unica tabla contiene los productos de la venta. Cada fila permite restar 1, sumar 1 o eliminar el producto. El total se recalcula. Puede indicar el efectivo recibido para ver el cambio; vacio significa pago exacto. Cobrar guarda venta, detalles y descuento de stock juntos.
5. El jefe registra compras agregando varias lineas y guarda; el inventario aumenta una sola vez.
6. Los gastos variables afectan el dia. Un gasto fijo adicional se prorratea y no debe repetir un costo del catalogo.
7. El jefe consulta balance, margen, historial y modifica una venta solo con motivo.
8. Al terminar consulta el efectivo esperado, introduce el contado y confirma el cierre.
9. Un dia cerrado rechaza ventas, compras, gastos y ajustes posteriores.

El semaforo se actualiza al guardar movimientos. El boton Actualizar semaforo permite consultarlo otra vez. Cerrar una ventana de venta cancela su carrito sin mover stock.

## Interpretacion

- Rojo: todavia no se cubren costos o no hay margen positivo para calcular una meta.
- Amarillo: se alcanzo el porcentaje configurado, 70 por ciento inicialmente.
- Verde: se alcanzo el equilibrio.
- Meta estimada: sin ventas se usa el margen promedio del catalogo activo; no es un resultado garantizado.
- No calculable: no hay un margen positivo disponible y no se inventa un equilibrio de cero.
- Resultado negativo: perdida contable.
- Diferencia de caja negativa: se conto menos que lo esperado.
- Saldo de caja esperado negativo: los pagos registrados superaron los cobros sin un fondo inicial.

## Decisiones que deben aparecer en el informe academico

El cajero ve semaforo y porcentaje, pero no ingresos ni costos. Se usa un solo usuario por sesion de escritorio y una sola cola de operaciones. Los dias abiertos usan configuracion actual; los cerrados conservan su fotografia financiera.

Por ahora se supone que todas las ventas, compras y gastos registrados se cobran o pagan en efectivo y no hay fondo inicial. Los costos mensuales configurados no se restan otra vez del efectivo. Esta convencion debe confirmarse con el negocio que el grupo valide.

Los datos de ejemplo son publicos y solo deben cargarse en una base de demostracion vacia. El sistema real no debe usar esas claves.

## Exportacion y cierre del programa

En Balance o Reportes se puede exportar CSV. El archivo no es PDF ni XLSX. CSV se abre con una hoja de calculo configurada para punto y coma y coma decimal.

Al cerrar el programa se espera a que termine la operacion pendiente y se cierra la conexion. Si una ventana informa que esta ocupada, esperar su respuesta antes de cerrarla.

## Trabajo academico externo pendiente

El grupo debe realizar una prueba manual de su instalacion, validar el uso con un negocio real, registrar observaciones y escribir viabilidad y trabajo futuro. No deben presentarse entrevistas ni validaciones que no hayan ocurrido.

La entrega original pide JUnit y exportacion PDF/Excel. Esos puntos permanecen pendientes mientras se conserve la decision de pruebas por consola y CSV sin dependencias adicionales.
