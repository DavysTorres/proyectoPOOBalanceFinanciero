# Revision general frente a la guia

La revision cubrio todo src/main/java, las pruebas, el POM, scripts, esquema SQL y documentacion. Se volvio a leer la guia entregada por el usuario y se conservaron las decisiones acordadas durante el proyecto. La aplicacion ya tiene ventanas conectadas a sus controladores y las funciones principales se comprobaron. El cumplimiento de la entrega original sigue condicionado por los pendientes indicados al final.

## Cambios de esta revision

- Main ahora inicia AplicacionDesktop, que enlaza vistas y controladores reales.
- Se agregaron las ventanas de administracion, venta, compra/gasto, balance, reportes, margenes, historial, ajuste, cierre y configuracion, con FrmBase comun.
- El primer administrador y Negocio se configuran desde un formulario y se guardan en una sola transaccion.
- PanelSemaforo muestra estado, mensaje y progreso sin importes para el cajero. PanelGrafica compara ingresos y equilibrio usando Swing.
- Los cambios guardados notifican a la pantalla principal para refrescar su semaforo. Los clics duplicados siguen bloqueados.
- El paquete util contiene validacion, formato monetario, fechas, mensajes y lectura de .env.
- Se retiro dotenv-java; solo permanece el conector JDBC MySQL del POM.
- Se retiro el JAR manual duplicado de lib y el formulario vacio de IntelliJ que no construia el login.
- El nivel de compilacion y los scripts utilizan Java 17; no se necesita Java 27.
- Se aplico formato de IntelliJ al codigo para separar bloques y mejorar lectura.
- Se corrigio el cierre de sesion para descartar el carrito, incluso si el usuario fue desactivado.
- Se agregaron datos SQL de demostracion con tres perfiles, quince productos, costos y ventas de dos dias.

No se instalaron tablas, usuarios ni datos de ejemplo en la base original. Los ensayos MySQL crearon y eliminaron sus propios esquemas temporales.

## Comparacion por seccion

| Seccion de la guia | Resultado actual |
| --- | --- |
| Convenciones y MVC | Capas separadas, atributos del modelo privados, dinero con BigDecimal y SQL de negocio en DAO. Se conserva el nombre conexionDB por compatibilidad. |
| Modelo | Entidades y validaciones implementadas. Rol y Permiso son enums compartidos, sin duplicados internos. |
| Excepciones | Siete excepciones propias. StockInsuficienteException es comprobada; las demas son RuntimeException por decision documentada. |
| DAO y conexion | Once tablas de referencia, CRUD, consultas, claves generadas, transacciones y una sola conexion administrada. |
| Servicios | Autenticacion, permisos, catalogos, ventas, compras, gastos, balance, margen, historial y cierre. |
| Controladores | Trece controladores de la guia y uno adicional para primer uso. Reciben contratos de vista y servicios. |
| Vistas | Formularios Swing concretos y navegacion desde Main. La ventana inicial ya no es una demostracion con credenciales sin comprobar. |
| Utilidades | Validador, FormatoMoneda, Mensajes, FechaUtil y ConfiguracionEntorno. |
| Permisos | Cajero registra ventas y ve semaforo/progreso sin importes. Jefe y administrador conservan sus acciones autorizadas. |
| Reglas financieras | Ejemplo de 850000 y equilibrio 975000 verificado. Costos historicos y cierres conservan sus datos. |
| Datos de ejemplo | Script independiente probado en una base temporal; no se carga automaticamente. |
| Pulido | Alertas en tablas de productos, semaforo, grafica y CSV. PDF/XLSX pendientes. |
| Pruebas academicas | Pruebas automaticas por main; JUnit pendiente. |
| Diagramas y manual | docs/diseno.md y docs/manual.md actualizados. |
| Viabilidad y validacion externa | Requieren el trabajo real del grupo; no se inventaron evidencias. |

## Verificacion realizada

El script scripts/probar-general.ps1 compila todas las fuentes y pruebas con --release 17. Se ejecuto con el JDK disponible, 26.0.1, generando codigo compatible con Java 17.

| Grupo | Casos correctos |
| --- | ---: |
| Modelo | 65 |
| DAO y conexion | 29 |
| Servicios, calculadora, permisos y autenticacion | 53 |
| Inicializacion atomica y datos de ejemplo | 4 |
| Controladores, errores y eventos | 46 |
| Ventanas y utilidades | 10 |
| Total | 207 |

Se construyeron las ventanas sin mostrarlas al usuario y se revisaron imagenes de login, primer uso, menu, carrito, compras, balance y catalogos. Se comprobo tambien que las ventanas concretas aceptan los eventos de sus controladores. Esto no sustituye una prueba manual de todos los recorridos en la instalacion final del grupo.

Un intento inicial de ejecutar varias JVM en paralelo agoto la memoria del entorno. Los scripts finales ejecutan las pruebas secuencialmente y limitan memoria. Esa verificacion termino correctamente.

## Conformidad con las decisiones hechas

La guia coloca ConexionBD en DAO; el proyecto conserva su administrador unico en conexion por decision del grupo. Las interfaces de vistas permiten probar sin JFrame y no agregan librerias.

Los indicadores de meta calculable y estimada se agregaron a BalanceDia para representar correctamente el dia sin ventas. Sin margen positivo no se declara una meta verde de cero. El saldo esperado de caja admite valores negativos para no ocultar egresos mayores que cobros; el efectivo contado sigue siendo no negativo. Si ya existe una version anterior de la base, hay que revisar su CHECK antes de admitir ese caso.

GastoFijo registrado representa un gasto adicional, no la repeticion del catalogo de costos. El grupo debe confirmar la convencion de caja en su validacion real.

PBKDF2 y la cola de tareas son APIs del JDK. No se usa Spring, Hibernate, Lombok, bibliotecas de graficas ni paquetes de cifrado externos. Estas partes estan aisladas y comentadas porque no conviene sustituirlas por claves en texto plano o consultas que congelen Swing.

## Pendientes para afirmar entrega completa

1. La guia pide JUnit. Las 207 verificaciones actuales son mains de Java. Debe acordarse con el profesor aceptar esa adaptacion o incorporar JUnit como dependencia exclusiva de pruebas.
2. La guia pide PDF y Excel. CSV no equivale a PDF ni XLSX. Antes de introducir librerias de exportacion debe cumplirse la consulta al profesor indicada en la misma guia.
3. El grupo debe realizar la prueba manual final, la validacion con un negocio y las secciones de viabilidad, observaciones y trabajo futuro del informe.
4. El esquema definitivo debe compararse con sql/esquema.sql antes de instalar o migrar. La revision no altero la base de produccion.

El siguiente trabajo debe partir de estos pendientes reales; no se debe afirmar que la guia esta cumplida al cien por ciento mientras sigan abiertos.
