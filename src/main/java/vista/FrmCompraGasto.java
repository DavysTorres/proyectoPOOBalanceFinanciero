<<<<<<< HEAD

package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class FrmCompraGasto extends JFrame {

    // Componentes de la pestaña Compras
    private JTextField txtProveedor, txtProducto, txtCantidad, txtCostoUnitario;
    private JTable tblLineasCompra;
    private DefaultTableModel modelCompra;
    private JButton btnAgregarLinea, btnGuardarCompra;

    // Componentes de la pestaña Gastos
    private JTextField txtConceptoGasto, txtMontoGasto, txtFechaGasto;
    private JComboBox<String> cbTipoGasto;
    private JButton btnGuardarGasto;

    public FrmCompraGasto() {
        setTitle("Registro de Compras y Gastos (Jefe) - ZeroLoss");
        setSize(550, 520);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 13));

        // Pestaña 1: Registro de Compras
        JPanel panelCompras = new JPanel(new BorderLayout(10, 10));
        panelCompras.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formCompraPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        txtProveedor = new JTextField();
        txtProducto = new JTextField();
        txtCantidad = new JTextField();
        txtCostoUnitario = new JTextField();

        formCompraPanel.add(new JLabel("Proveedor:"));
        formCompraPanel.add(txtProveedor);
        formCompraPanel.add(new JLabel("Producto:"));
        formCompraPanel.add(txtProducto);
        formCompraPanel.add(new JLabel("Cantidad / Costo Unitario:"));

        JPanel subCantPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        subCantPanel.add(txtCantidad);
        subCantPanel.add(txtCostoUnitario);
        formCompraPanel.add(subCantPanel);

        btnAgregarLinea = new JButton("+ Agregar Línea");
        btnGuardarCompra = new JButton("💾 Guardar Compra Completa");

        modelCompra = new DefaultTableModel(new String[]{"Producto", "Cantidad", "Costo U.", "Subtotal"}, 0);
        tblLineasCompra = new JTable(modelCompra);
        JScrollPane scrollTabla = new JScrollPane(tblLineasCompra);

        JPanel botCompraPanel = new JPanel(new BorderLayout(5, 5));
        botCompraPanel.add(btnAgregarLinea, BorderLayout.NORTH);
        botCompraPanel.add(btnGuardarCompra, BorderLayout.SOUTH);

        panelCompras.add(formCompraPanel, BorderLayout.NORTH);
        panelCompras.add(scrollTabla, BorderLayout.CENTER);
        panelCompras.add(botCompraPanel, BorderLayout.SOUTH);

        // Pestaña 2: Registro de Gastos
        JPanel panelGastos = new JPanel(new GridLayout(5, 2, 10, 15));
        panelGastos.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        txtConceptoGasto = new JTextField();
        txtMontoGasto = new JTextField();
        txtFechaGasto = new JTextField("AAAA-MM-DD");
        cbTipoGasto = new JComboBox<>(new String[]{"Gasto Fijo", "Gasto Variable"});
        btnGuardarGasto = new JButton("💾 Guardar Gasto");

        panelGastos.add(new JLabel("Concepto del Gasto:"));
        panelGastos.add(txtConceptoGasto);
        panelGastos.add(new JLabel("Monto ($):"));
        panelGastos.add(txtMontoGasto);
        panelGastos.add(new JLabel("Tipo de Gasto:"));
        panelGastos.add(cbTipoGasto);
        panelGastos.add(new JLabel("Fecha:"));
        panelGastos.add(txtFechaGasto);
        panelGastos.add(new JLabel(""));
        panelGastos.add(btnGuardarGasto);

        tabbedPane.addTab("🛒 Registrar Compra", panelCompras);
        tabbedPane.addTab("💸 Registrar Gasto", panelGastos);

        add(tabbedPane);
    }
}
=======
package vista;

import modelo.*;
import util.FormatoMoneda;
import vista.contratos.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/** Compras y gastos conectados a CompraGastoControlador. */
public final class FrmCompraGasto extends FrmBase implements VistaCompraGasto {
    private final JTextField txtProveedor = new JTextField();
    private final JTextField txtProducto = new JTextField();
    private final JComboBox<Producto> cbProducto = new JComboBox<>();
    private final JTextField txtCantidad = new JTextField("1");
    private final JTextField txtCostoUnitario = new JTextField();
    private final DefaultTableModel modelCompra = EstiloZeroLoss.tabla("Producto", "Cantidad", "Costo U.", "Subtotal");
    private final JTable tblLineasCompra = new JTable(modelCompra);
    private final JLabel total = new JLabel("Total: —", SwingConstants.RIGHT);
    private final JTextField txtConceptoGasto = new JTextField();
    private final JTextField txtMontoGasto = new JTextField();
    private final JTextField txtFechaGasto = new JTextField(LocalDate.now().toString());
    private final JComboBox<String> cbTipoGasto = new JComboBox<>(new String[]{"Gasto Fijo", "Gasto Variable"});
    private final JTextField txtDiasOperacion = new JTextField("30");
    private final DefaultTableModel comprasDia = EstiloZeroLoss.tabla("Número", "Proveedor", "Fecha", "Total");
    private final DefaultTableModel gastosDia = EstiloZeroLoss.tabla("Número", "Concepto", "Fecha", "Monto");

    public FrmCompraGasto() {
        super("Registro de Compras y Gastos - ZeroLoss");
        JTabbedPane pestanas = new FormularioZeroLoss.Pestanas();
        pestanas.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        JPanel compras = new JPanel(new BorderLayout(10, 10));
        compras.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel formulario = new JPanel(new FormularioZeroLoss.Campos());
        EstiloZeroLoss.campo(formulario, "Proveedor:", txtProveedor);
        JPanel busqueda = new JPanel(new BorderLayout(5, 0));
        busqueda.add(txtProducto, BorderLayout.CENTER);
        JButton buscar = boton("Buscar", AccionVista.BUSCAR);
        busqueda.add(buscar, BorderLayout.EAST);
        txtProducto.addActionListener(e -> buscar.doClick());
        EstiloZeroLoss.campo(formulario, "Buscar producto:", busqueda);
        cbProducto.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                return super.getListCellRendererComponent(l, v instanceof Producto ? ((Producto)v).getNombre() : "Seleccione un producto", i, s, f);
            }
        });
        cbProducto.addActionListener(e -> {
            Producto p = getProductoSeleccionado();
            txtCostoUnitario.setText(p == null ? "" : p.getCostoCompra().toPlainString());
        });
        EstiloZeroLoss.campo(formulario, "Producto:", cbProducto);
        EstiloZeroLoss.campo(formulario, "Cantidad:", txtCantidad);
        EstiloZeroLoss.campo(formulario, "Costo unitario ($):", txtCostoUnitario);
        txtCostoUnitario.setToolTipText("Sin separadores de miles, por ejemplo 6000 o 6000,50");
        compras.add(formulario, BorderLayout.NORTH);
        tblLineasCompra.setFillsViewportHeight(true);
        compras.add(new JScrollPane(tblLineasCompra), BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout(5, 8));
        pie.add(total, BorderLayout.NORTH);
        JPanel acciones = new JPanel(new FormularioZeroLoss.Acciones(FlowLayout.RIGHT));
        acciones.add(boton("+ Agregar línea", AccionVista.AGREGAR));
        acciones.add(boton("Cancelar compra", AccionVista.CANCELAR));
        acciones.add(boton("Guardar compra completa", AccionVista.GUARDAR_COMPRA));
        pie.add(acciones, BorderLayout.SOUTH);
        compras.add(pie, BorderLayout.SOUTH);

        JPanel gastos = new JPanel(new BorderLayout(10, 15));
        gastos.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel campos = new JPanel(new FormularioZeroLoss.Campos());
        EstiloZeroLoss.campo(campos, "Concepto del gasto:", txtConceptoGasto);
        EstiloZeroLoss.campo(campos, "Monto ($):", txtMontoGasto);
        EstiloZeroLoss.campo(campos, "Tipo de gasto:", cbTipoGasto);
        EstiloZeroLoss.campo(campos, "Días de operación:", txtDiasOperacion);
        EstiloZeroLoss.campo(campos, "Fecha (día actual):", txtFechaGasto);
        txtFechaGasto.setEditable(false);
        txtFechaGasto.setToolTipText("El servicio asigna la fecha al guardar el gasto.");

        cbTipoGasto.addActionListener(e -> txtDiasOperacion.setEnabled(esGastoFijo() && !estaOcupada()));
        gastos.add(campos, BorderLayout.NORTH);
        JPanel pieGasto = new JPanel(new BorderLayout(0, 12));
        pieGasto.add(new JLabel("Los días de operación se utilizan para distribuir el gasto fijo."), BorderLayout.NORTH);
        JPanel accionesGasto = new JPanel(new FormularioZeroLoss.Acciones(FlowLayout.RIGHT));
        accionesGasto.add(boton("Guardar gasto", AccionVista.GUARDAR_GASTO));
        pieGasto.add(accionesGasto, BorderLayout.SOUTH);
        gastos.add(pieGasto, BorderLayout.SOUTH);

        JPanel registros = new JPanel(new BorderLayout(8, 8));
        JTabbedPane listados = new FormularioZeroLoss.Pestanas();
        listados.addTab("Compras del día", new JScrollPane(new JTable(comprasDia)));
        listados.addTab("Gastos del día", new JScrollPane(new JTable(gastosDia)));
        registros.add(listados, BorderLayout.CENTER);
        JPanel accionesRegistros = new JPanel(new FormularioZeroLoss.Acciones(FlowLayout.RIGHT));
        accionesRegistros.add(boton("Actualizar registros del día", AccionVista.LISTAR));
        registros.add(accionesRegistros, BorderLayout.SOUTH);
        pestanas.addTab("Registrar compra", compras);
        pestanas.addTab("Registrar gasto", gastos);
        pestanas.addTab("Registros del día", registros);
        setContentPane(pestanas);
        setMinimumSize(new Dimension(740, 560));
        setSize(920, 650);
        setLocationRelativeTo(null);
    }

    private JButton boton(String texto, AccionVista accion) {
        JButton boton = new JButton(texto);
        boton.setFocusPainted(false);
        registrarBoton(accion, boton);
        return boton;
    }

    @Override public String getFiltro() { return txtProducto.getText().trim(); }
    @Override public Producto getProductoSeleccionado() { return (Producto) cbProducto.getSelectedItem(); }
    @Override public String getCantidad() { return txtCantidad.getText().trim(); }
    @Override public String getCostoUnitario() { return txtCostoUnitario.getText().trim(); }
    @Override public String getProveedor() { return txtProveedor.getText().trim(); }
    @Override public String getConceptoGasto() { return txtConceptoGasto.getText().trim(); }
    @Override public String getMontoGasto() { return txtMontoGasto.getText().trim(); }
    @Override public boolean esGastoFijo() { return cbTipoGasto.getSelectedIndex() == 0; }
    @Override public String getDiasOperacion() { return txtDiasOperacion.getText().trim(); }

    @Override public void mostrarProductos(List<Producto> productos) {
        cbProducto.removeAllItems();
        for (Producto p : productos) cbProducto.addItem(p);
    }

    @Override public void mostrarCompra(Compra compra) {
        modelCompra.setRowCount(0);
        if (compra == null) {
            txtProveedor.setText("");
            total.setText("Total: —");
            return;
        }
        txtProveedor.setText(compra.getProveedor());
        for (DetalleCompra d : compra.getDetalles()) modelCompra.addRow(new Object[]{
                d.getProducto().getNombre(), d.getCantidad(), FormatoMoneda.mostrar(d.getCostoUnitario()),
                FormatoMoneda.mostrar(d.calcularSubtotal())});
        total.setText("Total: " + FormatoMoneda.mostrar(compra.getTotal()));
    }

    @Override public void mostrarCompras(List<Compra> compras) {
        comprasDia.setRowCount(0);
        for (Compra c : compras) comprasDia.addRow(new Object[]{c.getId(), c.getProveedor(), c.getFechaHora(), FormatoMoneda.mostrar(c.getTotal())});
    }

    @Override public void mostrarGastos(List<Gasto> gastos) {
        gastosDia.setRowCount(0);
        for (Gasto g : gastos) gastosDia.addRow(new Object[]{g.getId(), g.getConcepto(), g.getFecha(), FormatoMoneda.mostrar(g.getMonto())});
    }

    @Override public void setOcupada(boolean ocupada) {
        super.setOcupada(ocupada);
        for (JComponent campo : new JComponent[]{txtProveedor, txtProducto, cbProducto, txtCantidad,
                txtCostoUnitario, txtConceptoGasto, txtMontoGasto, cbTipoGasto}) campo.setEnabled(!ocupada);
        txtDiasOperacion.setEnabled(!ocupada && esGastoFijo());
        if (!ocupada) txtFechaGasto.setText(LocalDate.now().toString());
    }
}
>>>>>>> 725c7fa98dbc60634746338e00a76164ed0f4cdf
