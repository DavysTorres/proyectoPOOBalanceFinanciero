package servicio;

import modelo.*;
import excepciones.DatosInvalidosException;

import java.math.*;
import java.util.List;

/**
 * Funciones puras: calculan resultados sin sesion, DAO, SQL ni ventanas.
 */
public class CalculadoraFinanciera {
    private static final BigDecimal CIEN = new BigDecimal("100");

    // Los ingresos usan el total registrado para respetar descuentos autorizados.
    public BigDecimal calcularIngresos(List<Venta> ventas) {
        BigDecimal suma = BigDecimal.ZERO;
        for (Venta v : ventas) suma = suma.add(v.getTotal());
        return suma;
    }

    // Se suma el costo historico vendido y los gastos variables, nunca las compras otra vez.
    public BigDecimal calcularCostosVariables(List<Venta> ventas, List<Gasto> gastos) {
        BigDecimal suma = BigDecimal.ZERO;
        for (Venta v : ventas) suma = suma.add(v.calcularCostoTotal());
        for (Gasto g : gastos) if (g instanceof GastoVariable) suma = suma.add(g.calcularMontoDiario());
        return suma;
    }

    // Margen y resultado pueden ser negativos: representan una perdida.
    public BigDecimal calcularMargenContribucion(BigDecimal ingresos, BigDecimal costosVariables) {
        return ingresos.subtract(costosVariables);
    }

    public BigDecimal calcularResultado(BigDecimal margen, BigDecimal costosFijosDia) {
        return margen.subtract(costosFijosDia);
    }

    // La razon usa escala de 0 a 1, a diferencia de los porcentajes de la vista.
    public BigDecimal calcularRazonMargen(BigDecimal margen, BigDecimal ingresos) {
        noNegativo(ingresos);
        return ingresos.signum() == 0 ? BigDecimal.ZERO : margen.divide(ingresos, 10, RoundingMode.HALF_UP);
    }

    // Divide costos mensuales entre dias validos y redondea el importe diario.
    public BigDecimal calcularCostoFijoDiario(BigDecimal totalMensual, int diasOperacion) {
        noNegativo(totalMensual);
        if (diasOperacion < 1 || diasOperacion > 31) throw new DatosInvalidosException("Dias entre 1 y 31");
        return totalMensual.divide(BigDecimal.valueOf(diasOperacion), 2, RoundingMode.HALF_UP);
    }

    // No inventa un punto de equilibrio si la actividad tiene margen nulo o negativo.
    public BigDecimal calcularPuntoEquilibrio(BigDecimal costosFijosDia, BigDecimal razonMargen) {
        noNegativo(costosFijosDia);
        if (razonMargen.signum() <= 0)
            throw new DatosInvalidosException("No hay margen positivo para calcular el equilibrio");
        return costosFijosDia.divide(razonMargen, 2, RoundingMode.HALF_UP);
    }

    // El faltante nunca baja de cero cuando ya se alcanzo la meta.
    public BigDecimal calcularFaltante(BigDecimal puntoEquilibrio, BigDecimal ingresos) {
        noNegativo(puntoEquilibrio);
        noNegativo(ingresos);
        return puntoEquilibrio.subtract(ingresos).max(BigDecimal.ZERO);
    }

    // Un equilibrio cero valido significa que no hay costos que cubrir.
    public BigDecimal calcularPorcentajeAvance(BigDecimal ingresos, BigDecimal puntoEquilibrio) {
        noNegativo(ingresos);
        noNegativo(puntoEquilibrio);
        return puntoEquilibrio.signum() == 0 ? CIEN :
                ingresos.multiply(CIEN).divide(puntoEquilibrio, 2, RoundingMode.HALF_UP);
    }

    // Se usa double solo para una fraccion temporal; los importes siguen siendo BigDecimal.
    public BigDecimal proyectarCierre(BigDecimal ingresosActuales, double fraccionDelDia) {
        noNegativo(ingresosActuales);
        if (!Double.isFinite(fraccionDelDia) || fraccionDelDia <= 0 || fraccionDelDia > 1)
            throw new DatosInvalidosException("La proyeccion requiere una fraccion mayor que cero y hasta uno");
        return ingresosActuales.divide(BigDecimal.valueOf(fraccionDelDia), 2, RoundingMode.HALF_UP);
    }

    // Regla local para entradas monetarias que no pueden representar una perdida.
    private void noNegativo(BigDecimal valor) {
        if (valor == null || valor.signum() < 0) throw new DatosInvalidosException("Se requiere un monto no negativo");
    }
}
