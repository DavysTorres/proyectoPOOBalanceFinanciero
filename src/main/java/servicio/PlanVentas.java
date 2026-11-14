package servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Simulacion explicita; no modifica balances, inventario ni metas observadas. */
public final class PlanVentas {
    private PlanVentas() { }
    public static BigDecimal ventasNecesarias(BigDecimal costosFijos, BigDecimal margenPorcentaje, BigDecimal ganancia) {
        if (costosFijos == null || costosFijos.signum() < 0 || ganancia == null || ganancia.signum() < 0)
            throw new IllegalArgumentException("Costos y ganancia deben ser valores no negativos.");
        if (margenPorcentaje == null || margenPorcentaje.signum() <= 0 || margenPorcentaje.compareTo(new BigDecimal("100")) > 0)
            throw new IllegalArgumentException("El margen previsto debe ser mayor que 0 y hasta 100 %.");
        return costosFijos.add(ganancia).multiply(new BigDecimal("100")).divide(margenPorcentaje, 2, RoundingMode.CEILING);
    }
}
