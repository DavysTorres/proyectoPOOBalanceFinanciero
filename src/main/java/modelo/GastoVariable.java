<<<<<<< HEAD

package modelo;
public class GastoVariable extends Gasto {
    public GastoVariable(String id, String usuario, String concepto, double monto, String fecha) {
        super(id, usuario, concepto, monto, fecha);
    }

    @Override
    public double calcularMontoDiario(int diasOperacion) {
        return this.monto; // Retorna el monto completo
=======
package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Gasto que pertenece por completo al dia en que fue registrado.
 */
public class GastoVariable extends Gasto {
    // Reutiliza la validacion de la clase padre.
    public GastoVariable(int id, Usuario usuario, String concepto, BigDecimal monto, LocalDate fecha) {
        super(id, usuario, concepto, monto, fecha);
    }

    // Demuestra polimorfismo: la misma operacion tiene otra formula.
    @Override
    public BigDecimal calcularMontoDiario() {
        return getMonto();
>>>>>>> 725c7fa98dbc60634746338e00a76164ed0f4cdf
    }
}
