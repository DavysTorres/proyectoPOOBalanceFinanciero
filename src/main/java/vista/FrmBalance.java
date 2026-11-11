package vista;

import javax.swing.*;
import java.awt.*;

public class FrmBalance extends JFrame {

    private JLabel lblIngresos, lblCostosVariables, lblMargenContribucion;
    private JLabel lblCostosFijos, lblResultado, lblPuntoEquilibrio, lblFaltante;
    private JButton btnExportar, btnVolver;

    public FrmBalance() {
        setTitle("Balance del día - ZeroLoss");
        setSize(400, 520);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        // Header / Barra superior
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(Color.WHITE);
        btnVolver = new JButton("←");
        btnVolver.setFocusPainted(false);
        JLabel titleLabel = new JLabel("Balance del día - 24 oct");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        headerPanel.add(btnVolver);
        headerPanel.add(titleLabel);

        // Panel de datos del balance
        JPanel dataPanel = new JPanel(new GridLayout(7, 2, 10, 12));
        dataPanel.setBackground(Color.WHITE);

        Font labelFont = new Font("SansSerif", Font.PLAIN, 14);
        Font boldFont = new Font("SansSerif", Font.BOLD, 14);

        dataPanel.add(createLabel("Ingresos por ventas:", labelFont));
        lblIngresos = createLabel("$ 850.000", boldFont);
        dataPanel.add(lblIngresos);

        dataPanel.add(createLabel("Costos variables:", labelFont));
        lblCostosVariables = createLabel("$ 510.000", boldFont);
        dataPanel.add(lblCostosVariables);

        dataPanel.add(createLabel("Margen de contribución:", boldFont));
        lblMargenContribucion = createLabel("$ 340.000", boldFont);
        dataPanel.add(lblMargenContribucion);

        dataPanel.add(createLabel("Costos fijos del día:", labelFont));
        lblCostosFijos = createLabel("$ 390.000", boldFont);
        dataPanel.add(lblCostosFijos);

        // Resultado con indicador rojo/verde
        JPanel resPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        resPanel.setBackground(Color.WHITE);
        lblResultado = createLabel("-$ 50.000", new Font("SansSerif", Font.BOLD, 14));
        lblResultado.setForeground(Color.RED);
        JLabel dotRojo = new JLabel("●");
        dotRojo.setForeground(Color.RED);
        resPanel.add(lblResultado);
        resPanel.add(dotRojo);

        dataPanel.add(createLabel("Resultado del día:", boldFont));
        dataPanel.add(resPanel);

        dataPanel.add(createLabel("Punto de equilibrio:", boldFont));
        lblPuntoEquilibrio = createLabel("$ 1.200.000", boldFont);
        dataPanel.add(lblPuntoEquilibrio);

        dataPanel.add(createLabel("Te faltaron:", labelFont));
        lblFaltante = createLabel("$ 350.000", boldFont);
        dataPanel.add(lblFaltante);

        // Botón exportar
        btnExportar = new JButton(" Exportar a PDF / Excel");
        btnExportar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnExportar.setPreferredSize(new Dimension(0, 45));
        btnExportar.setFocusPainted(false);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(dataPanel, BorderLayout.CENTER);
        mainPanel.add(btnExportar, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JLabel createLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        return label;
    }
}
