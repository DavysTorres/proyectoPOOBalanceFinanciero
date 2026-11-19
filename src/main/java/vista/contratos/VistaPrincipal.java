package vista.contratos;

import modelo.Usuario;
import servicio.*;

public interface VistaPrincipal extends VistaBase {
    default void iniciarCargaResumen() { }
    default void mostrarFalloResumen() { }
    default void mostrarBalanceDashboard(modelo.BalanceDia balance) { }
    void mostrarUsuario(Usuario usuario);
    void mostrarSemaforo(EstadoNegocio estado);
    default void mostrarResumenSemaforo(ResumenSemaforo resumen) {
        mostrarSemaforo(resumen.getEstado());
    }
}
