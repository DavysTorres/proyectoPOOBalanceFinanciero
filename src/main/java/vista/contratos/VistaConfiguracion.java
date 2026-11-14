package vista.contratos;
import modelo.Negocio;
public interface VistaConfiguracion extends VistaBase {
    String getNombre();
    String getDiasOperacion();
    String getHoraApertura();
    String getHoraCierre();
    String getUmbralAmarillo();
    String getUmbralMargenBajo();
    void mostrarNegocio(Negocio negocio);
}
