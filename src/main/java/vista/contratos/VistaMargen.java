package vista.contratos;

import modelo.Producto;
import java.util.List;

public interface VistaMargen extends VistaBase {
    Producto getProductoSeleccionado();
    void mostrarRanking(List<Producto> productos);
    void mostrarConsejo(String consejo);
}
