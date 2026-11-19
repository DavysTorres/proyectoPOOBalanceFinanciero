package servicio;

import java.math.BigDecimal;

/**
 * Datos visibles para todos los perfiles; no incluye ingresos, costos ni resultado monetario.
 */
public final class ResumenSemaforo {
    private final EstadoNegocio estado;
    private final String mensaje;
    private final BigDecimal porcentaje;
    private final boolean calculable;

    public ResumenSemaforo(EstadoNegocio estado, String mensaje, BigDecimal porcentaje, boolean calculable) {
        this.estado = estado;
        this.mensaje = mensaje;
        this.porcentaje = porcentaje;
        this.calculable = calculable;
    }

    public EstadoNegocio getEstado() {
        return estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public boolean isCalculable() {
        return calculable;
    }
}
