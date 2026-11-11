package vista;

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
        txtConsejo.setEditable(false);
        txtConsejo.setLineWrap(true);
        txtConsejo.setWrapStyleWord(true);
        txtConsejo.setBackground(new Color(245, 245, 245));
        txtConsejo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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