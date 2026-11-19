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
        if (balance.getIngresos().signum() == 0) return "Sin ventas registradas todavia. El propietario puede planificar su objetivo con un margen previsto.";
        if (!balance.isPuntoEquilibrioCalculable()) return "No hay margen positivo: revisa precios, costo de los productos vendidos y gastos variables.";
        return "Aun no cubres los costos registrados. La estimacion cambia si cambian tus ventas o gastos.";
    }
}
