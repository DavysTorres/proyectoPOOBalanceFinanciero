package vista;

import modelo.*;
import util.FormatoMoneda;
import vista.contratos.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

/** Catálogo y carrito disponibles para los tres roles que registran ventas. */
public final class FrmVenta extends FrmModulo implements VistaVenta {
    private final JTextField filtro = texto("Buscar producto:", "");
    private final JTextField cantidad = texto("Cantidad a agregar:", "1");
    private final DefaultTableModel catalogo = EstiloZeroLoss.tabla("Código", "Producto", "Precio", "Stock");
    private final DefaultTableModel lineas = EstiloZeroLoss.tabla("Código", "Producto", "Cantidad", "Precio U.", "Subtotal");
    private final JTable tablaProductos = tabla(catalogo);
    private final JTable tablaCarrito = tabla(lineas);
    private final JTabbedPane pestanas = new JTabbedPane();
    private List<Producto> productos = List.of();
    private List<DetalleVenta> detalles = List.of();
    private final JLabel total = new JLabel("Total: —", SwingConstants.RIGHT);

    public FrmVenta() {
        super("Registrar venta", "Seleccione productos del catálogo y revise el carrito antes de cobrar.");
        pestanas.addTab("Catálogo", new JScrollPane(tablaProductos));
        pestanas.addTab("Carrito", new JScrollPane(tablaCarrito));
        JPanel centro = new JPanel(new BorderLayout(10, 10));
        centro.setOpaque(false);
        centro.add(pestanas, BorderLayout.CENTER);
        total.setFont(new Font("Segoe UI", Font.BOLD, 20));
        centro.add(total, BorderLayout.SOUTH);
        cuerpo.add(centro, BorderLayout.CENTER);
        accion("Nueva venta", AccionVista.NUEVA_VENTA);
        JButton buscar = accion("Buscar", AccionVista.BUSCAR);
        accion("Agregar", AccionVista.AGREGAR);
        accion("Quitar", AccionVista.QUITAR);
        accion("Reducir 1", AccionVista.REDUCIR);
        accion("Cobrar", AccionVista.COBRAR);
        accion("Cancelar", AccionVista.CANCELAR);
        filtro.addActionListener(e -> buscar.doClick());
        estado.setText("Pulse Nueva venta para iniciar. Quitar y Reducir utilizan la fila seleccionada del carrito.");
        setSize(1050, 650);
    }
    @Override public String getFiltro() { return leer(filtro); }
    @Override public String getCantidad() { return leer(cantidad); }
    @Override public Producto getProductoSeleccionado() {
        JTable tabla = pestanas.getSelectedIndex() == 0 ? tablaProductos : tablaCarrito;
        int fila = tabla.getSelectedRow();
        if (fila < 0) return null;
        int indice = tabla.convertRowIndexToModel(fila);
        return pestanas.getSelectedIndex() == 0 ? productos.get(indice) : detalles.get(indice).getProducto();
    }
    @Override public void mostrarProductos(List<Producto> productos) {
        this.productos = new ArrayList<>(productos);
        catalogo.setRowCount(0);
        for (Producto p : productos) catalogo.addRow(new Object[]{p.getId(), p.getNombre(), FormatoMoneda.mostrar(p.getPrecioVenta()), p.getStock()});
    }
    @Override public void mostrarVenta(Venta venta) {
        detalles = venta == null ? List.of() : new ArrayList<>(venta.getDetalles());
        lineas.setRowCount(0);
        for (DetalleVenta d : detalles) lineas.addRow(new Object[]{d.getProducto().getId(), d.getProducto().getNombre(), d.getCantidad(),
                FormatoMoneda.mostrar(d.getPrecioUnitario()), FormatoMoneda.mostrar(d.calcularSubtotal())});
        total.setText("Total: " + (venta == null ? "—" : FormatoMoneda.mostrar(venta.getTotal())));
        estado.setText(venta == null ? "No hay una venta en curso. Pulse Nueva venta."
                : "Venta en curso · " + detalles.size() + " productos en el carrito.");
    }
    @Override public void mostrarVentaCobrada(Venta venta) {
        estado.setText("Venta #" + venta.getId() + " cobrada · " + FormatoMoneda.mostrar(venta.getTotal()));
    }
}
