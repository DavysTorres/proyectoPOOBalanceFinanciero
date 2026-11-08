package vista.main;

import javax.swing.*;
import java.awt.*;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;

public class FrmPrincipal extends FrmBase {
    
    private JDesktopPane escritorioPrincipal;
    private JButton btnVentas, btnCompras, btnBalance, btnHistorial, btnAdministracion, btnCierreCaja, btnMargen;
    private JLabel lblUsuario;
    private PanelSemaforo panelSemaforo; 

    public FrmPrincipal() {
        super("Sistema de Balance Diario");
        configurarVentana();
        inicializarComponentes();
    }

    private void configurarVentana() {
        setSize(700, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
    }

    private void inicializarComponentes() {
        escritorioPrincipal = new JDesktopPane();
        escritorioPrincipal.setBackground(new Color(50, 85, 130));
        add(escritorioPrincipal, BorderLayout.CENTER);

        // Menú Lateral
        JPanel pnlMenu = new JPanel(new GridLayout(8, 1, 5, 5));
        
        pnlMenu.setBackground(new Color(245, 245, 245));
        
        pnlMenu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 3, new Color(158, 97, 49)), 
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        btnVentas = crearBotonMenuLateral("Ventas");
        btnCompras = crearBotonMenuLateral("Compras / Gastos");
        btnBalance = crearBotonMenuLateral("Balance");
        btnMargen = crearBotonMenuLateral("Margen por Producto");
        btnHistorial = crearBotonMenuLateral("Historial");
        btnCierreCaja = crearBotonMenuLateral("Cierre de Caja");
        btnAdministracion = crearBotonMenuLateral("Administración");

        // Los botoenes se dejan ocultos por defecto, todos ocultos y el controlador los encenderá.
        btnVentas.setVisible(false);
        btnCompras.setVisible(false);
        btnBalance.setVisible(false);
        btnMargen.setVisible(false);
        btnHistorial.setVisible(false);
        btnCierreCaja.setVisible(false);
        btnAdministracion.setVisible(false);

        pnlMenu.add(btnVentas);
        pnlMenu.add(btnCompras);
        pnlMenu.add(btnBalance);
        pnlMenu.add(btnMargen);
        pnlMenu.add(btnHistorial);
        pnlMenu.add(btnCierreCaja);
        pnlMenu.add(btnAdministracion);

        add(pnlMenu, BorderLayout.WEST);

        // Barra de Estado
        JPanel pnlEstado = new JPanel(new BorderLayout());
        lblUsuario = new JLabel("Usuario: ");
        lblUsuario.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        
        panelSemaforo = new PanelSemaforo();

        pnlEstado.add(lblUsuario, BorderLayout.WEST);
        pnlEstado.add(panelSemaforo, BorderLayout.CENTER);

        add(pnlEstado, BorderLayout.SOUTH);
    }

    // Getters para que el Controlador pueda manipular la vista
    public JButton getBtnVentas() { return btnVentas; }
    public JButton getBtnCompras() { return btnCompras; }
    public JButton getBtnBalance() { return btnBalance; }
    public JButton getBtnMargen() { return btnMargen; }
    public JButton getBtnHistorial() { return btnHistorial; }
    public JButton getBtnCierreCaja() { return btnCierreCaja; }
    public JButton getBtnAdministracion() { return btnAdministracion; }
    public JLabel getLblUsuario() { return lblUsuario; }
    public PanelSemaforo getPanelSemaforo() { return panelSemaforo; }
    public JDesktopPane getEscritorioPrincipal() { return escritorioPrincipal; }

    private JButton crearBotonMenuLateral(String texto) {
        JButton boton = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isArmed()) {
                    g2.setColor(new Color(220, 220, 220));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(240, 240, 240));
                } else {
                    g2.setColor(Color.WHITE);
                }
                
                // Utiliizamos un radio de 15px para que encaje mejor en el menú lateral
                g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
                
                g2.setColor(new Color(158, 97, 49)); // Borde Marrón Dorado
                g2.setStroke(new BasicStroke(2.0f)); 
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
                
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Sin borde
            }
        };
        
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setForeground(new Color(25, 48, 80)); 
        boton.setFocusPainted(false); 
        boton.setContentAreaFilled(false); 
        boton.setOpaque(false); 
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return boton;
    }
}