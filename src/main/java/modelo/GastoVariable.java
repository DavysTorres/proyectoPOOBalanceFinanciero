package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Gasto que pertenece por completo al dia en que fue registrado. */
public class GastoVariable extends Gasto {
    // Reutiliza la validacion de la clase padre.
    public GastoVariable(int id, Usuario usuario, String concepto, BigDecimal monto, LocalDate fecha) {
        super(id, usuario, concepto, monto, fecha);
    }

    // Demuestra polimorfismo: la misma operacion tiene otra formula.
    @Override
    public BigDecimal calcularMontoDiario() { return getMonto(); }
}
