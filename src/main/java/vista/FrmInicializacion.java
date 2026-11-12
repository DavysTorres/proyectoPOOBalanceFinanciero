package vista;

import vista.contratos.*;
import javax.swing.*;
import java.awt.Dimension;

/** Primer uso: el servicio guarda administrador y negocio en una transacción. */
public final class FrmInicializacion extends FrmConfiguracion implements VistaInicializacion {
    private final JTextField administrador = texto("Nombre del administrador:", "");
    private final JTextField username = texto("Usuario del administrador:", "");
    private final JPasswordField clave = campo("Clave (mínimo 8 caracteres):", new JPasswordField());
    public FrmInicializacion() {
        super("Bienvenido a ZeroLoss", "Primer uso · Configure el negocio y cree su primera cuenta de administrador.");
        accion("Crear negocio y administrador", AccionVista.GUARDAR);
        estado.setText("Guarde estos datos para comenzar. La cuenta tendrá acceso a todos los módulos.");
        setMinimumSize(new Dimension(800, 620));
        setSize(880, 700);
        setLocationRelativeTo(null);
    }
    @Override public String getNombreAdministrador() { return leer(administrador); }
    @Override public String getUsername() { return leer(username); }
    @Override public char[] getContrasena() { return clave.getPassword(); }
    @Override public void limpiarContrasena() { clave.setText(""); }
}
