package vista.contratos;
import modelo.Venta;
public interface VistaModificarVenta extends VistaBase {
    String getVentaId();
    String getNuevoTotal();
    String getMotivo();
    void mostrarVenta(Venta venta);
}
