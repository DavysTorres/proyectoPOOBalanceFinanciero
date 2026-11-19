package servicio;

import modelo.BalanceDia;

/**
 * Contrato comun de los estados del semaforo, independiente de Swing.
 */
public interface EstadoNegocio {
    String getColor();

    String getTitulo();

    String getMensaje(BalanceDia balance);
}
