package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Abstraccion de un gasto: comparte datos, pero delega su calculo diario.
 */
public abstract class Gasto {
    // Estado encapsulado comun a gastos fijos y variables.
    private int id;
    private Usuario usuario;
    private String concepto;
    private BigDecimal monto;
    private LocalDate fecha;

    // Las subclases reutilizan estas validaciones mediante super.
    protected Gasto(int id, Usuario usuario, String concepto, BigDecimal monto, LocalDate fecha) {
        setId(id);
        setUsuario(usuario);
        setConcepto(concepto);
        setMonto(monto);
        setFecha(fecha);
    }

    // El id cero permite crear gastos antes de persistirlos.
    public int getId() {
        return id;
    }

    public final void setId(int id) {
        this.id = ValidacionModelo.id(id);
    }

    // Identifica a quien registra el gasto sin realizar consultas a la base de datos.
    public Usuario getUsuario() {
        return usuario;
    }

    public final void setUsuario(Usuario usuario) {
        this.usuario = ValidacionModelo.obligatorio(usuario, "El usuario");
    }

    // El concepto describe el motivo del gasto.
    public String getConcepto() {
        return concepto;
    }

    public final void setConcepto(String concepto) {
        this.concepto = ValidacionModelo.texto(concepto, "El concepto");
    }

    // BigDecimal conserva el importe decimal sin errores propios de double.
    public BigDecimal getMonto() {
        return monto;
    }

    public final void setMonto(BigDecimal monto) {
        this.monto = ValidacionModelo.positivo(monto, "El monto");
    }

    // La fecha relaciona el gasto con el periodo contable correspondiente.
    public LocalDate getFecha() {
        return fecha;
    }

    public final void setFecha(LocalDate fecha) {
        this.fecha = ValidacionModelo.obligatorio(fecha, "La fecha");
    }

    // Cada subclase aplica su formula al invocarse mediante una referencia Gasto.
    public abstract BigDecimal calcularMontoDiario();
}
