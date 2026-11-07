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
        if (!balance.isPuntoEquilibrioCalculable())
            return "No hay margen positivo para estimar la meta. Revise precios y costos.";
        return "Los ingresos ya cubren los costos del dia." + (balance.isPuntoEquilibrioEstimado() ? " La meta es una estimacion del catalogo." : "");
    }
}
