package vista.contratos;
import modelo.*;
import java.util.List;
public interface VistaUsuarios extends VistaBase {
    Integer getIdSeleccionado();
    String getNombre();
    String getUsername();
    char[] getClave();
    Rol getRol();
    boolean isActivo();
    void limpiarClave();
    void mostrarUsuarios(List<Usuario> usuarios);
}
