
package modelo;
// 1. Clase Padre Abstracta
public abstract class Gasto {
    protected String id;
    protected String usuario;
    protected String concepto;
    protected double monto;
    protected String fecha;

    public Gasto(String id, String usuario, String concepto, double monto, String fecha) {
        this.id = id;
        this.usuario = usuario;
        this.concepto = concepto;
        this.monto = monto;
        this.fecha = fecha;
    }

    // Método abstracto a implementar obligatoriamente por las clases hijas
    public abstract double calcularMontoDiario(int diasOperacion);
}