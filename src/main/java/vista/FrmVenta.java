package vista;

import modelo.*;
import util.FormatoMoneda;
import vista.contratos.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

/** Punto de venta: buscador compacto y una unica lista de los productos de la venta. */
public final class FrmVenta extends FrmModulo implements VistaVenta {
    private final JTextField filtro = texto("Buscar por nombre o c\u00f3digo:", "");
    private final JTextField cantidad = texto("Cantidad a agregar:", "1");
    private final JComboBox<Producto> sugerencias = new JComboBox<>();
    private final JLabel ayudaBusqueda = new JLabel("Escribe el nombre o c\u00f3digo del producto.");
    private final JTextField recibido = new JTextField(10);
    private final JLabel cambio = new JLabel("Pago exacto si dejas el campo vac\u00edo.");
    private final JLabel total = new JLabel("Total a cobrar: $ 0,00", SwingConstants.RIGHT);
    private final DefaultTableModel lineas = new DefaultTableModel(
            new String[]{"Producto", "Cantidad", "Precio unitario", "Importe", "Restar", "Sumar", "Eliminar"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return columna >= 4 && !estaOcupada(); }
    };
    private final JTable tablaCarrito = tabla(lineas);
    private final List<Producto> productos = new ArrayList<>();
    private List<DetalleVenta> detalles = List.of();
    private final JButton agregar;
    private final JButton cobrar;
    private final JButton cancelar;
    private final JButton nueva = new JButton("Nueva venta");
    private BigDecimal totalActual = BigDecimal.ZERO;
    private BigDecimal ultimoCambio;
    private BigDecimal cambioCobrado;
    private boolean ventaActiva;

    public FrmVenta() {
        super("Registrar venta", "Busca el producto, agr\u00e9galo y cobra. La venta comienza al agregar el primer producto.");
        JPanel selector = new JPanel(new BorderLayout(8, 0));
        selector.setOpaque(false);
        agregar = new JButton("Agregar a la venta");
        registrarBoton(AccionVista.AGREGAR, agregar);
        selector.add(sugerencias, BorderLayout.CENTER);
        selector.add(agregar, BorderLayout.EAST);
        JPanel busqueda = new JPanel(new BorderLayout(0, 12));
        busqueda.setOpaque(false);
        busqueda.add(campos, BorderLayout.NORTH);
        JPanel producto = new JPanel(new BorderLayout(0, 6));
        producto.setOpaque(false);
        JLabel etiquetaProducto = new JLabel("Producto encontrado:");
        etiquetaProducto.setLabelFor(sugerencias);
        producto.add(etiquetaProducto, BorderLayout.NORTH);
        producto.add(selector, BorderLayout.CENTER);
        busqueda.add(producto, BorderLayout.CENTER);
        cuerpo.add(busqueda, BorderLayout.NORTH);
        sugerencias.setMaximumRowCount(8);
        sugerencias.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object valor, int i, boolean s, boolean f) {
                Producto p = valor instanceof Producto ? (Producto)valor : null;
                String texto = p == null ? "Selecciona un producto de la b\u00fasqueda" : p.getNombre() + "  |  "
                        + FormatoMoneda.mostrar(p.getPrecioVenta());
                JLabel celda = (JLabel)super.getListCellRendererComponent(l, texto, i, s, f);
                celda.setToolTipText(texto);
                return celda;
            }
        });
        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        ayudaBusqueda.setForeground(TemaZeroLoss.SUAVE);
        centro.add(ayudaBusqueda, BorderLayout.NORTH);
        centro.add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);
        JPanel resumen = new JPanel(new BorderLayout(18, 10));
        resumen.setOpaque(false);
        JPanel efectivo = new JPanel(new FormularioZeroLoss.Acciones(FlowLayout.LEFT));
        efectivo.setOpaque(false);
        JLabel etiqueta = new JLabel("Efectivo recibido ($, opcional):");
        etiqueta.setLabelFor(recibido);
        efectivo.add(etiqueta); efectivo.add(recibido); efectivo.add(cambio);
        resumen.add(efectivo, BorderLayout.CENTER);
        total.setFont(new Font("Segoe UI", Font.BOLD, 26));
        total.setForeground(TemaZeroLoss.ACENTO);
        resumen.add(total, BorderLayout.NORTH);
        centro.add(resumen, BorderLayout.SOUTH);
        cuerpo.add(centro, BorderLayout.CENTER);
        registrarBoton(AccionVista.NUEVA_VENTA, nueva);
        registrarBoton(AccionVista.BUSCAR, new JButton("Buscar"));
        registrarBoton(AccionVista.AUMENTAR, new JButton("Sumar 1"));
        registrarBoton(AccionVista.REDUCIR, new JButton("Restar 1"));
        registrarBoton(AccionVista.QUITAR, new JButton("Eliminar producto"));
        cancelar = accion("Vaciar venta", AccionVista.CANCELAR);
        cobrar = accion("Cobrar", AccionVista.COBRAR);
        instalarAccionFila(4, "-1", AccionVista.REDUCIR);
        instalarAccionFila(5, "+1", AccionVista.AUMENTAR);
        instalarAccionFila(6, "Eliminar", AccionVista.QUITAR);
        filtro.getDocument().addDocumentListener(new DocumentListener() {
            private void actualizar() { filtrarProductos(); }
            public void insertUpdate(DocumentEvent e) { actualizar(); }
            public void removeUpdate(DocumentEvent e) { actualizar(); }
            public void changedUpdate(DocumentEvent e) { actualizar(); }
        });
        recibido.getDocument().addDocumentListener(new DocumentListener() {
            private void actualizar() { actualizarCambio(); }
            public void insertUpdate(DocumentEvent e) { actualizar(); }
            public void removeUpdate(DocumentEvent e) { actualizar(); }
            public void changedUpdate(DocumentEvent e) { actualizar(); }
        });
        sugerencias.addActionListener(e -> actualizarControles());
        filtro.addActionListener(e -> {
            if (getProductoSeleccionado() != null) accionar(AccionVista.AGREGAR);
            else if (sugerencias.getItemCount() > 1 && sugerencias.isShowing()) sugerencias.showPopup();
            else accionar(AccionVista.BUSCAR);
        });
        cantidad.addActionListener(e -> accionar(AccionVista.AGREGAR));
        atajo("F2", "Nueva venta", () -> { if (!estaOcupada()) filtro.requestFocusInWindow(); });
        atajo("F3", "Buscar", () -> { if (!estaOcupada()) { filtro.requestFocusInWindow(); filtro.selectAll(); } });
        atajo("F4", "Agregar", () -> accionar(AccionVista.AGREGAR));
        atajo("F8", "Cobrar", () -> accionar(AccionVista.COBRAR));
        estado.setText("Enter: agregar producto  |  +1 / -1 / Eliminar en cada fila  |  F8: cobrar");
        mostrarVenta(null);
        setSize(1100, 700);
    }
    private void instalarAccionFila(int columna, String texto, AccionVista accion) {
        TableColumn c = tablaCarrito.getColumnModel().getColumn(columna);
        JButton render = new JButton(texto);
        TemaZeroLoss.boton(render, TemaZeroLoss.TipoBoton.SECUNDARIO);
        render.setBorder(BorderFactory.createEmptyBorder(2, 7, 2, 7));
        c.setCellRenderer((tabla, valor, seleccionada, foco, fila, col) -> {
            render.setEnabled(!estaOcupada());
            return render;
        });
        class Editor extends AbstractCellEditor implements TableCellEditor {
            private final JButton boton = new JButton(texto);
            Editor() {
                TemaZeroLoss.boton(boton, TemaZeroLoss.TipoBoton.SECUNDARIO);
                boton.setBorder(BorderFactory.createEmptyBorder(2, 7, 2, 7));
                boton.addActionListener(e -> { fireEditingStopped(); accionar(accion); });
            }
            public Component getTableCellEditorComponent(JTable tabla, Object valor, boolean s, int fila, int col) {
                tabla.setRowSelectionInterval(fila, fila);
                return boton;
            }
            public Object getCellEditorValue() { return texto; }
        }
        c.setCellEditor(new Editor());
        if (tablaCarrito.getRowSorter() instanceof TableRowSorter<?> ordenador) ordenador.setSortable(columna, false);
        c.setPreferredWidth(columna == 6 ? 100 : 60);
        c.setMinWidth(columna == 6 ? 85 : 50);
    }
    private void atajo(String tecla, String nombre, Runnable accion) {
        cuerpo.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(tecla), nombre);
        cuerpo.getActionMap().put(nombre, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { accion.run(); }
        });
    }
    private static String normalizar(String texto) {
        return java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
    }
    private void filtrarProductos() {
        if (estaOcupada()) return;
        sugerencias.removeAllItems();
        String consulta = normalizar(getFiltro());
        if (consulta.isBlank()) { ayudaBusqueda.setText("Escribe el nombre o c\u00f3digo del siguiente producto."); actualizarControles(); return; }
        int coincidencias = 0, exacto = -1;
        for (Producto p : productos) if (p.isActivo() && (normalizar(p.getNombre()).contains(consulta) || Integer.toString(p.getId()).equals(consulta))) {
            if (coincidencias < 15) {
                if (normalizar(p.getNombre()).equals(consulta) || Integer.toString(p.getId()).equals(consulta)) exacto = coincidencias;
                sugerencias.addItem(p);
            }
            coincidencias++;
        }
        sugerencias.setSelectedIndex(exacto >= 0 ? exacto : coincidencias == 1 ? 0 : -1);
        ayudaBusqueda.setText(coincidencias == 0 ? "No se encontraron productos. Revisa el nombre o el c\u00f3digo."
                : coincidencias > 15 ? "Hay m\u00e1s de 15 coincidencias. Escribe un nombre m\u00e1s preciso."
                : "Selecciona el producto y pulsa Agregar o Enter. Puedes cambiar la cantidad antes de agregar.");
        actualizarControles();
    }
    private void actualizarControles() {
        boolean libre = !estaOcupada(), hayLineas = !detalles.isEmpty();
        if (agregar != null) agregar.setEnabled(libre && sugerencias.getSelectedItem() != null);
        if (cobrar != null) cobrar.setEnabled(libre && hayLineas && pagoSuficiente());
        if (cancelar != null) cancelar.setEnabled(libre && ventaActiva);
        nueva.setEnabled(libre && !ventaActiva);
        recibido.setEnabled(libre && hayLineas);
    }
    @Override public String getFiltro() { return leer(filtro); }
    @Override public String getCantidad() { return leer(cantidad); }
    @Override public String getEfectivoRecibido() { return leer(recibido); }
    @Override public Producto getProductoSeleccionado() { return (Producto)sugerencias.getSelectedItem(); }
    @Override public Producto getProductoEnCarrito() {
        int fila = tablaCarrito.getSelectedRow();
        return fila < 0 ? null : detalles.get(tablaCarrito.convertRowIndexToModel(fila)).getProducto();
    }
    @Override public void mostrarProductos(List<Producto> encontrados) {
        if (getFiltro().isBlank()) { productos.clear(); productos.addAll(encontrados); }
        else for (Producto p : encontrados) {
            productos.removeIf(anterior -> anterior.getId() == p.getId()); productos.add(p);
        }
        // La consulta termina antes de habilitar editores; filtrar en setOcupada(false).
        if (!estaOcupada()) filtrarProductos();
    }
    @Override public void mostrarVenta(Venta venta) {
        Producto seleccionado = getProductoEnCarrito();
        int filaAnterior = tablaCarrito.getSelectedRow();
        if (venta == null && ventaActiva) cambioCobrado = ultimoCambio;
        ventaActiva = venta != null;
        detalles = venta == null ? List.of() : new ArrayList<>(venta.getDetalles());
        lineas.setRowCount(0);
        for (DetalleVenta d : detalles) lineas.addRow(new Object[]{d.getProducto().getNombre(), d.getCantidad(),
                FormatoMoneda.mostrar(d.getPrecioUnitario()), FormatoMoneda.mostrar(d.calcularSubtotal()), "-1", "+1", "Eliminar"});
        totalActual = venta == null ? BigDecimal.ZERO : venta.getTotal();
        total.setText("Total a cobrar: " + FormatoMoneda.mostrar(totalActual));
        if (detalles.isEmpty()) recibido.setText("");
        if (!detalles.isEmpty()) {
            int fila = Math.max(0, Math.min(filaAnterior, detalles.size() - 1));
            if (seleccionado != null) for (int i = 0; i < detalles.size(); i++) if (detalles.get(i).getProducto().getId() == seleccionado.getId()) fila = i;
            tablaCarrito.setRowSelectionInterval(tablaCarrito.convertRowIndexToView(fila), tablaCarrito.convertRowIndexToView(fila));
        }
        estado.setText(detalles.isEmpty() ? "Agrega el primer producto para empezar. Enter: agregar  |  F8: cobrar"
                : detalles.size() + " productos en esta venta. Usa +1, -1 o Eliminar en la fila correspondiente.");
        actualizarCambio(); actualizarControles();
    }
    private boolean pagoSuficiente() {
        if (getEfectivoRecibido().isBlank()) return true;
        try {
            return getEfectivoRecibido().matches("[0-9]+([.,][0-9]+)?")
                    && new BigDecimal(getEfectivoRecibido().replace(',', '.')).compareTo(totalActual) >= 0;
        } catch (NumberFormatException e) { return false; }
    }
    private void actualizarCambio() {
        ultimoCambio = null;
        if (recibido.getText().isBlank()) { cambio.setText("Pago exacto si dejas el campo vac\u00edo."); actualizarControles(); return; }
        try {
            if (!getEfectivoRecibido().matches("[0-9]+([.,][0-9]+)?")) throw new NumberFormatException();
            BigDecimal efectivo = new BigDecimal(getEfectivoRecibido().replace(',', '.'));
            BigDecimal diferencia = efectivo.subtract(totalActual);
            if (diferencia.signum() < 0) cambio.setText("Faltan: " + FormatoMoneda.mostrar(diferencia.negate()));
            else { ultimoCambio = diferencia; cambio.setText("Cambio: " + FormatoMoneda.mostrar(diferencia)); }
        } catch (NumberFormatException e) { cambio.setText("Escribe el efectivo sin separadores de miles."); }
        actualizarControles();
    }
    @Override public void productoAgregado() {
        filtro.setText(""); cantidad.setText("1");
        SwingUtilities.invokeLater(() -> { if (isDisplayable() || filtro.isShowing()) filtro.requestFocusInWindow(); });
    }
    @Override public void mostrarVentaCobrada(Venta venta) {
        estado.setText("Venta #" + venta.getId() + " cobrada: " + FormatoMoneda.mostrar(venta.getTotal()) + (cambioCobrado == null ? ". Pago exacto." : ". Cambio a devolver: " + FormatoMoneda.mostrar(cambioCobrado))
                + " Agrega otro producto para la siguiente venta.");
    }
    @Override public void setOcupada(boolean valor) {
        super.setOcupada(valor);
        sugerencias.setEnabled(!valor);
        tablaCarrito.setEnabled(!valor);
        if (!valor) filtrarProductos();
        actualizarControles();
    }
}
