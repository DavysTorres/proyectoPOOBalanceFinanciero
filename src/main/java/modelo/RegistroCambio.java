package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RegistroCambio {

    private final int id;
    private final Venta venta;
    private final Usuario usuario;
    private final LocalDateTime fechaHora;
    private final BigDecimal valorAnterior;
    private final BigDecimal valorNuevo;
    private final String motivo;

    public RegistroCambio(int id, Venta venta, Usuario usuario, LocalDateTime fechaHora,
                          BigDecimal valorAnterior, BigDecimal valorNuevo, String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo es obligatorio");
        }
        this.id = id;
        this.venta = venta;
        this.usuario = usuario;
        this.fechaHora = fechaHora;
        this.valorAnterior = valorAnterior;
        this.valorNuevo = valorNuevo;
        this.motivo = motivo;
    }

    public int getId() {
        return id;
    }

    public Venta getVenta() {
        return venta;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public BigDecimal getValorAnterior() {
        return valorAnterior;
    }

    public BigDecimal getValorNuevo() {
        return valorNuevo;
    }

    public String getMotivo() {
        return motivo;
    }
}
