package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

public class FrmCierreCaja extends JFrame {

    private static final Color FONDO = new Color(247, 247, 247);
    private static final Color PANEL = new Color(238, 238, 238);
    private static final Color TEXTO = new Color(45, 45, 45);
    private static final Color VERDE = new Color(45, 195, 91);
    private static final Color ROJO = new Color(255, 60, 65);
    private static final NumberFormat MONEDA =
            NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    private final JLabel lblFecha = new JLabel();
    private final JLabel lblVentas = new JLabel();
    private final JLabel lblGastos = new JLabel();
    private final JLabel lblEfectivo = new JLabel();
    private final JLabel lblEstado = new JLabel("● Caja abierta");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Hora", "Movimiento", "Tipo", "Valor"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    public FrmCierreCaja() {
        super("Cierre de caja");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(700, 560));
        setSize(820, 650);
        setLocationRelativeTo(null);

        getContentPane().setBackground(FONDO);
        setLayout(new BorderLayout(14, 14));
        ((JPanel) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(20, 24, 22, 24));

        lblFecha.setText(java.time.LocalDate.now().toString());
        cargarMovimientos();

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearMovimientos(), BorderLayout.CENTER);
        add(crearResumen(), BorderLayout.SOUTH);

        calcularResumen();
    }

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titulo = new JLabel("Cierre de caja");
        titulo.setFont(new Font("Serif", Font.BOLD, 26));
        titulo.setForeground(TEXTO);

        JLabel fecha = new JLabel("Día: " + lblFecha.getText());
        fecha.setFont(new Font("Serif", Font.PLAIN, 17));
        fecha.setForeground(new Color(100, 100, 100));

        panel.add(titulo, BorderLayout.WEST);
        panel.add(fecha, BorderLayout.EAST);
        return panel;
    }

    private JPanel crearMovimientos() {
        JPanel panel = tarjeta(new BorderLayout(10, 10));

        JLabel titulo = new JLabel("Movimientos del día");
        titulo.setFont(new Font("Serif", Font.BOLD, 20));
        panel.add(titulo, BorderLayout.NORTH);

        JTable tabla = new JTable(modelo);
        tabla.setFont(new Font("Serif", Font.PLAIN, 15));
        tabla.setRowHeight(30);
        tabla.setBackground(PANEL);
        tabla.setForeground(TEXTO);
        tabla.setGridColor(new Color(220, 220, 220));
        tabla.setShowVerticalLines(false);
        tabla.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(new Color(232, 232, 232));

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PANEL);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearResumen() {
        JPanel panel = tarjeta(new BorderLayout(12, 12));

        JLabel titulo = new JLabel("Resumen del día");
        titulo.setFont(new Font("Serif", Font.BOLD, 20));
        panel.add(titulo, BorderLayout.NORTH);

        JPanel datos = new JPanel(new GridLayout(3, 2, 12, 8));
        datos.setOpaque(false);

        datos.add(etiqueta("Ingresos por ventas:"));
        datos.add(lblVentas);
        datos.add(etiqueta("Gastos registrados:"));
        datos.add(lblGastos);
        datos.add(etiqueta("Efectivo esperado en caja:"));
        datos.add(lblEfectivo);

        for (JLabel valor : new JLabel[]{lblVentas, lblGastos, lblEfectivo}) {
            valor.setFont(new Font("Serif", Font.BOLD, 16));
            valor.setHorizontalAlignment(SwingConstants.RIGHT);
        }

        panel.add(datos, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);

        lblEstado.setFont(new Font("Serif", Font.BOLD, 15));
        lblEstado.setForeground(VERDE);

        JButton cerrar = boton("Cerrar caja", ROJO, Color.WHITE);
        cerrar.setPreferredSize(new Dimension(170, 48));
        cerrar.addActionListener(e -> cerrarCaja());

        pie.add(lblEstado, BorderLayout.WEST);
        pie.add(cerrar, BorderLayout.EAST);
        panel.add(pie, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel etiqueta(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Serif", Font.PLAIN, 16));
        label.setForeground(TEXTO);
        return label;
    }

    private void cargarMovimientos() {
        modelo.addRow(new Object[]{
            "09:15", "Venta #1042", "Ingreso", MONEDA.format(11000)
        });
        modelo.addRow(new Object[]{
            "10:30", "Compra de bolsas", "Gasto", MONEDA.format(5000)
        });
        modelo.addRow(new Object[]{
            "11:05", "Venta #1043", "Ingreso", MONEDA.format(18500)
        });
        modelo.addRow(new Object[]{
            "12:20", "Pago de transporte", "Gasto", MONEDA.format(3500)
        });
    }

    private void calcularResumen() {
        int ventas = 0;
        int gastos = 0;

        for (int i = 0; i < modelo.getRowCount(); i++) {
            String tipo = String.valueOf(modelo.getValueAt(i, 2));
            String valorTexto = String.valueOf(modelo.getValueAt(i, 3));
            int valor = parsearMoneda(valorTexto);

            if ("Ingreso".equals(tipo)) {
                ventas += valor;
            } else {
                gastos += valor;
            }
        }

        lblVentas.setText(MONEDA.format(ventas));
        lblGastos.setText(MONEDA.format(gastos));
        lblEfectivo.setText(MONEDA.format(ventas - gastos));
    }

    private int parsearMoneda(String valor) {
        String soloNumeros = valor.replaceAll("[^0-9]", "");
        if (soloNumeros.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(soloNumeros);
    }

    private void cerrarCaja() {
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Confirmas el cierre de caja del día "
                        + lblFecha.getText() + "?",
                "Confirmar cierre",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (respuesta == JOptionPane.YES_OPTION) {
            lblEstado.setText("● Caja cerrada");
            lblEstado.setForeground(ROJO);

            JOptionPane.showMessageDialog(
                    this,
                    "Caja cerrada correctamente.\n"
                            + "Efectivo esperado: " + lblEfectivo.getText(),
                    "Cierre completado",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static JPanel tarjeta(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        return panel;
    }

    private static JButton boton(String texto, Color fondo, Color frente) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Serif", Font.BOLD, 15));
        boton.setBackground(fondo);
        boton.setForeground(frente);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setOpaque(true);
        return boton;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignorada) {
                // Se conserva el estilo predeterminado.
            }

            new FrmCierreCaja().setVisible(true);
        });
    }
}