package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

public class FrmVenta extends JFrame {
    private static final Color FONDO = new Color(247, 247, 247);
    private static final Color PANEL = new Color(238, 238, 238);
    private static final Color TEXTO = new Color(45, 45, 45);
    private static final Color VERDE = new Color(45, 195, 91);
    private static final Color AMARILLO = new Color(255, 207, 0);
    private static final NumberFormat MONEDA =
            NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    private final JTextField txtFactura = new JTextField("XXXXXX", 10);
    private final JTextField txtBuscar = new JTextField();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Producto", "Precio", "Acción"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    private final JTable tabla = new JTable(modelo);

    private final DefaultTableModel detalleModelo = new DefaultTableModel(
            new Object[]{"Producto", "Cant.", "Subtotal", "Acción"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    private final JTable tablaDetalle = new JTable(detalleModelo);
    private final JLabel lblTotal = new JLabel("TOTAL: $0");
    private int siguienteFactura = 1043;

    public FrmVenta() {
        super("Nueva venta");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(760, 610));
        setSize(900, 700);
        setLocationRelativeTo(null);

        getContentPane().setBackground(FONDO);
        setLayout(new BorderLayout(14, 12));
        ((JPanel) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(18, 22, 20, 22));

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearContenido(), BorderLayout.CENTER);
        add(crearPie(), BorderLayout.SOUTH);

        cargarProductos();
        actualizarTotal();
    }

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout(8, 5));
        panel.setBackground(FONDO);

        JLabel titulo = new JLabel("Factura N°:");
        titulo.setFont(new Font("Serif", Font.BOLD, 22));
        titulo.setForeground(TEXTO);

        txtFactura.setText(String.valueOf(siguienteFactura));
        txtFactura.setEditable(false);
        txtFactura.setFont(new Font("Serif", Font.PLAIN, 19));
        txtFactura.setForeground(new Color(155, 155, 155));
        txtFactura.setBorder(BorderFactory.createEmptyBorder(5, 4, 5, 4));
        txtFactura.setBackground(FONDO);

        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.add(titulo, BorderLayout.WEST);
        fila.add(txtFactura, BorderLayout.CENTER);

        panel.add(fila, BorderLayout.CENTER);
        panel.add(new LineaSeparadora(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearContenido() {
        JPanel contenido = new JPanel(new GridBagLayout());
        contenido.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 10, 0);
        contenido.add(crearBuscador(), c);

        c.gridy = 1;
        c.weighty = 0.43;
        c.fill = GridBagConstraints.BOTH;
        contenido.add(crearListaProductos(), c);

        c.gridy = 2;
        c.weighty = 0;
        c.insets = new Insets(12, 0, 8, 0);
        contenido.add(new LineaSeparadora(), c);

        c.gridy = 3;
        c.weighty = 0.57;
        c.insets = new Insets(0, 0, 0, 0);
        contenido.add(crearDetalle(), c);

        return contenido;
    }

    private JPanel crearBuscador() {
        JPanel panel = new JPanel(new BorderLayout(9, 0));
        panel.setOpaque(false);

        JLabel lupa = new JLabel("⌕", SwingConstants.CENTER);
        lupa.setFont(new Font("SansSerif", Font.BOLD, 22));
        lupa.setForeground(new Color(72, 102, 140));
        lupa.setPreferredSize(new Dimension(32, 34));

        txtBuscar.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtBuscar.setToolTipText("Escribe el nombre del producto");
        txtBuscar.addActionListener(e -> filtrarProductos());

        JButton buscar = crearBoton("Buscar producto",
                new Color(235, 235, 235), TEXTO);
        buscar.addActionListener(e -> filtrarProductos());

        panel.add(lupa, BorderLayout.WEST);
        panel.add(txtBuscar, BorderLayout.CENTER);
        panel.add(buscar, BorderLayout.EAST);

        return panel;
    }

    private JPanel crearListaProductos() {
        JPanel panel = crearTarjeta(new BorderLayout());

        configurarTabla(tabla);
        tabla.setRowHeight(30);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PANEL);
        panel.add(scroll, BorderLayout.CENTER);

        JButton agregar = crearBoton(
                "＋  Agregar producto seleccionado",
                new Color(225, 225, 225), TEXTO);
        agregar.addActionListener(e -> agregarSeleccionado());

        JPanel acciones = new JPanel(new BorderLayout());
        acciones.setOpaque(false);
        acciones.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        acciones.add(agregar, BorderLayout.EAST);
        panel.add(acciones, BorderLayout.SOUTH);

        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    agregarSeleccionado();
                }
            }
        });

        return panel;
    }

    private JPanel crearDetalle() {
        JPanel panel = crearTarjeta(new BorderLayout(10, 8));

        JLabel titulo = new JLabel("Detalle de la venta:");
        titulo.setFont(new Font("Serif", Font.BOLD, 20));
        panel.add(titulo, BorderLayout.NORTH);

        configurarTabla(tablaDetalle);
        tablaDetalle.setRowHeight(30);

        JScrollPane scroll = new JScrollPane(tablaDetalle);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PANEL);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel sur = new JPanel(new BorderLayout());
        sur.setOpaque(false);

        JButton quitar = crearBoton("Quitar producto seleccionado",
                new Color(225, 225, 225), TEXTO);
        quitar.addActionListener(e -> quitarSeleccionado());

        lblTotal.setFont(new Font("Serif", Font.BOLD, 17));
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);

        sur.add(quitar, BorderLayout.WEST);
        sur.add(lblTotal, BorderLayout.EAST);
        panel.add(sur, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPie() {
        JPanel pie = new JPanel(new BorderLayout(14, 0));
        pie.setOpaque(false);

        JButton guardar = crearBoton("En proceso\n(Guardar)", AMARILLO, TEXTO);
        JButton cobrar = crearBoton("Cobrar\n(Finalizar)", VERDE, Color.WHITE);

        guardar.setPreferredSize(new Dimension(155, 54));
        cobrar.setPreferredSize(new Dimension(155, 54));

        guardar.addActionListener(e -> guardarEnProceso());
        cobrar.addActionListener(e -> cobrar());

        JLabel ayuda = new JLabel(
                "Guarda la venta pendiente o registra el cobro.");
        ayuda.setForeground(new Color(105, 105, 105));

        pie.add(ayuda, BorderLayout.CENTER);
        pie.add(guardar, BorderLayout.EAST);
        pie.add(cobrar, BorderLayout.WEST);

        return pie;
    }

    private void cargarProductos() {
        modelo.setRowCount(0);

        String[][] productos = {
            {"Arroz 1 Kg", "3500"},
            {"Pan tajado", "5000"},
            {"Leche 1 L", "3500"},
            {"Gaseosa", "4200"},
            {"Aceite 1 L", "8900"}
        };

        String filtro = txtBuscar.getText()
                .trim().toLowerCase(new Locale("es", "CO"));

        for (String[] producto : productos) {
            String nombre = producto[0];
            String nombreMinuscula =
                    nombre.toLowerCase(new Locale("es", "CO"));

            if (nombreMinuscula.contains(filtro)) {
                modelo.addRow(new Object[]{
                    nombre,
                    MONEDA.format(Integer.parseInt(producto[1])),
                    "( + )"
                });
            }
        }
    }

    private void filtrarProductos() {
        cargarProductos();
    }

    private void agregarSeleccionado() {
        int fila = tabla.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona un producto de la lista.",
                    "Agregar producto",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String nombre = String.valueOf(modelo.getValueAt(fila, 0));
        int precio = precioDe(nombre);

        for (int i = 0; i < detalleModelo.getRowCount(); i++) {
            if (nombre.equals(detalleModelo.getValueAt(i, 0))) {
                int cantidad = Integer.parseInt(
                        String.valueOf(detalleModelo.getValueAt(i, 1))) + 1;

                detalleModelo.setValueAt(String.valueOf(cantidad), i, 1);
                detalleModelo.setValueAt(
                        MONEDA.format(precio * cantidad), i, 2);
                actualizarTotal();
                return;
            }
        }

        detalleModelo.addRow(new Object[]{
            nombre, "1", MONEDA.format(precio), "( - )"
        });

        actualizarTotal();
    }

    private int precioDe(String nombre) {
        switch (nombre) {
            case "Arroz 1 Kg":
                return 3500;
            case "Pan tajado":
                return 5000;
            case "Leche 1 L":
                return 3500;
            case "Gaseosa":
                return 4200;
            case "Aceite 1 L":
                return 8900;
            default:
                return 0;
        }
    }

    private void quitarSeleccionado() {
        int fila = tablaDetalle.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecciona un producto del detalle.",
                    "Quitar producto",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        detalleModelo.removeRow(fila);
        actualizarTotal();
    }

    private void actualizarTotal() {
        int total = 0;

        for (int i = 0; i < detalleModelo.getRowCount(); i++) {
            String producto = String.valueOf(detalleModelo.getValueAt(i, 0));
            int cantidad = Integer.parseInt(
                    String.valueOf(detalleModelo.getValueAt(i, 1)));

            total += precioDe(producto) * cantidad;
        }

        lblTotal.setText("TOTAL: " + MONEDA.format(total));
    }

    private void guardarEnProceso() {
        if (detalleModelo.getRowCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Agrega al menos un producto antes de guardar la venta.",
                    "Venta vacía",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "La venta " + txtFactura.getText()
                        + " quedó en proceso.\nTotal: " + lblTotal.getText(),
                "Venta guardada",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void cobrar() {
        if (detalleModelo.getRowCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Agrega al menos un producto antes de cobrar.",
                    "Venta vacía",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String total = lblTotal.getText().replace("TOTAL: ", "");

        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Confirmas el cobro por " + total + "?",
                "Finalizar venta",
                JOptionPane.YES_NO_OPTION);

        if (respuesta == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(
                    this,
                    "Venta " + txtFactura.getText()
                            + " cobrada correctamente.",
                    "Cobro finalizado",
                    JOptionPane.INFORMATION_MESSAGE);

            detalleModelo.setRowCount(0);
            txtFactura.setText(String.valueOf(++siguienteFactura));
            actualizarTotal();
        }
    }

    private static JPanel crearTarjeta(
            java.awt.LayoutManager distribucion) {
        JPanel panel = new JPanel(distribucion);
        panel.setBackground(PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        return panel;
    }

    private static void configurarTabla(JTable tabla) {
        tabla.setFont(new Font("Serif", Font.PLAIN, 15));
        tabla.setForeground(TEXTO);
        tabla.setBackground(PANEL);
        tabla.setGridColor(new Color(220, 220, 220));
        tabla.setShowVerticalLines(false);
        tabla.setSelectionBackground(new Color(255, 239, 160));
        tabla.setSelectionForeground(TEXTO);
        tabla.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(new Color(232, 232, 232));
        tabla.getTableHeader().setForeground(TEXTO);
    }

    private static JButton crearBoton(
            String texto, Color fondo, Color frente) {
        String textoHtml = texto.replace("\n", "<br>");

        JButton boton = new JButton(
                "<html><center>" + textoHtml + "</center></html>");

        boton.setFont(new Font("Serif", Font.BOLD, 14));
        boton.setBackground(fondo);
        boton.setForeground(frente);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setOpaque(true);

        return boton;
    }

    private static class LineaSeparadora extends JPanel {
        LineaSeparadora() {
            setPreferredSize(new Dimension(1, 2));
            setBackground(new Color(65, 65, 65));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Usa el estilo predeterminado si no se puede cambiar.
            }

            new FrmVenta().setVisible(true);
        });
    }
}