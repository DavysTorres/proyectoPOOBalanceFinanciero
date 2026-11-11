package vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

public class FrmHistorial extends JFrame {

    private static final Color FONDO = new Color(247, 247, 247);
    private static final Color PANEL = new Color(238, 238, 238);
    private static final Color TEXTO = new Color(45, 45, 45);

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"Venta", "Modificada por", "Cuándo",
                "Antes", "Después", "Motivo"}, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    public FrmHistorial() {
        super("Historial de ventas");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(850, 500));
        setSize(1050, 580);
        setLocationRelativeTo(null);

        getContentPane().setBackground(FONDO);
        setLayout(new BorderLayout(14, 14));
        ((JPanel) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(22, 24, 22, 24));

        cargarDatosEjemplo();

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearTabla(), BorderLayout.CENTER);
    }

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titulo = new JLabel("Historial de ventas");
        titulo.setFont(new Font("Serif", Font.BOLD, 26));
        titulo.setForeground(TEXTO);

        JLabel subtitulo = new JLabel(
                "Registro de ventas modificadas");
        subtitulo.setFont(new Font("Serif", Font.PLAIN, 16));
        subtitulo.setForeground(new Color(110, 110, 110));

        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 4));
        textos.setOpaque(false);
        textos.add(titulo);
        textos.add(subtitulo);

        panel.add(textos, BorderLayout.WEST);
        return panel;
    }

    private JPanel crearTabla() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        JTable tabla = new JTable(modelo);
        tabla.setFont(new Font("Serif", Font.PLAIN, 15));
        tabla.setForeground(TEXTO);
        tabla.setBackground(PANEL);
        tabla.setRowHeight(42);
        tabla.setGridColor(new Color(215, 215, 215));
        tabla.setShowVerticalLines(false);
        tabla.setSelectionBackground(new Color(255, 239, 160));
        tabla.setSelectionForeground(TEXTO);
        tabla.getTableHeader().setFont(
                new Font("SansSerif", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(new Color(232, 232, 232));
        tabla.getTableHeader().setForeground(TEXTO);
        tabla.getTableHeader().setReorderingAllowed(false);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PANEL);

        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void cargarDatosEjemplo() {
        modelo.addRow(new Object[]{
            "Venta #1042",
            "Jefe",
            "2025-10-24",
            "$11.100",
            "$8.100",
            "Error al digitalizar"
        });

        modelo.addRow(new Object[]{
            "Venta #1038",
            "Jefe",
            "2026-01-24",
            "$5.200",
            "$3.200",
            "Devolvieron una leche"
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignorada) {
                // Se mantiene el estilo predeterminado de Swing.
            }

            new FrmHistorial().setVisible(true);
        });
    }
}