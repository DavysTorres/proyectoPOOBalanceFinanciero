package vista.contratos;
import modelo.Producto;
import java.util.List;
public interface VistaProductos extends VistaBase {
    Integer getIdSeleccionado();
    String getNombre();
    String getPrecio();
    String getCosto();
    String getStock();
    String getStockMinimo();
    void mostrarProductos(List<Producto> productos);
    void mostrarProducto(Producto producto);
}
