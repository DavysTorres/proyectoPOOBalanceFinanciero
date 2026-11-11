package servicio;

import modelo.BalanceDia;

/**
 * Estado rojo: implementacion concreta del contrato del semaforo.
 */
public final class EstadoRojo implements EstadoNegocio {
    // La vista interpreta el color; el servicio no importa java.awt ni construye componentes.
    public String getColor() {
        return "#D32F2F";
    }

    public String getTitulo() {
        return "Costos pendientes";
    }

    // Mensaje sin cifras para que tambien pueda utilizarlo el perfil cajero.
    public String getMensaje(BalanceDia balance) {
        if (!balance.isPuntoEquilibrioCalculable())
            return "No hay margen positivo para estimar la meta. Revise precios y costos.";
        return "Aun no se han cubierto los costos del dia." + (balance.isPuntoEquilibrioEstimado() ? " La meta es una estimacion del catalogo." : "");
    }
}
