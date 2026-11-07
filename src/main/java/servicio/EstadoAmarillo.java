package servicio;

import modelo.BalanceDia;

/**
 * Estado amarillo: implementacion concreta del contrato del semaforo.
 */
public final class EstadoAmarillo implements EstadoNegocio {
    // La vista interpreta el color; el servicio no importa java.awt ni construye componentes.
    public String getColor() {
        return "#F9A825";
    }

    public String getTitulo() {
        return "Cerca de la meta";
    }

    // Mensaje sin cifras para que tambien pueda utilizarlo el perfil cajero.
    public String getMensaje(BalanceDia balance) {
        if (!balance.isPuntoEquilibrioCalculable())
            return "No hay margen positivo para estimar la meta. Revise precios y costos.";
        return "Las ventas se acercan al punto de equilibrio." + (balance.isPuntoEquilibrioEstimado() ? " La meta es una estimacion del catalogo." : "");
    }
}
