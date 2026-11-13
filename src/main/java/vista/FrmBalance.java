package vista;

import modelo.BalanceDia;
import util.FormatoMoneda;
import vista.contratos.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Presenta los importes calculados por BalanceControlador, sin repetir formulas. */
public class FrmBalance extends FrmBase implements VistaBalance {
    private static final Color ROJO = new Color(185, 28, 28);
    private static final Color VERDE = new Color(21, 128, 61);
    private final JLabel titulo = new JLabel();
    private final JTextField fecha = new JTextField(LocalDate.now().toString(), 10);
    private final JLabel lblIngresos = valor();
    private final JLabel lblCostosVariables = valor();
    private final JLabel lblMargenContribucion = valor();
    private final JLabel lblCostosFijos = valor();
    private final JLabel lblResultado = valor();
    private final JLabel indicador = new JLabel("\u25cf");
    private final JLabel lblPuntoEquilibrio = valor();
    private final JLabel lblFaltante = valor();
    private final JLabel nota = new JLabel("Consulte una fecha para ver el balance.");
    private final JButton btnExportar = new JButton("Exportar a Excel (CSV)");
    private final JButton btnVolver = new JButton("\u2190");
    private BalanceDia balanceMostrado;

    public FrmBalance() {
        super("Balance del día - ZeroLoss");
        JPanel principal = new JPanel(new BorderLayout(10, 18));
        principal.setBackground(Color.WHITE);
        principal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel cabecera = blanco(new BorderLayout(8, 12));
        JPanel barra = blanco(new BorderLayout(10, 0));
        btnVolver.setFocusPainted(false);
        btnVolver.setToolTipText("Volver al menú");
        btnVolver.getAccessibleContext().setAccessibleName("Volver al menú");
        btnVolver.addActionListener(e -> solicitarCierre());
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        actualizarTitulo(LocalDate.now());
        barra.add(btnVolver, BorderLayout.WEST);
        barra.add(titulo, BorderLayout.CENTER);
        cabecera.add(barra, BorderLayout.NORTH);
        JPanel consulta = blanco(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JLabel etiquetaFecha = new JLabel("Fecha:");
        etiquetaFecha.setLabelFor(fecha);
        fecha.setToolTipText("AAAA-MM-DD");
        JButton consultar = new JButton("Consultar");
        consultar.setFocusPainted(false);
        registrarBoton(AccionVista.CARGAR, consultar);
        fecha.addActionListener(e -> consultar.doClick());
        consulta.add(etiquetaFecha);
        consulta.add(fecha);
        consulta.add(consultar);
        cabecera.add(consulta, BorderLayout.SOUTH);

        JPanel datos = blanco(new GridLayout(0, 2, 18, 18) {
            private void adaptar(Container p) { setColumns(p.getWidth() >= 1100 ? 4 : p.getWidth() >= 820 ? 3 : 2); }
            @Override public Dimension preferredLayoutSize(Container p) { adaptar(p); return super.preferredLayoutSize(p); }
            @Override public void layoutContainer(Container p) { adaptar(p); super.layoutContainer(p); }
        });
        fila(datos, "Ingresos por ventas:", lblIngresos, false);
        fila(datos, "Costos variables:", lblCostosVariables, false);
        fila(datos, "Margen de contribución:", lblMargenContribucion, true);
        fila(datos, "Costos fijos del día:", lblCostosFijos, false);
        JPanel resultado = blanco(new FlowLayout(FlowLayout.LEFT, 5, 0));
        resultado.add(lblResultado);
        resultado.add(indicador);
        fila(datos, "Resultado del día:", resultado, true);
        fila(datos, "Punto de equilibrio:", lblPuntoEquilibrio, true);
        fila(datos, "Te faltaron:", lblFaltante, false);

        JPanel pie = blanco(new BorderLayout(0, 12));
        nota.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        pie.add(nota, BorderLayout.NORTH);
        btnExportar.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

        btnExportar.setFocusPainted(false);
        registrarBoton(AccionVista.EXPORTAR, btnExportar);
        JPanel acciones = blanco(new FormularioZeroLoss.Acciones(FlowLayout.RIGHT));
        acciones.add(btnExportar);
        pie.add(acciones, BorderLayout.SOUTH);
        principal.add(cabecera, BorderLayout.NORTH);
        principal.add(datos, BorderLayout.CENTER);
        principal.add(pie, BorderLayout.SOUTH);
        setContentPane(principal);
        mostrarBalance(null);
        setMinimumSize(new Dimension(720, 540));
        setSize(480, 540);
        setLocationRelativeTo(null);
    }

    private static JPanel blanco(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Color.WHITE);
        return panel;
    }

    private static JLabel valor() {
        JLabel label = new JLabel("—", SwingConstants.RIGHT);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        return label;
    }

    private static void fila(JPanel panel, String texto, JComponent valor, boolean negrita) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font(Font.SANS_SERIF, negrita ? Font.BOLD : Font.PLAIN, 14));
        JPanel tarjeta = TemaZeroLoss.tarjeta();
        tarjeta.setLayout(new BorderLayout(0, 10));
        etiqueta.setForeground(TemaZeroLoss.SUAVE);
        tarjeta.add(etiqueta, BorderLayout.NORTH);
        valor.setFont(new Font("Segoe UI", Font.BOLD, 22));
        if (valor instanceof JLabel label) label.setHorizontalAlignment(SwingConstants.LEFT);
        tarjeta.add(valor, BorderLayout.CENTER);
        panel.add(tarjeta);
    }

    private void actualizarTitulo(LocalDate dia) {
        titulo.setText("Balance del día - " + dia.format(
                DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es-CO"))));
    }

    @Override public String getFecha() { return fecha.getText().trim(); }

    @Override public void mostrarBalance(BalanceDia balance) {
        balanceMostrado = balance;
        if (balance == null) {
            for (JLabel label : new JLabel[]{lblIngresos, lblCostosVariables, lblMargenContribucion,
                    lblCostosFijos, lblResultado, lblPuntoEquilibrio, lblFaltante}) label.setText("—");
            lblResultado.setForeground(Color.DARK_GRAY);
            indicador.setVisible(false);
            nota.setText("Sin balance cargado.");
        } else {
            actualizarTitulo(balance.getFecha());
            lblIngresos.setText(FormatoMoneda.mostrar(balance.getIngresos()));
            lblCostosVariables.setText(FormatoMoneda.mostrar(balance.getCostosVariables()));
            lblMargenContribucion.setText(FormatoMoneda.mostrar(balance.getMargenContribucion()));
            lblCostosFijos.setText(FormatoMoneda.mostrar(balance.getCostosFijosDia()));
            lblResultado.setText(FormatoMoneda.mostrar(balance.getResultado()));
            int signo = balance.getResultado().signum();
            Color color = signo < 0 ? ROJO : signo > 0 ? VERDE : Color.DARK_GRAY;
            lblResultado.setForeground(color);
            indicador.setForeground(color);
            indicador.setVisible(true);
            indicador.setToolTipText(signo < 0 ? "Pérdida" : signo > 0 ? "Ganancia" : "En equilibrio");
            boolean calculable = balance.isPuntoEquilibrioCalculable();
            lblPuntoEquilibrio.setText(calculable ? FormatoMoneda.mostrar(balance.getPuntoEquilibrio()) : "No calculable");
            lblFaltante.setText(calculable ? FormatoMoneda.mostrar(balance.getFaltante()) : "No calculable");
            nota.setText(!calculable ? "No hay margen positivo para calcular la meta."
                    : balance.isPuntoEquilibrioEstimado() ? "Punto de equilibrio estimado según el catálogo."
                    : "Balance correspondiente al " + balance.getFecha() + ".");
        }
        btnExportar.setEnabled(balance != null && !estaOcupada());
    }

    @Override public void setOcupada(boolean ocupada) {
        super.setOcupada(ocupada);
        fecha.setEnabled(!ocupada);
        btnVolver.setEnabled(!ocupada);
        btnExportar.setEnabled(!ocupada && balanceMostrado != null);
    }

    @Override public String getRutaExportacion() {
        if (balanceMostrado == null) return null;
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Exportar balance a CSV");
        selector.setFileFilter(new FileNameExtensionFilter("Archivo CSV para Excel (*.csv)", "csv"));
        selector.setAcceptAllFileFilterUsed(false);
        selector.setSelectedFile(new java.io.File("balance-" + balanceMostrado.getFecha() + ".csv"));
        if (selector.showSaveDialog(padreDialogos()) != JFileChooser.APPROVE_OPTION) return null;
        String ruta = selector.getSelectedFile().getAbsolutePath();
        return ruta.toLowerCase(Locale.ROOT).endsWith(".csv") ? ruta : ruta + ".csv";
    }
}
