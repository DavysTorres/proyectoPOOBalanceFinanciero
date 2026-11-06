package modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Configuracion de un costo mensual recurrente, como arriendo o servicios. */
public class CostoFijo {
    // No se almacenan listas de gastos; cada entidad representa un costo configurado.
    private int id;
    private String concepto;
    private BigDecimal montoMensual;
    private boolean activo;

    // Permite recuperar tanto costos activos como desactivados.
    public CostoFijo(int id, String concepto, BigDecimal montoMensual, boolean activo) {
        setId(id);
        setConcepto(concepto);
        setMontoMensual(montoMensual);
        this.activo = activo;
    }

    // Identificador asignado por persistencia; cero para un registro nuevo.
    public int getId() { return id; }
    public final void setId(int id) { this.id = ValidacionModelo.id(id); }

    // Descripcion obligatoria del costo.
    public String getConcepto() { return concepto; }
    public final void setConcepto(String concepto) {
        this.concepto = ValidacionModelo.texto(concepto, "El concepto");
    }

    // El valor mensual debe ser positivo.
    public BigDecimal getMontoMensual() { return montoMensual; }
    public final void setMontoMensual(BigDecimal montoMensual) {
        this.montoMensual = ValidacionModelo.positivo(montoMensual, "El monto mensual");
    }

    // La baja logica evita eliminar datos historicos.
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    // El servicio selecciona los costos activos; esta operacion solo aplica la formula.
    public BigDecimal calcularMontoDiario(int diasOperacion) {
        ValidacionModelo.dias(diasOperacion);
        return montoMensual.divide(BigDecimal.valueOf(diasOperacion), 2, RoundingMode.HALF_UP);
    }
}
