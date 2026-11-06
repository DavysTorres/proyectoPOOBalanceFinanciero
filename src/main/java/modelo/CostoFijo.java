
package modelo;
public class CostoFijo {
    private String id;
    private String concepto;
    private double montoMensual;
    private boolean activo;

    public CostoFijo(String id, String concepto, double montoMensual, boolean activo) {
        this.id = id;
        this.concepto = concepto;
        this.montoMensual = montoMensual;
        this.activo = activo;
    }

    public double calcularMontoDiario(int diasOperacion) {
        return this.montoMensual / diasOperacion;
    }
}
