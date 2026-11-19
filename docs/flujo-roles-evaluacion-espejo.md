# Flujo de operación y evaluación espejo por rol

Revisión del código y pruebas locales: 11 de noviembre de 2026.

La evaluación espejo compara una misma acción desde los tres perfiles y contrasta lo que se muestra en la vista con lo que permite el controlador y autoriza el servicio. No modifica permisos ni reglas del proyecto.

## Entrada y salida comunes

1. `AplicacionDesktop` consulta si hace falta la configuración inicial.
2. Si no hay usuarios, presenta `FrmInicializacion`: nombre del negocio, días de operación, horario, umbrales, nombre del administrador, usuario y clave. El servicio crea negocio y administrador juntos. No hay autorregistro general de cuentas.
3. Si ya existen usuarios, presenta `FrmLogin`. Credenciales válidas de una cuenta activa abren `FrmPrincipal`; errores de acceso mantienen al usuario en el login. Registrarse informa que debe solicitarse una cuenta al administrador.
4. `FrmPrincipal` presenta nombre, rol, operaciones autorizadas y `PanelSemaforo`. Todos pueden actualizar el semáforo y cerrar sesión.
5. Los módulos operativos se abren como ventanas independientes; `FrmAdministracion` es una ventana interna del escritorio principal. Se mantiene una ventana por destino.
6. Volver cierra el módulo; no equivale a cerrar sesión. Cerrar la ventana de venta cancela el carrito pendiente. Cerrar sesión descarta el carrito de ventas y cierra las ventanas de esa sesión.

## Matriz de permisos y vistas

| Vista / acción | Cajero | Jefe | Administrador |
|---|---|---|---|
| Principal y semáforo | Sí | Sí | Sí |
| Venta: catálogo, carrito y cobro | Sí | Sí | Sí |
| Compras y gastos | No | Sí | Sí |
| Balance diario con importes | No | Sí | Sí |
| Margen por producto y consejos | No | Sí | Sí |
| Modificar total de una venta registrada | No | Sí | Sí |
| Historial de ajustes | No | Sí | Sí |
| Reporte diario y exportación CSV | No | Sí | Sí |
| Efectivo esperado y cierre de caja | No | Sí | Sí |
| Panel interno de administración | No | No | Sí |
| Crear, editar y desactivar usuarios; restablecer claves | No | No | Sí |
| Crear, editar y desactivar productos | No | No | Sí |
| Configurar catálogo de costos fijos mensuales | No | No | Sí |
| Cambiar configuración del negocio | No | No | Sí |

El cajero sí ve precios de venta, stock y el total de su carrito. Lo restringido son las cifras financieras del balance, los márgenes, los egresos y el efectivo esperado de caja.

## Recorrido del cajero

1. Inicia sesión y consulta el mensaje y porcentaje del semáforo.
2. Abre Ventas y pulsa Nueva venta.
3. Busca productos, selecciona una fila del catálogo, indica cantidad y pulsa Agregar.
4. Revisa Carrito. Para Quitar o Reducir 1 selecciona allí la línea correspondiente.
5. Pulsa Cobrar. El servicio verifica carrito, propietario, jornada, disponibilidad y precios antes de guardar y descontar existencias.
6. Si cancela antes del cobro, no se registra ingreso ni se descuenta inventario.
7. Puede iniciar otra venta o cerrar sesión.

No puede cambiar manualmente el precio del catálogo ni el total de una venta cobrada. Tampoco puede abrir balance, compras, márgenes, historial, reportes, cierre ni administración. Que un botón esté oculto no es la única protección: las acciones restringidas vuelven a comprobar permisos al navegar y al operar.

## Recorrido del jefe

Puede realizar el recorrido de ventas del cajero y, además:

- **Compras:** introduce proveedor; busca y selecciona productos existentes; define cantidad y costo unitario; agrega líneas y guarda la compra completa. El inventario se actualiza al guardar, no al preparar las líneas.
- **Gastos:** introduce concepto y monto, elige fijo o variable y, si es fijo, indica días de operación. El servicio asigna la fecha actual. Un gasto fijo adicional no debe duplicar un concepto activo del catálogo de costos fijos.
- **Registros del día:** actualiza las listas de compras y gastos de la jornada.
- **Balance:** consulta una fecha y revisa ingresos, costos variables, margen de contribución, costos fijos diarios, resultado, punto de equilibrio y faltante. Las metas estimadas o no calculables se presentan como tales.
- **Márgenes:** revisa los extremos del ranking del catálogo, selecciona un producto y solicita el consejo. Esta pantalla no cambia precios.
- **Modificar venta:** busca su número, revisa los datos, introduce un nuevo total y el motivo, y guarda el ajuste. Cambiar el número invalida la consulta anterior. El servicio actualiza total e historial en una transacción; no altera cantidades ni inventario.
- **Historial:** consulta los ajustes de ventas y filtra por fecha. No dispone de edición ni borrado de registros.
- **Reportes:** consulta un balance diario y exporta CSV para una hoja de cálculo.
- **Cierre:** consulta el efectivo esperado, cuenta el efectivo físico, introduce el conteo y pulsa Cerrar jornada. Puede cerrar hoy o un día anterior abierto; no una fecha futura ni una jornada ya cerrada.

No puede crear usuarios, cambiar roles, restablecer claves, crear productos, editar precios del catálogo, configurar costos fijos mensuales ni cambiar el negocio. Registrar una compra con costo unitario y registrar un gasto fijo adicional son operaciones diferentes de administrar esos catálogos.

## Recorrido del administrador

Además de todas las operaciones del jefe, abre Administración y utiliza:

1. **Usuarios:** crea cuentas con nombre, usuario, clave y rol; selecciona filas para cambiar datos, rol y estado; desactiva cuentas o restablece claves. La creación genera cuentas activas; el estado se modifica al editar. Las claves existentes y sus hashes no se muestran en la tabla.
2. **Productos:** crea nombre, precio, costo, stock inicial y mínimo; selecciona un producto para editar su catálogo o desactivarlo. El stock existente es de consulta: los movimientos corresponden a compras y ventas.
3. **Costos fijos:** crea, edita o desactiva conceptos con monto mensual. El balance los distribuye según los días de operación.
4. **Configuración:** cambia negocio, días de operación, apertura, cierre y umbrales.
5. **Historial y reportes:** accede a las mismas consultas disponibles en el menú operativo.

Ser administrador no permite omitir las validaciones: no puede desactivar o degradar al último administrador activo, guardar movimientos de una jornada cerrada, cerrar fechas futuras, ajustar ventas sin motivo ni editar el historial desde estas vistas. No existe una acción de reapertura de jornada.

## Evaluación espejo de acciones

En esta tabla, «coincide» significa correspondencia verificada por lectura de vista, controlador y servicio. Las pruebas indicadas al final aportan evidencia local adicional; no implican que se haya ejecutado cada fila contra MySQL.

| Acción a reproducir | Cajero | Jefe | Administrador | Correspondencia actual |
|---|---|---|---|---|
| Abrir Ventas y preparar un carrito válido | Permitido | Permitido | Permitido | Coincide |
| Cobrar una venta vacía o sin stock | Rechazado | Rechazado | Rechazado | Coincide: validación en servicio |
| Consultar importes del balance | Acceso rechazado | Permitido | Permitido | Coincide: menú y servicio |
| Registrar una compra con productos existentes | Acceso rechazado | Permitido | Permitido | Coincide |
| Crear un producto nuevo | Acceso rechazado | Acceso rechazado | Permitido | Coincide |
| Registrar gasto fijo adicional | Acceso rechazado | Permitido | Permitido | Coincide; no equivale a editar el catálogo mensual |
| Cambiar precio de venta del catálogo | Acceso rechazado | Acceso rechazado | Permitido | Coincide |
| Ajustar total de venta abierta con motivo | Acceso rechazado | Permitido | Permitido | Coincide |
| Ajustar venta de una jornada cerrada | Rechazado | Rechazado | Rechazado | Coincide: validación del servicio |
| Consultar historial de ajustes | Acceso rechazado | Permitido | Permitido | Coincide |
| Editar o borrar un registro del historial | Sin acción en la vista | Sin acción en la vista | Sin acción en la vista | Coincide con una consulta de solo lectura |
| Exportar balance diario | Acceso rechazado | CSV | CSV | Disponible; PDF y XLSX no están implementados |
| Cerrar jornada abierta | Acceso rechazado | Permitido | Permitido | Coincide; requiere conteo válido |
| Reabrir jornada cerrada | Sin acción | Sin acción | Sin acción | No implementado |
| Cambiar roles y restablecer claves | Acceso rechazado | Acceso rechazado | Permitido | Coincide |
| Desactivar al último administrador activo | Acceso rechazado | Acceso rechazado | Rechazado | Coincide: protección en servicio |

## Observaciones para evaluar el uso real

1. `EstiloZeroLoss` define apariencia, tablas no editables y asociación de etiquetas con campos. Los permisos se definen en los modelos de usuario; la vista decide visibilidad; el controlador y el servicio comprueban autorización.
2. `Jefe.mostrarMenu()` no incluye los textos de Modificar venta y Reportes, aunque sus permisos y el menú Swing sí los habilitan. `Administrador.mostrarMenu()` enumera administración y no todas las operaciones permitidas. El menú real utiliza permisos, no esas listas. Conviene unificar esas representaciones.
3. Historial registra ajustes del total de ventas; no es una auditoría general de creación de usuarios, cambios de productos, compras y configuración.
4. Reportes reutiliza el balance diario. La vista no ofrece un rango de fechas ni PDF/XLSX, aunque existe un método de servicio para consultar balances por rango.
5. Cerrar jornada ejecuta el cierre directamente al pulsar el botón; no hay un segundo diálogo de confirmación. La consulta previa del efectivo es parte del recorrido recomendado, no una condición que el controlador exija.
6. Las listas administrativas se actualizan mediante Actualizar lista; guardar no recarga automáticamente todas las tablas. Balance y márgenes abiertos también requieren una nueva consulta para reflejar otros cambios. El semáforo principal se solicita de nuevo tras operaciones exitosas.
7. La caja supone que ventas, compras y gastos se pagan en efectivo y no contempla fondo inicial. El efectivo esperado es ingresos menos compras menos gastos completos. El resultado del balance descuenta costos de ventas y costos fijos diarios: ambos valores pueden ser diferentes sin representar un error.
8. Las pruebas locales no sustituyen una evaluación completa de persistencia, stock e historial contra una base MySQL de demostración.

## Evidencia ejecutada en esta revisión

- `controlador.RevisionControlador`: 28 casos aprobados, 0 fallidos. Incluye navegación del cajero y administrador, denegación real de operaciones restringidas, entrada del ajuste de venta, exportación y restauración de controles.
- `vista.RevisionFormulariosAdjuntos`: integración aprobada de compras, gastos, ranking, consejos, navegación, administración, cambio de visibilidad por perfil y semáforo con servicios de prueba.
- Lectura de los tres modelos de rol, `FrmPrincipal`, `PrincipalControlador`, `AplicacionDesktop`, `EstiloZeroLoss` y servicios de ventas, compras, usuarios, balance, cierre, historial, productos, costos, configuración y exportación.

No se iniciaron sesiones contra MySQL ni se modificaron datos del negocio durante esta evaluación.
