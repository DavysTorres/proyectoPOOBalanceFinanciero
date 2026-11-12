package vista;

import modelo.Producto;
import util.FormatoMoneda;
import vista.contratos.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public final class FrmProductos extends FrmModulo implements VistaProductos {
    private final JTextField nombre = texto("Nombre:", "");
    private final JTextField precio = texto("Precio de venta ($):", "");
    private final JTextField costo = texto("Costo de compra ($):", "");
    private final JTextField stock = texto("Stock inicial / actual:", "0");
    private final JTextField minimo = texto("Stock mínimo:", "0");
    private final DefaultTableModel modelo = EstiloZeroLoss.tabla("Código", "Producto", "Precio", "Costo", "Stock", "Mínimo", "Activo");
    private final JTable tabla = tabla(modelo);
    private List<Producto> productos = List.of();
    private Integer seleccionado;
    public FrmProductos() {
        super("Catálogo de productos", "Administrador · Los cambios de inventario se registran con compras y ventas.");
        cuerpo.add(new JScrollPane(tabla), BorderLayout.CENTER);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) mostrarProducto(productos.get(tabla.convertRowIndexToModel(tabla.getSelectedRow())));
        });
        local("Limpiar", this::limpiar);
        accion("Crear", AccionVista.CREAR);
        accion("Guardar cambios", AccionVista.EDITAR);
        accion("Desactivar", AccionVista.DESACTIVAR);
        accion("Actualizar lista", AccionVista.LISTAR);
        estado.setText("Seleccione una fila para editar. El stock inicial solo se introduce al crear.");
    }
    private void limpiar() {
        tabla.clearSelection(); seleccionado = null;
        nombre.setText(""); precio.setText(""); costo.setText(""); stock.setText("0"); minimo.setText("0");
        stock.setEditable(true);
    }
    @Override public Integer getIdSeleccionado() { return seleccionado; }
    @Override public String getNombre() { return leer(nombre); }
    @Override public String getPrecio() { return leer(precio); }
    @Override public String getCosto() { return leer(costo); }
    @Override public String getStock() { return leer(stock); }
    @Override public String getStockMinimo() { return leer(minimo); }
    @Override public void mostrarProductos(List<Producto> lista) {
        productos = new ArrayList<>(lista); modelo.setRowCount(0); limpiar();
        for (Producto p : lista) modelo.addRow(new Object[]{p.getId(), p.getNombre(), FormatoMoneda.mostrar(p.getPrecioVenta()),
                FormatoMoneda.mostrar(p.getCostoCompra()), p.getStock(), p.getStockMinimo(), p.isActivo() ? "Sí" : "No"});
    }
    @Override public void mostrarProducto(Producto p) {
        if (p == null) { limpiar(); return; }
        seleccionado = p.getId();
        nombre.setText(p.getNombre()); precio.setText(p.getPrecioVenta().toPlainString()); costo.setText(p.getCostoCompra().toPlainString());
        stock.setText(Integer.toString(p.getStock())); minimo.setText(Integer.toString(p.getStockMinimo()));
        stock.setEditable(false);
        estado.setText("Producto #" + p.getId() + " · " + (p.isActivo() ? "Activo" : "Inactivo") + ". Actualice la lista para consultar cambios.");
    }
}
