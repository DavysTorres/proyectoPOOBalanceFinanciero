# Sistema de balance diario

Proyecto de Programacion Orientada a Objetos con Java 17, Swing y MySQL. El codigo se organiza en modelo, excepciones, conexion, dao, servicio, controlador, vista y util. La unica dependencia externa de la aplicacion es el conector JDBC de MySQL declarado en pom.xml.

## Preparar el proyecto

1. Instala un JDK 17 o superior y abre pom.xml en IntelliJ como proyecto Maven.
2. Selecciona un SDK disponible y recarga Maven. El codigo se compila con nivel Java 17.
3. En una base MySQL vacia, ejecuta sql/esquema.sql. Se requiere MySQL 8.0.16 o posterior para aplicar CHECK.
4. Copia .env.example a .env y completa DB_URL, DB_USER y DB_PASSWORD.
5. Usa la raiz del proyecto como directorio de trabajo y ejecuta principal.Main.

El archivo .env se lee con util.ConfiguracionEntorno, usando Java estandar. Admite comentarios al inicio de linea, valores entre comillas y caracteres como # o = dentro del valor. Las variables del sistema tienen prioridad. Nunca se muestra ni se debe subir .env a Git.

Los scripts no son migraciones: si ya tienes tablas, compara su estructura antes de instalar otra version del esquema. La revision no instalo tablas ni datos en tu base original.

## Primer uso

Si la tabla de usuarios esta vacia, se abre un formulario para crear el primer administrador y configurar el negocio. Ambos registros se guardan juntos: si algo falla, se revierte la operacion. No existe una clave de administrador predeterminada.

Si ya hay usuarios, se abre el login real. Las cuentas antiguas con hashes SHA-256 hexadecimales se migran automaticamente a PBKDF2 al iniciar sesion correctamente. Los usuarios nuevos usan hashes generados por ServicioAutenticacion; los textos ficticios como hash_prueba utilizados en algunos casos de prueba no son claves validas para el login.

## Uso desde las ventanas

Tras iniciar sesion, las opciones del menu se muestran dentro del panel central de la ventana principal. La barra lateral permanece disponible y Resumen del dia permite volver al dashboard. Cambiar de seccion conserva los formularios y el carrito pendientes; la flecha Volver cierra ese formulario y, en ventas, cancela el carrito. Cerrar sesion cierra todos los modulos de la sesion. El menu se bloquea mientras se procesa una operacion.

- Administrador: menu lateral de operaciones y panel interno de administracion con seis accesos.
- Jefe: venta, compras y gastos, balance, margenes, historial, ajustes y cierre.
- Cajero: registrar ventas y consultar semaforo y progreso, sin recibir cifras monetarias del balance.
- Venta: buscar por nombre o codigo, seleccionar el producto, indicar cantidad y agregar. El primer producto inicia la venta; la tabla solo muestra lo que se va a cobrar, con -1, +1 y Eliminar en cada fila. El total se actualiza al instante. Efectivo recibido es opcional: vacio significa pago exacto; al introducirlo se muestra el cambio.
- Compras: escribir el proveedor, buscar y seleccionar un producto del catalogo, indicar cantidad y costo; agregar lineas y guardar. Las pestanas separan compras, gastos y registros del dia. La fecha del gasto la asigna el servicio al guardar; los gastos fijos requieren dias de operacion.
- Margenes: consultar los productos con mayor y menor margen, seleccionar uno y pulsar Ver consejo. Los datos provienen del catalogo y del analizador del proyecto.
- Balance: escribir una fecha AAAA-MM-DD, consultar las siete cifras o exportar CSV.
- Cierre: consultar el efectivo esperado, introducir el conteo y confirmar el cierre. Una jornada cerrada rechaza nuevos movimientos y ajustes.
- Modificar venta (administrador y jefe): buscar por numero, revisar productos y total, introducir el nuevo total y explicar el motivo. Cambiar el numero invalida la consulta anterior. El servicio conserva el historial del ajuste.
- Configuracion inicial: crear el primer administrador y definir nombre del negocio, dias de operacion, horario y umbrales. Despues del primer uso, la configuracion del negocio pertenece al administrador.
- Usuarios y catalogos: seleccionar una fila para editar o desactivar. Las tablas de usuarios nunca muestran hashes.
- El semaforo se actualiza despues de guardar operaciones y tambien mediante Actualizar semaforo.

Las claves nuevas tienen al menos ocho caracteres. Las horas se ingresan como 08:00 y los importes sin separadores de miles, usando coma o punto decimal.

## Datos del negocio

La instalacion inicia sin ventas, productos, compras ni costos de demostracion. Configure su negocio y registre sus datos reales desde los formularios. Los escenarios ficticios quedan exclusivamente en las pruebas con bases temporales; no se cargan en la base de uso diario.

El resumen distingue resultado registrado y meta estimada. Consulte [las reglas de calculo](docs/calculo-rentabilidad.md). La periodicidad de reposicion no determina el costo diario: se utiliza el costo promedio del inventario vendido.

## Verificacion

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/probar-general.ps1
```

El script compila todo con --release 17, limita memoria y ejecuta las pruebas por partes. Las pruebas MySQL crean esquemas aleatorios y los eliminan al terminar. Requieren que el usuario de pruebas tenga permiso para crear y eliminar esas bases temporales. Las pruebas de ventanas no se muestran en pantalla y guardan imagenes dentro de target/revision-ventanas.

Tambien existen probar-modelo.ps1, probar-dao.ps1, probar-servicio.ps1 y probar-controlador.ps1. Los scripts con MySQL aceptan -MysqlJar si el conector esta en una ruta diferente del repositorio Maven local.

La revision aislada de los formularios adjuntos no requiere MySQL:

```powershell
javac -encoding UTF-8 --release 17 -d target/formularios-check -sourcepath "src/main/java;src/test/java" src/test/java/vista/RevisionFormulariosAdjuntos.java
java -cp target/formularios-check vista.RevisionFormulariosAdjuntos
```

Comprueba eventos de compra, ranking, consejos, navegacion, permisos y semaforo; guarda capturas en target/revision-formularios-adjuntos. Todas las vistas y contratos referenciados por AplicacionDesktop estan implementados.

Para compilar el proyecto completo y comprobar ventanas, controladores, modelo y servicios sin usar MySQL:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/probar-vistas.ps1
```

La revision de ventanas guarda capturas en target/revision-ventanas. Las pruebas aisladas sustituyen los servicios de datos cuando comprueban interacciones; no verifican la persistencia JDBC de una instalacion real.

## Documentacion

- revision-general.md: comparacion actual con la guia y requisitos pendientes.
- docs/diseno.md: diagramas de clases y base de datos.
- docs/manual.md: uso y decisiones del proyecto.
- controlador.md, servicio.md y dao.md: explicaciones por capa.
- revision-modelo.md: registro historico de la revision inicial del modelo.

La exportacion actual es CSV, no PDF ni XLSX. Las pruebas actuales son mains de Java, no JUnit. Esos puntos y la validacion con un negocio real deben resolverse para afirmar cumplimiento completo de la entrega original.


El acceso a ventas reconoce `cajero_id` (esquema de referencia) y `usuario_id` (instalaciones anteriores) como identificador del responsable. No requiere renombrar la columna ni eliminar ventas existentes. `dao.RevisionEsquemaVenta` permite comprobar la compatibilidad del esquema configurado con consultas de solo lectura y EXPLAIN; no registra ventas ni descuenta inventario.
