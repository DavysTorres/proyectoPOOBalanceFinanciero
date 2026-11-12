package vista.contratos;

public interface VistaBase {
    void alAccionar(AccionVista accion, Runnable manejador);
    void mostrarError(String mensaje);
    void mostrarInformacion(String mensaje);
    void setOcupada(boolean ocupada);
}
