package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Resultado inmutable que el futuro servicio llenara usando CalculadoraFinanciera.
 */
public final class BalanceDia {
    // Datos ya calculados: el modelo no consulta DAO ni ejecuta la calculadora.
    private final LocalDate fecha;
    private final BigDecimal ingresos;
    private final BigDecimal costosVariables;
    private final BigDecimal margenContribucion;
    private final BigDecimal costosFijosDia;
    private final BigDecimal resultado;
    private final BigDecimal puntoEquilibrio;
    private final BigDecimal faltante;
    private final BigDecimal porcentajeAvance;
    // Distingue un equilibrio de cero real de una actividad sin margen positivo.
    private final boolean puntoEquilibrioCalculable;
    private final boolean puntoEquilibrioEstimado;

    // Solo el constructor asigna resultados; margen y resultado pueden ser negativos.
    public BalanceDia(LocalDate fecha,
                      BigDecimal ingresos,
                      BigDecimal costosVariables,
                      BigDecimal margenContribucion,
                      BigDecimal costosFijosDia,
                      BigDecimal resultado,
                      BigDecimal puntoEquilibrio,
                      BigDecimal faltante,
                      BigDecimal porcentajeAvance) {
        this(fecha, ingresos, costosVariables, margenContribucion, costosFijosDia,
                resultado, puntoEquilibrio, faltante, porcentajeAvance, true, false);
    }

    // El servicio indica si la meta es valida o una estimacion basada en el catalogo.
    public BalanceDia(LocalDate fecha, BigDecimal ingresos, BigDecimal costosVariables,
                      BigDecimal margenContribucion, BigDecimal costosFijosDia, BigDecimal resultado,
                      BigDecimal puntoEquilibrio, BigDecimal faltante, BigDecimal porcentajeAvance,
                      boolean calculable, boolean estimado) {
        this.fecha = ValidacionModelo.obligatorio(fecha, "Fecha");
        this.ingresos = ValidacionModelo.noNegativo(ingresos, "Ingresos");
        this.costosVariables = ValidacionModelo.noNegativo(costosVariables, "CostosVariables");
        this.margenContribucion = ValidacionModelo.obligatorio(margenContribucion, "MargenContribucion");
        this.costosFijosDia = ValidacionModelo.noNegativo(costosFijosDia, "CostosFijosDia");
        this.resultado = ValidacionModelo.obligatorio(resultado, "Resultado");
        this.puntoEquilibrio = ValidacionModelo.noNegativo(puntoEquilibrio, "PuntoEquilibrio");
        this.faltante = ValidacionModelo.noNegativo(faltante, "Faltante");
        this.porcentajeAvance = ValidacionModelo.noNegativo(porcentajeAvance, "PorcentajeAvance");
        if (!calculable && (puntoEquilibrio.signum() != 0 || faltante.signum() != 0 || porcentajeAvance.signum() != 0))
            throw new IllegalArgumentException("Un equilibrio no calculable no debe presentar una meta numerica");
        if (!calculable && estimado) throw new IllegalArgumentException("Una estimacion debe ser calculable");
        this.puntoEquilibrioCalculable = calculable;
        this.puntoEquilibrioEstimado = estimado;
    }

    // Consulta fecha sin permitir cambios posteriores.
    public LocalDate getFecha() {
        return fecha;
    }

    // Consulta ingresos sin permitir cambios posteriores.
    public BigDecimal getIngresos() {
        return ingresos;
    }

    // Consulta costosVariables sin permitir cambios posteriores.
    public BigDecimal getCostosVariables() {
        return costosVariables;
    }

    // Consulta margenContribucion sin permitir cambios posteriores.
    public BigDecimal getMargenContribucion() {
        return margenContribucion;
    }

    // Consulta costosFijosDia sin permitir cambios posteriores.
    public BigDecimal getCostosFijosDia() {
        return costosFijosDia;
    }

    // Consulta resultado sin permitir cambios posteriores.
    public BigDecimal getResultado() {
        return resultado;
    }

    // Consulta puntoEquilibrio sin permitir cambios posteriores.
    public BigDecimal getPuntoEquilibrio() {
        return puntoEquilibrio;
    }

    // Consulta faltante sin permitir cambios posteriores.
    public BigDecimal getFaltante() {
        return faltante;
    }

    // Consulta porcentajeAvance sin permitir cambios posteriores.
    public BigDecimal getPorcentajeAvance() {
        return porcentajeAvance;
    }

    // La vista debe consultar estos indicadores antes de mostrar la meta o el avance.
    public boolean isPuntoEquilibrioCalculable() {
        return puntoEquilibrioCalculable;
    }

    public boolean isPuntoEquilibrioEstimado() {
        return puntoEquilibrioEstimado;
    }
}
