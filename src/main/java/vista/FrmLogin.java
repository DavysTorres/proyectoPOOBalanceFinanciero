package vista;

import vista.contratos.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

/**
 * Recoge credenciales; el LoginControlador realiza la autenticacion.
 */
public final class FrmLogin extends FrmBase implements VistaLogin {
    private final JTextField usuario;
    private final JPasswordField contrasena;

    public FrmLogin() {
        super("Inicio de sesion");
        usuario = new JTextField(20);
        contrasena = new JPasswordField(20);
        JButton ingresar = new JButton("Ingresar");
        registrarBoton(AccionVista.INGRESAR, ingresar);

        JPanel principal = new JPanel(new GridLayout(1, 2));
        JPanel marca = new JPanel(new GridBagLayout());
        marca.setBackground(new Color(0, 91, 100));
        JLabel logo = new JLabel("BALANCE");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 30));
        marca.add(logo);

        JPanel formulario = new JPanel();
        formulario.setLayout(new BoxLayout(formulario, BoxLayout.Y_AXIS));
        formulario.setBorder(BorderFactory.createEmptyBorder(40, 30, 40, 30));
        formulario.setBackground(Color.WHITE);
        JLabel titulo = new JLabel("INICIAR SESIÓN");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        formulario.add(titulo);
        formulario.add(Box.createVerticalStrut(28));
        formulario.add(new JLabel("Usuario"));
        formulario.add(Box.createVerticalStrut(6));
        formulario.add(usuario);
        formulario.add(Box.createVerticalStrut(16));
        formulario.add(new JLabel("Contraseña"));
        formulario.add(Box.createVerticalStrut(6));
        formulario.add(contrasena);
        formulario.add(Box.createVerticalStrut(22));
        ingresar.setBackground(new Color(0, 91, 100));
        ingresar.setForeground(Color.WHITE);
        ingresar.setOpaque(true);
        ingresar.setBorderPainted(false);
        formulario.add(ingresar);
        formulario.add(Box.createVerticalStrut(22));
        formulario.add(Box.createVerticalGlue());
        JButton solicitarCuenta = new JButton("Registrarse");
        solicitarCuenta.addActionListener(e -> mostrarInformacion(
                "Solicite su cuenta al administrador. Los usuarios se crean desde el menu Usuarios."));
        JPanel registro = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        registro.setOpaque(false);
        registro.add(new JLabel("¿No tiene cuenta  "));
        registro.add(solicitarCuenta);
        registro.setMaximumSize(new Dimension(Integer.MAX_VALUE, registro.getPreferredSize().height));
        formulario.add(registro);

        for (JTextField campo : new JTextField[]{usuario, contrasena}) {
            campo.setPreferredSize(new Dimension(280, 36));
            campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        }
        ingresar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        ingresar.setPreferredSize(new Dimension(280, 40));
        for (Component componente : formulario.getComponents()) {
            if (componente instanceof JComponent)
                ((JComponent) componente).setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        principal.add(marca);
        principal.add(formulario);
        setContentPane(principal);
        setJMenuBar(crearMenu());
        getRootPane().setDefaultButton(ingresar);
        usuario.getAccessibleContext().setAccessibleName("Usuario");
        contrasena.getAccessibleContext().setAccessibleName("Contraseña");
        setMinimumSize(new Dimension(780, 460));
        setSize(840, 500);
        setLocationRelativeTo(null);
    }

    private JMenuBar crearMenu() {
        JMenuBar barra = new JMenuBar();
        JMenu archivo = new JMenu("Archivo");
        JMenuItem salir = new JMenuItem("Salir");
        salir.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "¿Quiere salir?", "Salir",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });
        archivo.add(salir);
        barra.add(archivo);
        return barra;
    }

    @Override
    public String getUsername() {
        return usuario.getText();
    }

    @Override
    public char[] getContrasena() {
        return contrasena.getPassword();
    }

    @Override
    public void limpiarContrasena() {
        contrasena.setText("");
    }
}
