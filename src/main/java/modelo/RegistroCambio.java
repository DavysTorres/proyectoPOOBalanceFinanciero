package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Evidencia de una modificacion: no ofrece setters ni operaciones de borrado.
 */
public final class RegistroCambio {
    // Los valores, motivo y fecha quedan fijados; venta y usuario son asociaciones.
    private final int id;
    private final Venta venta;
    private final Usuario usuario;
    private final LocalDateTime fechaHora;
    private final BigDecimal valorAnterior;
    private final BigDecimal valorNuevo;
    private final String motivo;

    // El DAO reconstruye el registro con su id; cero representa uno aun no guardado.
    public RegistroCambio(int id,
                          Venta venta,
                          Usuario usuario,
                          LocalDateTime fechaHora,
                          BigDecimal valorAnterior,
                          BigDecimal valorNuevo,
                          String motivo) {
        this.id = ValidacionModelo.id(id);
        this.venta = ValidacionModelo.obligatorio(venta, "Venta");
        this.usuario = ValidacionModelo.obligatorio(usuario, "Usuario");
        this.fechaHora = ValidacionModelo.obligatorio(fechaHora, "FechaHora");
        this.valorAnterior = ValidacionModelo.noNegativo(valorAnterior, "ValorAnterior");
        this.valorNuevo = ValidacionModelo.noNegativo(valorNuevo, "ValorNuevo");
        this.motivo = ValidacionModelo.texto(motivo, "Motivo");
    }

    // Consulta id; la asociacion no permite reemplazar datos del registro.
    public int getId() {
        return id;
    }

    // Consulta venta; la asociacion no permite reemplazar datos del registro.
    public Venta getVenta() {
        return venta;
    }

    // Consulta usuario; la asociacion no permite reemplazar datos del registro.
    public Usuario getUsuario() {
        return usuario;
    }

    // Consulta fechaHora; la asociacion no permite reemplazar datos del registro.
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    // Consulta valorAnterior; la asociacion no permite reemplazar datos del registro.
    public BigDecimal getValorAnterior() {
        return valorAnterior;
    }

    // Consulta valorNuevo; la asociacion no permite reemplazar datos del registro.
    public BigDecimal getValorNuevo() {
        return valorNuevo;
    }

    // Consulta motivo; la asociacion no permite reemplazar datos del registro.
    public String getMotivo() {
        return motivo;
    }
}
