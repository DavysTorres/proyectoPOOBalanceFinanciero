
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