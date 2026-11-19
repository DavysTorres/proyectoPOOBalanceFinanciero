package vista.contratos;

import modelo.*;
import java.util.List;

public interface VistaCompraGasto extends VistaBase {
    String getFiltro();
    Producto getProductoSeleccionado();
    String getCantidad();
    String getCostoUnitario();
    String getProveedor();
    String getConceptoGasto();
    String getMontoGasto();
    boolean esGastoFijo();
    String getDiasOperacion();
    void mostrarProductos(List<Producto> productos);
    void mostrarCompra(Compra compra);
    void mostrarCompras(List<Compra> compras);
    void mostrarGastos(List<Gasto> gastos);
}
