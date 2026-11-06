
package modelo;
public class GastoFijo extends Gasto {
    public GastoFijo(String id, String usuario, String concepto, double monto, String fecha) {
        super(id, usuario, concepto, monto, fecha);
    }

    @Override
    public double calcularMontoDiario(int diasOperacion) {
        return this.monto / diasOperacion;
    }
}
