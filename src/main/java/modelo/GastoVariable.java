
package modelo;
public class GastoVariable extends Gasto {
    public GastoVariable(String id, String usuario, String concepto, double monto, String fecha) {
        super(id, usuario, concepto, monto, fecha);
    }

    @Override
    public double calcularMontoDiario(int diasOperacion) {
        return this.monto; // Retorna el monto completo
    }
}
