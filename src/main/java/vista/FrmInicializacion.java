package vista;

import vista.contratos.*;
import javax.swing.*;
import java.awt.*;

/** Primer uso: el servicio guarda administrador y negocio en una transacción. */
public final class FrmInicializacion extends FrmConfiguracion implements VistaInicializacion {
    private final JTextField administrador = texto("Nombre del administrador:", "");
    private final JTextField username = texto("Usuario del administrador:", "");
    private final JPasswordField clave = campo("Clave (mínimo 8 caracteres):", new JPasswordField());
    public FrmInicializacion() {
        super("Bienvenido a ZeroLoss", "Primer uso · Configure el negocio y cree su primera cuenta de administrador.");
        ocultarVolver();
        organizarSecciones();
        accion("Crear negocio y administrador", AccionVista.GUARDAR);
        estado.setText("Guarde estos datos para comenzar. La cuenta tendrá acceso a todos los módulos.");
        setMinimumSize(new Dimension(800, 620));
        setSize(880, 700);
        setLocationRelativeTo(null);
    }
    private void organizarSecciones() {
        Component[] controles = campos.getComponents();
        campos.removeAll();
        campos.setLayout(new BoxLayout(campos, BoxLayout.Y_AXIS));
        for (int grupo = 0; grupo < 2; grupo++) {
            JPanel seccion = new JPanel(new BorderLayout(0, 12));
            seccion.setOpaque(false);
            JLabel titulo = new JLabel(grupo == 0 ? "01  Datos del negocio" : "02  Cuenta del administrador");
            titulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
            titulo.setForeground(TemaZeroLoss.ACENTO);
            seccion.add(titulo, BorderLayout.NORTH);
            JPanel entradas = new JPanel(new FormularioZeroLoss.Campos());
            entradas.setOpaque(false);
            int desde = grupo == 0 ? 0 : 12, hasta = grupo == 0 ? 12 : controles.length;
            for (int i = desde; i < hasta; i++) entradas.add(controles[i]);
            seccion.add(entradas, BorderLayout.CENTER);
            seccion.setAlignmentX(Component.LEFT_ALIGNMENT);
            campos.add(seccion);
            if (grupo == 0) campos.add(Box.createVerticalStrut(20));
        }
    }
    @Override public String getNombreAdministrador() { return leer(administrador); }
    @Override public String getUsername() { return leer(username); }
    @Override public char[] getContrasena() { return clave.getPassword(); }
    @Override public void limpiarContrasena() { clave.setText(""); }
}
