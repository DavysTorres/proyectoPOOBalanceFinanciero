package servicio;

import modelo.BalanceDia;

/**
 * Estado verde: implementacion concreta del contrato del semaforo.
 */
public final class EstadoVerde implements EstadoNegocio {
    // La vista interpreta el color; el servicio no importa java.awt ni construye componentes.
    public String getColor() {
        return "#388E3C";
    }

    public String getTitulo() {
        return "Meta alcanzada";
    }

    // Mensaje sin cifras para que tambien pueda utilizarlo el perfil cajero.
    public String getMensaje(BalanceDia balance) {
        if (!balance.isPuntoEquilibrioCalculable()) return "No hay margen positivo para estimar una meta.";
        return balance.getResultado().signum() == 0
                ? "Estas en equilibrio: has cubierto los costos registrados, con ganancia cero."
                : "Tus ventas ya dejan ganancia despues de cubrir los costos registrados. El resultado puede cambiar durante el dia.";
    }
}
