package modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/** Un gasto mensual que se distribuye entre los dias de operacion. */
public class GastoFijo extends Gasto {
    // Se almacena el divisor porque calcularMontoDiario no recibe parametros en la guia.
    private int diasOperacion;

    // Inicializa los datos heredados y el numero valido de dias.
    public GastoFijo(int id, Usuario usuario, String concepto, BigDecimal monto,
                     LocalDate fecha, int diasOperacion) {
        super(id, usuario, concepto, monto, fecha);
        setDiasOperacion(diasOperacion);
    }

    // Impide divisiones entre cero o meses con mas de 31 dias.
    public int getDiasOperacion() { return diasOperacion; }
    public final void setDiasOperacion(int diasOperacion) {
        this.diasOperacion = ValidacionModelo.dias(diasOperacion);
    }

    // Distribuye el monto mensual y redondea a dos decimales de moneda.
    @Override
    public BigDecimal calcularMontoDiario() {
        return getMonto().divide(BigDecimal.valueOf(diasOperacion), 2, RoundingMode.HALF_UP);
    }
}
