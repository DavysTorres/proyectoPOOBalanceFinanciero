package vista.contratos;
import modelo.RegistroCambio;
import java.util.List;
public interface VistaHistorial extends VistaBase {
    String getFecha();
    void mostrarRegistros(List<RegistroCambio> registros);
}
