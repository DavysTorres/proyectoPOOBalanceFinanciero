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
            return "No hay margen positivo observado o faltan ventas para estimar la meta. Revise los datos registrados.";
        return "Las ventas cubren los costos registrados hasta ahora; no garantiza el resultado al cierre." + (balance.isPuntoEquilibrioEstimado() ? " Meta estimada si se mantiene el margen observado; puede cambiar con nuevas ventas y gastos." : "");
    }
}
