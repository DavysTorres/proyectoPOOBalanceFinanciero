Estado actualizado: ver revision-general.md. La conexion conserva un solo administrador y ya no depende de dotenv-java.

# Paquete DAO

Los DAO de la seccion 5 de la guia estan implementados con JDBC. No se agregaron dependencias: se reutilizan MySQL Connector/J y la configuracion local mediante ConfiguracionEntorno.

## Clases y responsabilidades

| Clase | Responsabilidad |
| --- | --- |
| CrudDAO<T> | Contrato generico de guardar, actualizar, buscar y listar entidades editables. |
| DAOBase<T> | Reutiliza consultas parametrizadas, claves generadas, cierre de recursos y transacciones. |
| conexion.conexionDB | Unico Singleton que abre y cierra la conexion y permite transacciones explicitas. No existe otra fachada en dao. |
| UsuarioDAO | CRUD, busqueda por username, activos, cambio de estado y hash; Factory para reconstruir Administrador, Jefe o Cajero. |
| ProductoDAO | Catalogo, busqueda parcial, activos, alertas y actualizacion de stock y costo. |
| VentaDAO | Guarda cabecera, detalles y descuento de stock en una transaccion; recupera precios historicos y totales ajustados. |
| CompraDAO | Guarda compra y detalles, aumenta stock y actualiza costo en una transaccion. |
| GastoDAO | Persiste gastos y reconstruye GastoFijo o GastoVariable segun su tipo. |
| CostoFijoDAO | Catalogo de costos mensuales y suma de los activos. |
| CierreDiarioDAO | Guarda cierres, busca por fecha y consulta rangos; la fecha es unica. |
| HistorialCambiosDAO | Inserta y consulta registros sin metodos de actualizar o borrar. |
| ConfiguracionDAO | Guarda claves y recupera Negocio; guarda la configuracion completa atomicamente. |
| AccesoDatosException | Envuelve errores JDBC conservando su causa para diagnostico. |

Los bloques de codigo incluyen comentarios explicativos. El diseno demuestra interfaces, genericos, herencia, encapsulacion, polimorfismo, composicion y manejo de excepciones. Trabajo<R> es una interfaz local que permite indicar que operaciones ejecutar dentro de una transaccion; sus lambdas son sintaxis de Java estandar, no una libreria adicional.

## Esquema y configuracion

`sql/esquema.sql` define once tablas MySQL con claves foraneas, username unico, fecha de cierre unica y CHECK de importes y cantidades. Usa InnoDB para transacciones y utf8mb4 para texto. Requiere MySQL 8.0.16 o posterior para aplicar las restricciones CHECK.

El script se preparo para ejecutarse manualmente en una base vacia seleccionada por el grupo. No se ha instalado en la base de la aplicacion. Si ya existe un esquema, primero se deben comparar tablas y columnas; no es una migracion ni elimina tablas para recrearlas.

Se conservan DB_URL, DB_USER y DB_PASSWORD como variables del entorno o del archivo .env. No se incluyen ni se imprimen credenciales. Los DAO tienen constructor sin argumentos y constructor con Connection, de modo que las pruebas y servicios pueden compartir una conexion. El constructor sin argumentos obtiene la conexion directamente de conexion.conexionDB en su primera consulta y la conserva. Si se cierra, se crea otro DAO; no se sustituye una conexion dentro de una operacion.

No hay datos de demostracion insertados en la base real. La prueba crea sus usuarios y productos exclusivamente en su esquema temporal.

## Reglas para integrar con servicios

- La vista llama al controlador; el servicio autoriza la operacion y usa los DAO. El modelo permanece independiente de JDBC.
- VentaDAO.guardarConDetalles ya descuenta stock en SQL. CompraDAO.guardarConDetalles ya lo aumenta y actualiza el costo. El servicio no debe repetir esos cambios, pues duplicaria los movimientos.
- Las entidades de productos ya cargadas en memoria no se actualizan con los movimientos SQL: se recargan con ProductoDAO despues de confirmar.
- ProductoDAO.actualizar modifica el catalogo y el estado, pero no sobrescribe stock desde una copia antigua. Para un ajuste manual de inventario existe actualizarStock.
- La condicion stock>=cantidad esta dentro del UPDATE de venta; no se depende solo de un stock leido previamente en Java.
- Cada guardarConDetalles confirma su propia transaccion si no hay otra activa. Si un servicio inicio una transaccion, el DAO usa un savepoint y deja el commit o rollback general al servicio.
- Tras revertir una transaccion general, se deben descartar o recargar las entidades a las que ya se asignaron ids: la memoria Java no se revierte automaticamente con SQL.
- Los ids de las cabeceras se asignan al finalizar la operacion. Para obtener los ids persistidos de los detalles se consulta de nuevo la compra o venta.
- Cambiar el total de una venta exige permiso, motivo, historial y dia abierto en el futuro servicio. VentaDAO.actualizarTotal y HistorialCambiosDAO.guardar deben agruparse en una sola transaccion.
- Se incorporo a Venta el atributo totalRegistrado y el metodo restaurarTotalRegistrado para recuperar un ajuste sin perderlo al sumar los detalles. getTotal devuelve el importe registrado; calcularTotal sigue ofreciendo la suma de las lineas. Modificar el carrito descarta el ajuste restaurado.
- CierreDiarioDAO impide duplicar la fecha mediante la base. Autorizar el cierre y rechazar operaciones sobre dias cerrados corresponde a ServicioCierre y los demas servicios.
- HistorialCambiosDAO.guardar devuelve el id generado; no modifica el RegistroCambio inmutable.
- Las listas vacias y las busquedas sin resultado no son errores: se devuelve lista vacia o null, respectivamente.
- GastoDAO.sumarPorTipoYFecha suma importes registrados. Los costos variables del balance solicitan VARIABLE; las compras no se suman de nuevo al costo de lo vendido.
- No hay SQL ni calculos financieros en las ventanas. El CRUD no implementa autenticacion ni cifrado; recibe hashes ya generados por el servicio.
- Los importes del esquema tienen cuatro decimales. Si la aplicacion admite mas precision, el grupo debe acordar otra escala antes de guardar esos datos.
- La aplicacion de escritorio usa una conexion compartida. Para tareas simultaneas, cada hilo debe recibir una conexion propia; no debe compartir una transaccion del Singleton con otro hilo.

## Pruebas

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/probar-dao.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/probar-modelo.ps1
```

El primer script compila los DAO, el modelo y sus dependencias existentes. Busca el JAR JDBC del POM en el repositorio Maven local. Si estan en otra carpeta, admite -MysqlJar y con sus rutas.

PruebaDAO conecta usando .env y crea una base con nombre aleatorio balance_dao_prueba_<identificador>. Necesita permiso para crear y eliminar ese esquema temporal. Carga alli el script, ejecuta las pruebas y elimina solamente ese esquema al finalizar. No crea tablas ni inserta filas en la base original. Si falta configuracion, MySQL no esta disponible o falla un caso, el proceso devuelve un codigo distinto de cero.

Resultado verificado tras revisar la conexion: 29 pruebas JDBC correctas en MySQL y 65 pruebas del modelo correctas. Se probaron todos los DAO, lectura y escritura, Factory, unicidad, consultas parametrizadas, valores historicos, rollback completo, transacciones anidadas con savepoints, costos y gastos, configuracion, historial, cierres, dias vacios y limites por fecha.

Se verifica con javac 26.0.1 y --release 17, igual que el POM. No se afirma una ejecucion Maven completa por consola. Las pruebas son mains de Java; no se agrego JUnit, aunque la guia lo solicita para la entrega academica final.

## Revision de redundancias de conexion

Se elimino dao.ConexionBD. Era una fachada que delegaba en conexion.conexionDB: no abria otra conexion fisica, pero introducia un segundo Singleton sin aportar una responsabilidad necesaria para este proyecto.

La ruta normal queda DAO concreto -> DAOBase -> conexion.conexionDB -> JDBC/MySQL. Los constructores con Connection se conservan para pruebas y transacciones compartidas; no abren otra conexion. La prueba JDBC abre una conexion de laboratorio independiente y la dirige a su esquema temporal, sin cambiar la arquitectura de la aplicacion.

conexionDB administra apertura, reutilizacion, inicio de transacciones explicitas, confirmacion, reversion y desconexion. DAOBase mantiene el bloque transaccional propio de las operaciones compuestas y los savepoints cuando participa en una transaccion del servicio. Son alcances distintos y no dos administradores de conexion.

Obtener el Singleton ya no conecta. desconectar no llama a getConnection ni abre una conexion para cerrarla. Si hay cambios pendientes, los revierte antes de cerrar. Los fallos de apertura se propagan como SQLException, y DAOBase los envuelve en AccesoDatosException. Se eliminaron la carga manual redundante del driver JDBC y las impresiones de errores de conexion. Confirmar o revertir sin una transaccion explicita es un error; tampoco abre una nueva conexion. Se conserva el nombre conexionDB para mantener la API existente.

Se agregaron seis pruebas reales: identidad de la conexion entre DAO normales, rollback y commit del administrador, cierre con cambios pendientes, rechazo de uso de un DAO cuya conexion se cerro y rechazo de confirmar sin transaccion. El esquema temporal se elimina al terminar.

No se eliminaron las validaciones de integridad SQL aunque tambien existan en el modelo: protegen datos escritos por otros clientes. Tampoco se unificaron historial/cierre con CrudDAO, porque ofrecer actualizar o eliminar esos registros violaria su contrato. Los mapeos especificos de cada entidad se conservan para que las diferencias del modelo sean explicitas y explicables en la sustentacion.

La infraestructura de base temporal se comparte ahora con las pruebas del paquete servicio. DAOBase.transaccion es publico para coordinar operaciones sin SQL en servicios. El esquema de referencia admite un saldo de caja esperado negativo; el conteo real sigue siendo no negativo. Las decisiones de negocio actuales se explican en servicio.md.
