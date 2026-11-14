# Resultado diario y meta estimada

La frecuencia de abastecimiento (diaria, quincenal o mensual) no se usa como divisor del costo de inventario.

- Compra: salida de caja en la fecha registrada y aumento de inventario.
- Reposici?n: costo promedio ponderado perpetuo = (existencias anteriores ? costo anterior + unidades compradas ? costo del lote) / unidades totales. Se conserva con cuatro decimales.
- Venta: conserva una copia del costo unitario vigente. Una compra posterior no modifica ese detalle hist?rico.
- Resultado registrado: ventas netas registradas menos costo de lo vendido, gastos variables registrados y costos fijos diarios.
- Los costos fijos configurados son mensuales y se dividen entre los d?as de operaci?n configurados.
- Un gasto FIJO adicional es mensual, corresponde al mes de su fecha y aporta su cuota diaria a los balances abiertos de ese mes. Su desembolso afecta la caja solo en la fecha registrada. No se extiende a meses posteriores autom?ticamente.

## Meta

Se calcula con la raz?n (ventas ? costo vendido ? otros gastos variables del d?a) / ventas. La meta es costos fijos diarios / raz?n. Es una estimaci?n condicionada a mantener esa raz?n y a que los registros est?n completos; cambia al registrar nuevas ventas o gastos. No se usa el promedio simple de porcentajes del cat?logo.

Sin ventas o sin margen positivo no se publica una cifra de meta ni de faltante. El resultado registrado s? se muestra, incluso cuando es negativo. El verde significa que el resultado registrado hasta ahora es no negativo, con ventas y margen positivo; no certifica rentabilidad futura ni dinero disponible para reponer inventario.

## Alcance de los datos

El cierre de caja mantiene la convenci?n existente: todas las ventas y pagos registrados son en efectivo, sin fondo inicial. No representa cuentas por cobrar o pagar ni compras a cr?dito.

Los cierres conservan las cifras guardadas; no se reescriben ni se corrigen operaciones anteriores. El promedio ponderado empieza con el stock y costo actualmente almacenados: si son incorrectos o proceden de datos de ejemplo, deben conciliarse con el inventario real. Las ediciones manuales de costos tambi?n afectan las ventas futuras.

Los costos configurados no tienen historial de vigencias: cambiar su importe puede cambiar balances pasados que a?n no est?n cerrados. El gasto mensual adicional no tiene un calendario de d?as abiertos; su cuota depende del divisor indicado. La precisi?n requiere configurar ese divisor correctamente y registrar todos los costos, mermas y gastos que correspondan. El sistema no infiere gastos omitidos, obligaciones futuras ni una utilidad deseada.

## Verificaci?n

RevisionEquilibrio usa DAO sustitutos sin conexiones para comprobar ausencia de ventas, margen ponderado de ventas, gasto fijo de otra fecha del mismo mes y margen negativo. PruebaDAO comprueba el promedio de 18 unidades a 6000 y 3 unidades a 7000: costo resultante 6142.8571; requiere la base temporal MySQL del proyecto.

## Datos de demostraci?n

Se retiraron el script de carga y su generador del proyecto. Las pruebas conservan casos sint?ticos ?nicamente en memoria y en esquemas temporales. La base configurada en `.env` se conserva por decisi?n del usuario; no se borraron sus registros ni se sustituyeron por ceros.

El resumen muestra ventas, resultado, meta y faltante estimados, costos variables, costos fijos y margen de contribuci?n. Al iniciar una actualizaci?n limpia las cifras anteriores; si falla, muestra ?Sin datos? y no conserva un sem?foro que parezca vigente.
