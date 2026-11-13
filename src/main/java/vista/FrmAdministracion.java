package vista;

import modelo.*;
import javax.swing.*;
import java.awt.*;

/** Panel interno alojado por FrmPrincipal; sus botones usan el controlador principal. */
public final class FrmAdministracion extends JInternalFrame {
    private final JButton btnUsuarios = EstiloZeroLoss.botonMenu("<html><center>Gestión de<br>Usuarios</center></html>");
    private final JButton btnProductos = EstiloZeroLoss.botonMenu("<html><center>Catálogo de<br>Productos</center></html>");
    private final JButton btnCostosFijos = EstiloZeroLoss.botonMenu("<html><center>Costos<br>Fijos</center></html>");
    private final JButton btnConfiguracion = EstiloZeroLoss.botonMenu("<html><center>Configuración<br>del Sistema</center></html>");
    private final JButton btnHistorial = EstiloZeroLoss.botonMenu("<html><center>Historial de<br>Cambios</center></html>");
    private final JButton btnReportes = EstiloZeroLoss.botonMenu("<html><center>Reportes<br>Financieros</center></html>");

    public FrmAdministracion() {
        super("Panel de Administración", true, true, true, true);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        setSize(600, 400);
        setMinimumSize(new Dimension(500, 320));
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(TemaZeroLoss.FONDO);
        JPanel cabecera = new JPanel();
        cabecera.setBackground(Color.WHITE);
        cabecera.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, TemaZeroLoss.ACENTO));
        JLabel titulo = new JLabel("Módulos Administrativos");
        titulo.setForeground(TemaZeroLoss.TINTA);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        cabecera.add(titulo);
        add(cabecera, BorderLayout.NORTH);
        JPanel botones = new JPanel(new GridLayout(2, 3, 15, 15));
        botones.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        botones.setOpaque(false);
        for (JButton boton : new JButton[]{btnUsuarios, btnProductos, btnCostosFijos, btnConfiguracion, btnHistorial, btnReportes}) {
            boton.setVisible(false);
            botones.add(boton);
        }
        add(botones, BorderLayout.CENTER);
    }

    public void mostrarPermisos(Usuario usuario) {
        JButton[] botones = {btnUsuarios, btnProductos, btnCostosFijos, btnConfiguracion, btnHistorial, btnReportes};
        Permiso[] permisos = {Permiso.GESTIONAR_USUARIOS, Permiso.GESTIONAR_PRODUCTOS, Permiso.DEFINIR_COSTOS_FIJOS,
                Permiso.CONFIGURAR, Permiso.VER_HISTORIAL, Permiso.VER_REPORTES};
        for (int i = 0; i < botones.length; i++) botones[i].setVisible(usuario != null && usuario.isActivo() && usuario.tienePermiso(permisos[i]));
    }

    public JButton getBtnUsuarios() { return btnUsuarios; }
    public JButton getBtnProductos() { return btnProductos; }
    public JButton getBtnCostosFijos() { return btnCostosFijos; }
    public JButton getBtnConfiguracion() { return btnConfiguracion; }
    public JButton getBtnHistorial() { return btnHistorial; }
    public JButton getBtnReportes() { return btnReportes; }
}
