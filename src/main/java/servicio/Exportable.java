package servicio;

import modelo.BalanceDia;

/**
 * Contrato de exportacion; permite incorporar otros formatos sin cambiar el controlador.
 */
public interface Exportable {
    void exportar(BalanceDia balance, String ruta);
}
