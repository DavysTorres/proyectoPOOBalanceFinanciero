package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Representa los datos del cierre; impedir cerrar dos veces corresponde al servicio y al DAO. */
public class CierreDiario {
    // Encapsula los resultados y el conteo de efectivo de una fecha.
    private int id;
    private LocalDate fecha;
    private Usuario usuario;
    private BigDecimal ingresos;
    private BigDecimal costosVariables;
    private BigDecimal costosFijos;
    private BigDecimal resultado;
    private BigDecimal puntoEquilibrio;
    private BigDecimal efectivoSistema;
    private BigDecimal efectivoContado;
    private BigDecimal diferencia;

    // Valida todos los datos; el resultado puede ser negativo si hubo perdida.
    public CierreDiario(int id,
                        LocalDate fecha,
                        Usuario usuario,
                        BigDecimal ingresos,
                        BigDecimal costosVariables,
                        BigDecimal costosFijos,
                        BigDecimal resultado,
                        BigDecimal puntoEquilibrio,
                        BigDecimal efectivoSistema,
                        BigDecimal efectivoContado) {
        setId(id);
        setFecha(fecha);
        setUsuario(usuario);
        setIngresos(ingresos);
        setCostosVariables(costosVariables);
        setCostosFijos(costosFijos);
        setResultado(resultado);
        setPuntoEquilibrio(puntoEquilibrio);
        setEfectivoSistema(efectivoSistema);
        setEfectivoContado(efectivoContado);
    }

    // Consulta y actualiza id aplicando su regla de integridad.
    public int getId() { return id; }
    public final void setId(int id) {
        this.id = ValidacionModelo.id(id);
    }

    // Consulta y actualiza fecha aplicando su regla de integridad.
    public LocalDate getFecha() { return fecha; }
    public final void setFecha(LocalDate fecha) {
        this.fecha = ValidacionModelo.obligatorio(fecha, "Fecha");
    }

    // Consulta y actualiza usuario aplicando su regla de integridad.
    public Usuario getUsuario() { return usuario; }
    public final void setUsuario(Usuario usuario) {
        this.usuario = ValidacionModelo.obligatorio(usuario, "Usuario");
    }

    // Consulta y actualiza ingresos aplicando su regla de integridad.
    public BigDecimal getIngresos() { return ingresos; }
    public final void setIngresos(BigDecimal ingresos) {
        this.ingresos = ValidacionModelo.noNegativo(ingresos, "Ingresos");
    }

    // Consulta y actualiza costosVariables aplicando su regla de integridad.
    public BigDecimal getCostosVariables() { return costosVariables; }
    public final void setCostosVariables(BigDecimal costosVariables) {
        this.costosVariables = ValidacionModelo.noNegativo(costosVariables, "CostosVariables");
    }

    // Consulta y actualiza costosFijos aplicando su regla de integridad.
    public BigDecimal getCostosFijos() { return costosFijos; }
    public final void setCostosFijos(BigDecimal costosFijos) {
        this.costosFijos = ValidacionModelo.noNegativo(costosFijos, "CostosFijos");
    }

    // Consulta y actualiza resultado aplicando su regla de integridad.
    public BigDecimal getResultado() { return resultado; }
    public final void setResultado(BigDecimal resultado) {
        this.resultado = ValidacionModelo.obligatorio(resultado, "Resultado");
    }

    // Consulta y actualiza puntoEquilibrio aplicando su regla de integridad.
    public BigDecimal getPuntoEquilibrio() { return puntoEquilibrio; }
    public final void setPuntoEquilibrio(BigDecimal puntoEquilibrio) {
        this.puntoEquilibrio = ValidacionModelo.noNegativo(puntoEquilibrio, "PuntoEquilibrio");
    }

    // Consulta y actualiza efectivoSistema aplicando su regla de integridad.
    public BigDecimal getEfectivoSistema() { return efectivoSistema; }
    public final void setEfectivoSistema(BigDecimal efectivoSistema) {
        this.efectivoSistema = ValidacionModelo.noNegativo(efectivoSistema, "EfectivoSistema");
        if (efectivoSistema != null && efectivoContado != null) calcularDiferenciaCaja();
    }

    // Consulta y actualiza efectivoContado aplicando su regla de integridad.
    public BigDecimal getEfectivoContado() { return efectivoContado; }
    public final void setEfectivoContado(BigDecimal efectivoContado) {
        this.efectivoContado = ValidacionModelo.noNegativo(efectivoContado, "EfectivoContado");
        if (efectivoSistema != null && efectivoContado != null) calcularDiferenciaCaja();
    }

    // La diferencia es derivada y no tiene setter para evitar inconsistencias.
    public BigDecimal getDiferencia() { return diferencia; }

    // Un valor negativo indica faltante; uno positivo, sobrante de efectivo.
    public final BigDecimal calcularDiferenciaCaja() {
        diferencia = efectivoContado.subtract(efectivoSistema);
        return diferencia;
    }

    // compareTo considera iguales 0.00 y 0 aunque tengan distinta escala.
    public boolean hayDescuadre() {
        return calcularDiferenciaCaja().compareTo(BigDecimal.ZERO) != 0;
    }
}
