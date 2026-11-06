# Correcciones y pruebas del paquete modelo

Fecha: 6 de noviembre de 2026.

El paquete modelo se completo segun la seccion 3 de la guia y se verifico mediante 63 pruebas de consola: todas pasaron. Se usa exclusivamente Java estandar y la excepcion propia de stock. No se agregaron dependencias al POM ni se modificaron servicios, DAO, controladores o vistas.

## Cambios realizados

- Rol y Permiso son ahora enums compartidos. Se eliminaron los enums duplicados dentro de Usuario. Se conservaron getId, getNombre, getDescripcion y fromString donde corresponde.
- Usuario valida identificador, nombre, username, hash, rol y fecha. El rol queda fijado por la subclase. setRol acepta el mismo rol y rechaza uno distinto o nulo, evitando que un Cajero se identifique como Jefe manteniendo permisos de cajero.
- Administrador, Cajero y Jefe usan los tipos compartidos y conservan los permisos y menus de la guia.
- Gasto es abstracta. GastoFijo divide el monto mensual entre dias de operacion y GastoVariable devuelve todo el monto del dia: se demuestra herencia y polimorfismo.
- CostoFijo representa un importe mensual configurable y permite calcular su valor diario.
- Negocio valida dias de operacion de 1 a 31 y un cierre posterior a la apertura. setHorario valida ambas horas antes de modificarlas. Los umbrales predeterminados son 70 y 15.
- RegistroCambio tiene campos privados final, constructor validado y getters sin setters. Sus valores monetarios, motivo y fecha no se pueden reemplazar. Venta y Usuario siguen siendo asociaciones a entidades mutables: el registro no convierte esas entidades completas en copias historicas. Los importes anterior y nuevo quedan independientes de cambios posteriores de la venta.
- BalanceDia es un objeto final con campos privados final y solo getters. Recibe resultados ya calculados, sin depender del futuro servicio CalculadoraFinanciera.
- CierreDiario almacena datos del cierre y calcula diferencia = efectivo contado menos efectivo del sistema. Cambiar cualquiera de los dos importes actualiza la diferencia. Se detectan sobrantes y faltantes y se permite un resultado contable negativo.
- Se documentaron atributos, constructores, validaciones y operaciones con comentarios explicativos. Los setters llamados desde constructores son final para que una subclase no omita sus validaciones durante la construccion.

## Decisiones de implementacion

1. Dinero con BigDecimal. Los calculos diarios de gastos y costos redondean a dos decimales con HALF_UP.
2. Los umbrales de Negocio y porcentajeAvance de BalanceDia se expresan de 0 a 100; el avance puede superar 100. Producto.calcularMargen sigue devolviendo un porcentaje double (40 significa 40 por ciento), no dinero. La razon de margen usada para dividir costos fijos se expresa de 0 a 1 (0.40).
3. El identificador cero representa un objeto nuevo antes de persistir. No se admiten identificadores negativos.
4. Los constructores de las entidades nuevas reciben los datos obligatorios; no se agregaron constructores vacios que permitan crearlas sin informacion requerida.
5. RegistroCambio y BalanceDia no tienen setters por exigencia de la guia. CierreDiario no tiene setter de diferencia porque ese valor es derivado.
6. El modelo recibe un hash no vacio, pero generar y verificar contraseñas corresponde a autenticacion. Validar texto no certifica que sea un hash criptografico.
7. Las excepciones de validacion nuevas son IllegalArgumentException, que es parte de Java. No se incorporaron validadores, frameworks ni librerias especializadas.
8. El modelo no guarda todas las ventas del negocio en memoria ni abre ventanas. Los textos de mostrarMenu se conservan porque los exige la guia.
9. Agregar detalles al carrito no modifica inventario. La confirmacion del cobro, autorizacion, bloqueo de dias cerrados y transaccion pertenecen a servicios y DAO. Los costos fijos inactivos deben excluirse en esa capa; calcularMontoDiario solo aplica la formula.
10. Las listas de detalles impiden agregar o quitar elementos desde sus getters, pero sus detalles siguen permitiendo actualizar cantidades con validacion. El servicio debera revalidar las operaciones al confirmarlas.

## Pruebas realizadas

Se compilaron todas las clases del modelo y StockInsuficienteException con javac 26.0.1 y se ejecutaron 63 casos, con resultado de 63 correctos y cero fallidos.

Los casos cubren permisos, menus de Jefe, activacion de usuarios, integridad de Usuario por constructor y setters, conversion de roles y permisos, margen y utilidad, stock insuficiente y desbordamiento, totales e identidad de productos, precios historicos, gastos fijos y variables, validacion de horarios y porcentajes, cierre de caja, historial e inmutabilidad estructural de resultados.

El ejemplo financiero integra una Venta, un GastoFijo y un BalanceDia en memoria. Produce ingresos 850000, costos variables 510000, costos fijos diarios 390000, margen 340000, punto de equilibrio 975000, resultado -50000, faltante 125000 y avance 87.18 por ciento. Las formulas se ejecutan en la prueba; todavia no existe un servicio de calculo operativo en el proyecto.

La compilacion con -Xlint:all solo conserva una advertencia preexistente: StockInsuficienteException no declara serialVersionUID. No impide las pruebas y no se modifico el paquete excepciones.

## Como repetir la verificacion

Desde PowerShell en la raiz del proyecto:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/probar-modelo.ps1
```

El script compila exclusivamente el modelo y sus pruebas en una carpeta nueva dentro de target/revision-modelo. Devuelve codigo cero si todo pasa y un codigo distinto de cero si falla la compilacion o algun caso.

No utiliza Maven, JUnit, MySQL ni JFrame. La guia pide JUnit para la entrega final: estas pruebas de consola ofrecen una verificacion reproducible sin instalar librerias, pero no se presentan como una suite JUnit.

## Limites de la verificacion

El POM sigue solicitando Java 27, mientras que el javac disponible es 26.0.1. El modelo compilo con este JDK, pero no se certifica el build Maven completo ni se cambio su configuracion.

La independencia MVC del modelo se conserva. No se certifican persistencia, login ni flujos completos de interfaz: requieren implementar y probar las otras capas. El cambio de tipos es intencional: los consumidores futuros deben importar modelo.Rol y modelo.Permiso, no Usuario.Rol ni Usuario.Permiso.

Se conservaron los movimientos de archivos ya preparados por el usuario y la eliminacion existente de prueba3.java.

