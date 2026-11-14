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
            return "No hay margen positivo observado o faltan ventas para estimar la meta. Revise los datos registrados.";
        return "Las ventas se acercan al punto de equilibrio." + (balance.isPuntoEquilibrioEstimado() ? " Meta estimada si se mantiene el margen observado; puede cambiar con nuevas ventas y gastos." : "");
    }
}
