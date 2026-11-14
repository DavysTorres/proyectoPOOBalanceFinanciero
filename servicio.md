Estado actualizado: ver revision-general.md. Se retiro dotenv-java; ConfiguracionEntorno usa Java estandar y permanece solo la dependencia JDBC.

# Paquete servicio y reglas de negocio

La capa servicio conecta los controladores con los DAO y aplica las reglas de la guia. Se implemento en el paquete servicio, como indica el documento, sin frameworks ni dependencias nuevas. No contiene SQL ni abre conexiones; reutiliza los DAO existentes y conexion.conexionDB sigue siendo el unico administrador de conexion de la aplicacion.

## Responsabilidades y POO

| Clase | Funcion y concepto de POO |
| --- | --- |
| Sesion | Singleton para un usuario autenticado y comprobacion central de permisos. No es otro Singleton de conexion. |
| ServicioBase | Herencia para compartir sesion, fecha y validaciones de texto. |
| ServicioAutenticacion | Login, cierre de sesion, hash de claves, tres intentos y bloqueo temporal de cinco minutos. |
| ServicioUsuarios | Factory de perfiles, username unico, edicion y baja logica. Protege al ultimo administrador activo. |
| ServicioProductos | Administracion del catalogo y consultas para el buscador de ventas. |
| ServicioCostosFijos | Crear, editar, listar y desactivar costos mensuales. |
| ServicioConfiguracion | Guardar y recuperar Negocio con permiso de administrador. |
| ServicioVentas | Carrito, cantidades acumuladas, cobro, cancelacion y ajuste de total con historial. |
| ServicioCompras | Registro de compras y gastos con usuario y fecha obtenidos de la sesion. |
| CalculadoraFinanciera | Funciones puras con BigDecimal; no usa sesion ni base de datos. |
| ServicioBalance | Compone resultados consultando ventas, gastos, costos, configuracion y cierres. |
| EstadoNegocio y EstadoRojo/Amarillo/Verde | Interfaz y polimorfismo para el semaforo sin componentes Swing. |
| Diagnostico | Decide el estado, calcula fraccion del horario y explica la proyeccion. |
| AnalizadorMargen | Ranking de productos, deteccion de margen bajo y consejo por producto. |
| ServicioCierre | Foto financiera, caja esperada, diferencia y prohibicion de movimientos en dias cerrados. |
| ServicioHistorial | Consultas autorizadas; no modifica ni borra evidencia. |
| Exportable y ServicioExportacion | Contrato de exportacion y CSV con Java estandar. |
| ServiciosAplicacion | Composicion: construye y comparte las dependencias de todos los servicios. |

Los bloques de codigo tienen comentarios sobre su funcion. Se utilizan interfaces, herencia, composicion, encapsulacion, polimorfismo y excepciones propias, sin ORM ni contenedor de inyeccion.

## Uso por los futuros controladores

Crear una sola instancia de ServiciosAplicacion y compartirla entre todos los controladores:

```java
ServiciosAplicacion servicios = new ServiciosAplicacion();

// En la configuracion inicial, solo cuando usuarios este vacia:
servicios.getUsuarios().crearAdministradorInicial(nombre, username, clave);

// En el controlador de login:
Usuario usuario = servicios.getAutenticacion().iniciarSesion(username, clave);

// En el controlador de ventas:
servicios.getVentas().iniciarVenta();
servicios.getVentas().agregarProducto(producto, cantidad);
Venta cobrada = servicios.getVentas().cobrar();
```

El primer administrador se crea explicitamente y solo si no hay usuarios. No hay contrasenas predeterminadas, hashes inventados ni creacion automatica de datos en la base real. Despues se configura Negocio y se crean los demas perfiles y productos como administrador.

La autenticacion debe conservar su instancia entre clics: crear un ServicioAutenticacion nuevo en cada intento perderia los contadores. ServiciosAplicacion mantiene esa instancia. Las ventanas ya se conectan mediante controladores y AplicacionDesktop. ServiciosAplicacion.cerrarSesion descarta el carrito y termina la autenticacion.

## Permisos

- Administrador: todas las capacidades de Permiso.
- Jefe: ventas, compras, gastos, modificacion de ventas, balance, reportes, historial y cierre.
- Cajero: registrar ventas y consultar el estado del semaforo. No recibe BalanceDia ni las cifras financieras.
- Consultar catalogo activo y alertas requiere una sesion activa.
- Sesion.exigirPermiso comprueba tambien que el usuario actual siga activo.
- No se puede desactivar o degradar al ultimo administrador activo.
- Cambiar el rol de un usuario reconstruye la subclase apropiada; no altera el rol fijo de un Cajero para fingir que es Jefe.

Los controladores deben presentar errores de negocio sin mostrar trazas SQL. StockInsuficienteException conserva su tipo comprobado; agregarProducto y cobrar lo declaran. Los demas errores de servicio son RuntimeException propias. Las validaciones ya existentes del modelo pueden producir IllegalArgumentException.

## Autenticacion con Java estandar

ServicioAutenticacion usa PBKDF2WithHmacSHA256, una sal aleatoria de 16 bytes por clave, 600000 iteraciones y un resultado de 256 bits. Guarda algoritmo, iteraciones, sal y resultado en password_hash. No guarda texto plano ni utiliza un cifrado reversible. El nombre cifrarClave se mantiene por el contrato de la guia, pero su operacion real es un hash de contrasena.

Se utilizan SecretKeyFactory, PBEKeySpec, SecureRandom, Base64 y MessageDigest del JDK. Las claves nuevas tienen entre 8 y 256 caracteres y no se recortan sus espacios. Los hashes ficticios de pruebas antiguas, como hash_prueba, no autentican: las cuentas reales deben crearse o restablecerse mediante este servicio.

Fuentes verificadas para esta decision: [SecretKeyFactory del JDK](https://docs.oracle.com/en/java/javase/26/docs/api/java.base/javax/crypto/SecretKeyFactory.html) y [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html).

El bloqueo es por cuenta y usa su username canonico para evitar que las mayusculas eludan el contador. Tres fallos bloquean cinco minutos; al cumplirse el plazo se permite intentar de nuevo. Los contadores son memoria del proceso, como en el diseno de la guia: reiniciar la aplicacion los reinicia. No se presenta esta implementacion de escritorio como un sistema de autenticacion distribuido.

## Ventas, compras e historial

El carrito no altera existencias. agregarProducto consulta el producto persistido y comprueba la cantidad acumulada de lineas repetidas. Al cobrar se revisa nuevamente disponibilidad, precios y costo. Si el catalogo cambio, se exige reiniciar la venta para no cobrar una cifra obsoleta.

La venta pertenece al usuario y al dia con que se inicio. Otro usuario no puede cobrar ese carrito. Una venta de otra jornada se cancela antes de iniciar una nueva. No se permite cobrar dos veces ni cobrar un carrito vacio.

VentaDAO ya descuenta stock y CompraDAO ya lo aumenta y actualiza costo; el servicio no repite esos movimientos. Se recargan productos desde el DAO para ver las nuevas existencias. Usuario y fecha de compras y gastos se fijan con la sesion y el reloj del servicio; no se aceptan fechas arbitrarias de la ventana.

Modificar una venta exige permiso, un total no negativo, motivo y dia abierto. El total y RegistroCambio se escriben en la misma transaccion. Una prueba provoca un fallo en el historial y verifica que el total anterior siga guardado. Este ajuste modifica el importe, no cantidades ni inventario.

DAOBase.transaccion y su interfaz Trabajo ahora son publicos para que un servicio coordine varios DAO sin importar JDBC ni duplicar el administrador de transacciones. Si ya existe una transaccion externa, el bloque usa un savepoint. Todos los DAO de un conjunto de servicios deben compartir la misma conexion.

## Balance y decisiones financieras

Los ingresos se suman con Venta.getTotal para respetar ajustes autorizados. Los costos variables son los costos historicos de lo vendido mas los GastoVariable del dia. No se suman las compras otra vez. El costo mensual activo se divide entre dias de operacion.

Un GastoFijo registrado representa un costo adicional y se prorratea con sus propios dias de operacion. El servicio rechaza un concepto igual al de un costo fijo activo para reducir el riesgo de registrarlo dos veces. El grupo debe evitar tambien duplicar el mismo costo bajo nombres diferentes.

Los importes usan BigDecimal. Las razones se expresan de 0 a 1; los porcentajes visibles y los umbrales, de 0 a 100. Los montos derivados de division se redondean a dos decimales; la razon mantiene diez decimales.

El ejemplo de la guia produce ingresos 850000, costos variables 510000, margen 340000, costos fijos diarios 390000, resultado -50000, equilibrio 975000, faltante 125000 y avance 87.18 por ciento.

### Dia sin ventas o margen no positivo

Sin ventas, el servicio usa el promedio simple de las razones de margen de los productos activos, incluyendo margenes negativos. No inventa una razon si el catalogo esta vacio o su promedio no es positivo. Cuando ya hay gastos variables pagados sin ninguna venta, la meta estimada tambien contempla esos gastos para no declarar cubierta una jornada que ya tiene egresos.

BalanceDia incorpora dos indicadores finales: puntoEquilibrioCalculable y puntoEquilibrioEstimado. Conserva el constructor anterior y agrega uno con indicadores. Si no hay meta calculable, los campos numericos de meta, faltante y avance son cero como valores de transporte; las vistas y exportadores deben usar el indicador y mostrar NO CALCULABLE. El semaforo queda rojo. No se interpreta ese cero como una meta lograda.

Cuando no hay ningun costo que cubrir, una meta real de cero es valida. Cuando la razon efectiva es cero o negativa y hay costos, no se presenta un equilibrio financiero finito. Las proyecciones tambien distinguen una estimacion del resultado real.

Los balances de dias abiertos usan la configuracion y el catalogo actuales. Los cierres conservan la foto financiera para que cambios posteriores de costos o dias de operacion no alteren resultados ya cerrados.

## Caja y cierre

Convencion provisional, a falta de confirmacion del grupo: todas las ventas se cobran en efectivo, las compras y gastos registrados se pagan con esa caja y no hay fondo inicial. Por tanto:

efectivoSistema = total de ventas ajustadas - total de compras - monto completo de gastos pagados.

El costo de mercancia vendida y los costos mensuales configurados no se descuentan otra vez del efectivo, pues son calculos contables y no pagos adicionales. Un GastoFijo pagado se descuenta completo de caja, aunque solo su prorrateo diario afecte el resultado.

El saldo esperado puede ser negativo si se registraron pagos mayores que los cobros sin fondo inicial; no se disfraza como cero. El efectivo contado sigue siendo no negativo. Para admitir esa situacion se ajusto la validacion de CierreDiario y se retiro del esquema de referencia solo el CHECK que exigia efectivo_sistema no negativo. No se ejecutaron alteraciones sobre la base real. Si ya se instalo una version anterior del esquema, esa restriccion debe revisarse antes de admitir esos saldos.

Si el grupo decide que las compras y gastos se pagan por separado, se adapta efectivoDelDia en ServicioCierre y las expectativas de las pruebas. El modelo actual no tiene medios de pago, prestamos, aportes ni fondo inicial: esas diferencias requieren modelarse antes de usarse como una caja contable completa.

No se permite cerrar una fecha futura ni cerrar dos veces. Ventas, compras, gastos y ajustes consultan el cierre antes de guardar. Se trabaja con el modelo de una aplicacion de escritorio y su conexion compartida; no se afirma un bloqueo distribuido de jornada entre varios procesos. La proteccion atomica de stock si sigue estando en SQL.

## Exportacion

ServicioExportacion implementa Exportable y genera un CSV real, UTF-8 con BOM, separado por punto y coma y con coma decimal para hojas configuradas en espanol. Requiere VER_REPORTES y no sobrescribe archivos existentes.

No se generan PDF ni XLSX, ni se cambia la extension de un CSV para aparentarlos. La guia ubica esas exportaciones en la fase de pulido y pide confirmar con el profesor el uso de librerias. La interfaz permite agregar despues otro formato. No se ha agregado ninguna libreria especializada para exportar.

## Pruebas y limites

```powershell
# Formulas, permisos, autenticacion, semaforo y CSV, sin conectar a MySQL:
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/probar-servicio.ps1 -SoloUnitarias

# Ademas simula un dia completo contra MySQL en una base temporal:
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/probar-servicio.ps1

# Regresion de las otras capas:
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/probar-modelo.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/probar-dao.ps1
```

Se verificaron 28 casos unitarios de servicios y 25 casos con servicios y DAO reales en MySQL. Tambien se mantienen 65 pruebas del modelo y 29 de DAO. La prueba real usa usuarios y productos unicamente de un esquema aleatorio creado para el ensayo; el esquema se elimina al terminar. No se crean cuentas ni movimientos en la base original.

BaseTemporal comparte la infraestructura de creacion y limpieza entre las pruebas DAO y de servicio. Los casos de autenticacion usan PBKDF2 real y un reloj controlable para probar el bloqueo sin esperar cinco minutos.

Se usan mains de Java, no JUnit. No se agregaron dependencias de pruebas; JUnit sigue siendo un requisito a adaptar para la entrega academica final si el profesor lo exige. El JDK disponible es 26.0.1 y el POM utiliza --release 17. La revision verifica compilacion directa y pruebas, no un build Maven ejecutado por consola.

Las ventanas ya usan ServiciosAplicacion a traves de sus controladores. No deben saltarse los servicios para ejecutar DAO directamente.
