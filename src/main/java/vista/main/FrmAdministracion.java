package vista.main;

import javax.swing.*;
import java.awt.*;

public class FrmAdministracion extends JInternalFrame {
    private JButton btnUsuarios, btnProductos, btnCostosFijos, btnConfiguracion, btnHistorial, btnReportes;

    public FrmAdministracion() {
        // Configuración básica del JInternalFrame: título, redimensionable, cerrable, maximizable, iconificable
        super("Panel de Administración", true, true, true, true);
        configurarVentana();
        inicializarComponentes();
    }

    private void configurarVentana() {
        setSize(600, 400);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 245));
    }

    private void inicializarComponentes() {
        // Panel superior con un título
        JPanel pnlTitulo = new JPanel();
        pnlTitulo.setBackground(Color.WHITE);
        pnlTitulo.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(158, 97, 49)));
        JLabel lblTitulo = new JLabel("Módulos Administrativos");
        lblTitulo.setForeground(new Color(25, 48, 80));
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        pnlTitulo.add(lblTitulo);
        add(pnlTitulo, BorderLayout.NORTH);
        // Panel central con los 6 botones
        JPanel pnlBotones = new JPanel(new GridLayout(2, 3, 15, 15));
        pnlBotones.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        pnlBotones.setOpaque(false);

        btnUsuarios = crearBotonMenu("<html><center>Gestión de<br>Usuarios</center></html>");
        btnProductos = crearBotonMenu("<html><center>Catálogo de<br>Productos</center></html>");
        btnCostosFijos = crearBotonMenu("<html><center>Costos<br>Fijos</center></html>");
        btnConfiguracion = crearBotonMenu("<html><center>Configuración<br>del Sistema</center></html>");
        btnHistorial = crearBotonMenu("<html><center>Historial de<br>Cambios</center></html>");
        btnReportes = crearBotonMenu("<html><center>Reportes<br>Financieros</center></html>");

        pnlBotones.add(btnUsuarios);
        pnlBotones.add(btnProductos);
        pnlBotones.add(btnCostosFijos);
        pnlBotones.add(btnConfiguracion);
        pnlBotones.add(btnHistorial);
        pnlBotones.add(btnReportes);

        add(pnlBotones, BorderLayout.CENTER);
    }

    // Método auxiliar para mantener el mismo diseño en todos los botones
    private JButton crearBotonMenu(String texto) {
        JButton boton = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // Suavizar los bordes
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // 1. Color de FONDO 
                if (getModel().isArmed()) {
                    g2.setColor(new Color(220, 220, 220)); 
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(240, 240, 240));
                } else {
                    g2.setColor(Color.WHITE); 
                }
                // Fondo redondeado
                g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
                // Color y grosor del BORDE
                g2.setColor(new Color(158, 97, 49)); 
                g2.setStroke(new BasicStroke(2.0f)); 
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
            @Override
            protected void paintBorder(Graphics g) {
                // Vacío para eliminar el borde cuadrado
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

    // Getters para el controlador
    public JButton getBtnUsuarios() { return btnUsuarios; }
    public JButton getBtnProductos() { return btnProductos; }
    public JButton getBtnCostosFijos() { return btnCostosFijos; }
    public JButton getBtnConfiguracion() { return btnConfiguracion; }
    public JButton getBtnHistorial() { return btnHistorial; }
    public JButton getBtnReportes() { return btnReportes; }
}

