package vista;

<<<<<<< HEAD
import javax.swing.*;
import java.awt.*;

public class FrmMargen extends JFrame {

    private JButton btnVolver;
    private JTextArea txtConsejo;

    public FrmMargen() {
        setTitle("Margen por Producto - ZeroLoss");
        setSize(420, 540);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        // Encabezado
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(Color.WHITE);
        btnVolver = new JButton("←");
        btnVolver.setFocusPainted(false);
        JLabel titleLabel = new JLabel("¿Qué productos te dejan plata?");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        headerPanel.add(btnVolver);
        headerPanel.add(titleLabel);

        // Contenido central
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        // Sección: Los que MÁS te dejan
        JLabel lblMas = new JLabel("Los que MÁS te dejan:");
        lblMas.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblMas.setForeground(new Color(0, 128, 0));

        JPanel masPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        masPanel.setBackground(Color.WHITE);
        masPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 15, 10));
        masPanel.add(new JLabel("Gaseosa 1.5 L"));
        masPanel.add(new JLabel("margen 42 %", SwingConstants.RIGHT));
        masPanel.add(new JLabel("Pan tajado"));
        masPanel.add(new JLabel("margen 38 %", SwingConstants.RIGHT));

        // Sección: Los que CASI no te dejan
        JLabel lblMenos = new JLabel("🔴  Los que CASI no te dejan:");
        lblMenos.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblMenos.setForeground(Color.RED);

        JPanel menosPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        menosPanel.setBackground(Color.WHITE);
        menosPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 15, 10));
        menosPanel.add(new JLabel("Arroz 1 kg"));
        menosPanel.add(new JLabel("margen 8 %  ", SwingConstants.RIGHT));
        menosPanel.add(new JLabel("Leche 1 L"));
        menosPanel.add(new JLabel("margen 11 % ", SwingConstants.RIGHT));

        // Sección: Consejos
        JLabel lblConsejoTitle = new JLabel("💡  Consejo:");
        lblConsejoTitle.setFont(new Font("SansSerif", Font.BOLD, 14));

        txtConsejo = new JTextArea("\"El arroz tiene muy poco margen.\nRevisa el precio de compra o súbelo un poco.\"");
        txtConsejo.setFont(new Font("SansSerif", Font.ITALIC, 13));
=======
import modelo.Producto;
import vista.contratos.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Locale;

/** Dos grupos del ranking recibido y consejos solicitados al controlador. */
public final class FrmMargen extends FrmBase implements VistaMargen {
    private final DefaultListModel<Producto> mas = new DefaultListModel<>();
    private final DefaultListModel<Producto> menos = new DefaultListModel<>();
    private final JList<Producto> listaMas = new JList<>(mas);
    private final JList<Producto> listaMenos = new JList<>(menos);
    private final JTextArea txtConsejo = new JTextArea("Seleccione un producto y pulse Ver consejo.", 4, 24);
    private final JLabel estado = new JLabel("Consulte el catálogo para ver los márgenes.");
    private final JButton btnVolver = new JButton("\u2190");

    public FrmMargen() {
        super("Margen por Producto - ZeroLoss");
        JPanel principal = new JPanel(new BorderLayout(10, 10));
        principal.setBackground(Color.WHITE);
        principal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JPanel cabecera = new JPanel(new BorderLayout(10, 0));
        cabecera.setOpaque(false);
        btnVolver.setFocusPainted(false);
        btnVolver.setToolTipText("Volver al menú");
        btnVolver.addActionListener(e -> solicitarCierre());
        cabecera.add(btnVolver, BorderLayout.WEST);
        JLabel titulo = new JLabel("¿Qué productos te dejan plata?");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        cabecera.add(titulo, BorderLayout.CENTER);
        principal.add(cabecera, BorderLayout.NORTH);
        JPanel contenido = new JPanel(new FormularioZeroLoss.Rejilla(320, 2, 18));
        contenido.setOpaque(false);
        contenido.add(seccion("Los que MÁS te dejan:", new Color(0, 128, 0), listaMas));
        contenido.add(seccion("Los de MENOR margen:", new Color(185, 28, 28), listaMenos));
        principal.add(contenido, BorderLayout.CENTER);
        listaMas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !listaMas.isSelectionEmpty()) { listaMenos.clearSelection(); limpiarConsejo(); }
        });
        listaMenos.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !listaMenos.isSelectionEmpty()) { listaMas.clearSelection(); limpiarConsejo(); }
        });
        JPanel pie = new JPanel(new BorderLayout(5, 8));
        pie.setOpaque(false);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        acciones.setOpaque(false);
        JButton actualizar = new JButton("Actualizar ranking");
        JButton consejo = new JButton("Ver consejo");
        registrarBoton(AccionVista.CARGAR, actualizar);
        registrarBoton(AccionVista.CONSEJO, consejo);
        acciones.add(actualizar);
        acciones.add(consejo);
        pie.add(acciones, BorderLayout.NORTH);
        txtConsejo.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 13));
>>>>>>> 725c7fa98dbc60634746338e00a76164ed0f4cdf
        txtConsejo.setEditable(false);
        txtConsejo.setLineWrap(true);
        txtConsejo.setWrapStyleWord(true);
        txtConsejo.setBackground(new Color(245, 245, 245));
        txtConsejo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
<<<<<<< HEAD

        contentPanel.add(lblMas);
        contentPanel.add(masPanel);
        contentPanel.add(lblMenos);
        contentPanel.add(menosPanel);
        contentPanel.add(Box.createVerticalStrut(10));
        contentPanel.add(lblConsejoTitle);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(txtConsejo);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        add(mainPanel);
    }
}
=======
        JScrollPane scroll = new JScrollPane(txtConsejo);
        scroll.setBorder(BorderFactory.createTitledBorder("Consejo"));
        pie.add(scroll, BorderLayout.CENTER);
        pie.add(estado, BorderLayout.SOUTH);
        principal.add(pie, BorderLayout.SOUTH);
        setContentPane(principal);
        setMinimumSize(new Dimension(740, 560));
        setSize(500, 590);
        setLocationRelativeTo(null);
    }

    private JPanel seccion(String titulo, Color color, JList<Producto> lista) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);
        JLabel label = new JLabel(titulo);
        label.setForeground(color);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panel.add(label, BorderLayout.NORTH);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> l, Object v, int i, boolean s, boolean f) {
                Producto p = (Producto) v;
                JLabel celda = (JLabel) super.getListCellRendererComponent(l, p.getNombre()
                        + String.format(Locale.forLanguageTag("es-CO"), "   — margen %.2f %%", p.calcularMargen()), i, s, f);
                celda.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
                return celda;
            }
        });
        panel.add(new JScrollPane(lista), BorderLayout.CENTER);
        return panel;
    }

    private void limpiarConsejo() { txtConsejo.setText("Pulse Ver consejo para el producto seleccionado."); }

    @Override public Producto getProductoSeleccionado() {
        return listaMas.getSelectedValue() != null ? listaMas.getSelectedValue() : listaMenos.getSelectedValue();
    }

    @Override public void mostrarRanking(List<Producto> productos) {
        mas.clear();
        menos.clear();
        // El controlador entrega el ranking descendente; no se modifica ni se vuelve a ordenar.
        int superiores = Math.min(2, (productos.size() + 1) / 2);
        for (int i = 0; i < superiores; i++) mas.addElement(productos.get(i));
        for (int i = Math.max(superiores, productos.size() - 2); i < productos.size(); i++) menos.addElement(productos.get(i));
        txtConsejo.setText("Seleccione un producto y pulse Ver consejo.");
        estado.setText(productos.isEmpty() ? "No hay productos activos."
                : "Seleccione un producto para consultar su consejo.");
    }

    @Override public void mostrarConsejo(String consejo) { txtConsejo.setText(consejo); txtConsejo.setCaretPosition(0); }

    @Override public void setOcupada(boolean ocupada) {
        super.setOcupada(ocupada);
        listaMas.setEnabled(!ocupada);
        listaMenos.setEnabled(!ocupada);
        btnVolver.setEnabled(!ocupada);
    }
}
>>>>>>> 725c7fa98dbc60634746338e00a76164ed0f4cdf
