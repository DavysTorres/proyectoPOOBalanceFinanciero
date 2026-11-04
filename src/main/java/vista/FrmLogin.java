package vista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FrmLogin {

    private JFrame ventana;
    private JPanel panelPrincipal;
    private JPanel panelMarca;
    private JPanel panelFormulario;
    private JPanel panelRegistro;

    private JLabel lblUsuario;
    private JLabel lblContrasena;
    private JLabel lblSinCuenta;
    private JLabel lblMarca;

    private JTextField txtUsuario;
    private JPasswordField txtContrasena;

    private JButton btnIngresar;
    private JButton btnRegistrar;

    public FrmLogin() {
        ventana = new JFrame("LOGIN");
        ventana.setContentPane(formulario());
        ventana.setJMenuBar(menubar());
        ventana.getRootPane().setDefaultButton(btnIngresar);
        ventana.pack();
        ventana.setLocationRelativeTo(null);
        ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ventana.setVisible(true);
    }

    private JPanel formulario() {
        panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new GridLayout(1,2));

        lblUsuario = new JLabel("Usuario:");
        lblContrasena = new JLabel("Contrasena:");
        lblSinCuenta = new JLabel("No tengo una cuenta:");

        txtUsuario = new JTextField(20);
        txtContrasena = new JPasswordField(20);

        btnIngresar = new JButton("Login");
        btnRegistrar = new JButton("Sing Up");

        btnIngresar.addActionListener(e -> iniciarSesion());

        panelMarca = new JPanel(new GridBagLayout());
        panelMarca.setBackground(new Color(0, 91, 100));
        lblMarca = new JLabel("BALANCE LOGO");
        lblMarca.setForeground(Color.WHITE);
        lblMarca.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));
        panelMarca.add(lblMarca);

        panelFormulario = new JPanel();
        panelFormulario.setLayout(new BoxLayout(panelFormulario, BoxLayout.Y_AXIS));
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(30, 25, 30, 25));
        panelFormulario.setBackground(Color.WHITE);
        panelFormulario.add(new JLabel("LOGIN"));
        panelFormulario.add(Box.createVerticalStrut(20));

        panelFormulario.add(lblUsuario);
        panelFormulario.add(txtUsuario);
        panelFormulario.add(Box.createVerticalStrut(10));

        panelFormulario.add(lblContrasena);
        panelFormulario.add(txtContrasena);
        panelFormulario.add(Box.createVerticalStrut(15));

        panelFormulario.add(btnIngresar);
        panelFormulario.add(Box.createVerticalStrut(20));

        panelPrincipal.add(panelMarca);
        panelPrincipal.add(panelFormulario);

        panelRegistro = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelRegistro.setOpaque(false);

        panelRegistro.add(lblSinCuenta);
        panelRegistro.add(Box.createHorizontalStrut(10));
        panelRegistro.add(btnRegistrar);
        panelFormulario.add(panelRegistro);

        // BoxLayout debe usar la misma alineación para todos sus componentes.
        for (Component componente : panelFormulario.getComponents()) {
            if (componente instanceof JComponent) {
                ((JComponent) componente).setAlignmentX(Component.LEFT_ALIGNMENT);
            }
        }



        return panelPrincipal;
    }

    private void iniciarSesion() {
        String usuario = txtUsuario.getText().trim();
        char[] contrasena = txtContrasena.getPassword();

        try {
            if (usuario.isEmpty()) {
                JOptionPane.showMessageDialog(ventana, "Ingrese su usuario.",
                        "Datos incompletos", JOptionPane.WARNING_MESSAGE);
                txtUsuario.requestFocusInWindow();
                return;
            }

            if (contrasena.length == 0) {
                JOptionPane.showMessageDialog(ventana, "Ingrese su contraseña.",
                        "Datos incompletos", JOptionPane.WARNING_MESSAGE);
                txtContrasena.requestFocusInWindow();
                return;
            }

            // Conectar aquí el servicio que compruebe las credenciales.
            JOptionPane.showMessageDialog(ventana,
                    "Campos completos. Falta conectar el servicio de autenticación.",
                    "Inicio de sesión", JOptionPane.INFORMATION_MESSAGE);
        } finally {
            java.util.Arrays.fill(contrasena, '\0');
        }
    }

    private JMenuBar menubar(){
        JMenuBar menubar = new JMenuBar();

        JMenu mnArchivo = new JMenu("Archivo");
        JMenu mnEditar = new JMenu("Editar");

        JMenuItem miGuardar = new JMenuItem("Guardar");
        JMenuItem miSalir = new JMenuItem("Salir");

        miSalir.addActionListener(new  ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                var op = JOptionPane.showConfirmDialog(null,"¿Quiere salir?");
                if(op == JOptionPane.YES_OPTION){
                    System.exit(0);
                }
            }
        });

        mnArchivo.add(miGuardar);
        mnArchivo.add(miSalir);

        menubar.add(mnArchivo);
        menubar.add(mnEditar);

        return menubar;
    }
}
