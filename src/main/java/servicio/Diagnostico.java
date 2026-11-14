package servicio;

import modelo.*;

import java.math.BigDecimal;

/**
 * Selecciona un estado mediante polimorfismo, sin logica de colores en las ventanas.
 */
public class Diagnostico {
    // No se confunde una meta imposible con una meta cero ya cubierta.
    public EstadoNegocio evaluar(BalanceDia balance, Negocio negocio) {
        if (!balance.isPuntoEquilibrioCalculable()) return new EstadoRojo();
        if (balance.getIngresos().signum() > 0 && balance.getResultado().signum() >= 0) return new EstadoVerde();
        if (balance.getPorcentajeAvance().compareTo(negocio.getUmbralAmarillo()) >= 0) return new EstadoAmarillo();
        return new EstadoRojo();
    }

    // Se distingue una proyeccion del ingreso real y del resultado contable.
    public String generarMensajeProyeccion(BalanceDia balance, double fraccionDelDia) {
        if (!balance.isPuntoEquilibrioCalculable()) return "No hay una meta calculable para proyectar.";
        if (fraccionDelDia == 0) return "La jornada aun no ha comenzado.";
        BigDecimal proyectado = new CalculadoraFinanciera().proyectarCierre(balance.getIngresos(), fraccionDelDia);
        return "Si mantiene este ritmo, los ingresos al cierre serian " + proyectado.toPlainString()
                + ". Es una estimacion, no una garantia.";
    }

    // Fraccion del horario transcurrido acotada entre cero y uno.
    public double fraccionDelDia(java.time.LocalTime ahora, Negocio negocio) {
        return util.FechaUtil.fraccionDelDia(ahora, negocio.getHoraApertura(), negocio.getHoraCierre());
    }
}
