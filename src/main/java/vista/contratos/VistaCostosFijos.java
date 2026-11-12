package vista.contratos;
import modelo.CostoFijo;
import java.util.List;
public interface VistaCostosFijos extends VistaBase {
    Integer getIdSeleccionado();
    String getConcepto();
    String getMontoMensual();
    boolean isActivo();
    void mostrarCostos(List<CostoFijo> costos);
}
