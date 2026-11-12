package vista.contratos;
import modelo.*;
import java.util.List;
public interface VistaVenta extends VistaBase {
    String getFiltro();
    Producto getProductoSeleccionado();
    String getCantidad();
    void mostrarProductos(List<Producto> productos);
    void mostrarVenta(Venta venta);
    void mostrarVentaCobrada(Venta venta);
}
