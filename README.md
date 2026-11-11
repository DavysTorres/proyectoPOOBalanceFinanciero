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

- Administrador: seis botones de administracion y un menu Operaciones para las demas acciones permitidas.
- Jefe: venta, compras y gastos, balance, margenes, historial, ajustes y cierre.
- Cajero: registrar ventas y consultar semaforo y progreso, sin recibir cifras monetarias del balance.
- Venta: cargar productos, pulsar Nueva venta, seleccionar producto y cantidad, agregar al carrito y cobrar. Las pestanas separan catalogo y carrito.
- Compras: buscar productos, seleccionar proveedor, cantidad y costo; agregar lineas y guardar. El mismo formulario registra gastos.
- Balance: escribir una fecha AAAA-MM-DD, consultar las cifras y grafica, o exportar CSV.
- Cierre: consultar el efectivo esperado, introducir el conteo y confirmar el cierre. Una jornada cerrada rechaza nuevos movimientos y ajustes.
- Usuarios y catalogos: seleccionar una fila para editar o desactivar. Las tablas de usuarios nunca muestran hashes.
- El semaforo se actualiza despues de guardar operaciones y tambien mediante Actualizar semaforo.

Las claves nuevas tienen al menos ocho caracteres. Las horas se ingresan como 08:00 y los importes sin separadores de miles, usando coma o punto decimal.

## Datos de demostracion

sql/datos_ejemplo.sql contiene tres perfiles, quince productos, tres costos fijos, compras, gastos y ventas de dos dias. Usalo solamente en un esquema de demostracion vacio que ya tenga las tablas.

Usuarios de demostracion: demo_admin, demo_jefe y demo_cajero. Su clave publica de ejemplo es DemoPoo2026!. Esos usuarios no se crean automaticamente en la base real. No uses claves de demostracion para una instalacion real.

## Verificacion

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/probar-general.ps1
```

El script compila todo con --release 17, limita memoria y ejecuta las pruebas por partes. Las pruebas MySQL crean esquemas aleatorios y los eliminan al terminar. Requieren que el usuario de pruebas tenga permiso para crear y eliminar esas bases temporales. Las pruebas de ventanas no se muestran en pantalla y guardan imagenes dentro de target/revision-ventanas.

Tambien existen probar-modelo.ps1, probar-dao.ps1, probar-servicio.ps1 y probar-controlador.ps1. Los scripts con MySQL aceptan -MysqlJar si el conector esta en una ruta diferente del repositorio Maven local.

## Documentacion

- revision-general.md: comparacion actual con la guia y requisitos pendientes.
- docs/diseno.md: diagramas de clases y base de datos.
- docs/manual.md: uso y decisiones del proyecto.
- controlador.md, servicio.md y dao.md: explicaciones por capa.
- revision-modelo.md: registro historico de la revision inicial del modelo.

La exportacion actual es CSV, no PDF ni XLSX. Las pruebas actuales son mains de Java, no JUnit. Esos puntos y la validacion con un negocio real deben resolverse para afirmar cumplimiento completo de la entrega original.
