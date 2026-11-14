package vista;

import vista.contratos.*;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/** Acceso a ZeroLoss; la autenticacion permanece en LoginControlador. */
public final class FrmLogin extends FrmBase implements VistaLogin {
    private static final Color TINTA = TemaZeroLoss.TINTA;
    private static final Color GRIS = TemaZeroLoss.SUAVE;
    private static final Color ACENTO = TemaZeroLoss.ACENTO;
    private final JTextField usuario = new JTextField(20);
    private final JPasswordField contrasena = new JPasswordField(20);
    private final char ecoOculto = contrasena.getEchoChar();
    private final JButton ingresar = new BotonAcceso();
    private final JButton verContrasena = new JButton("Mostrar");
    private final JLabel estado = etiqueta(" ", 12, GRIS, false);

    @Override protected boolean usaTemaFormulario() { return false; }

    public FrmLogin() {
        super("Iniciar sesi\u00f3n - ZeroLoss");
        registrarBoton(AccionVista.INGRESAR, ingresar);
        JPanel tarjeta = new JPanel(new GridLayout(1, 2)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = suave(g);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
            }
            @Override protected void paintChildren(Graphics g) {
                Graphics2D g2 = suave(g);
                g2.clip(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30));
                super.paintChildren(g2);
                g2.dispose();
            }
        };
        tarjeta.setOpaque(false);
        JPanel marca = crearMarca();
        tarjeta.add(marca);
        tarjeta.add(crearFormulario());
        JPanel fondo = new JPanel(null) {
            @Override public void doLayout() {
                int ancho = Math.max(280, Math.min(1040, getWidth() - 24));
                int alto = Math.max(530, Math.min(570, getHeight() - 24));
                if (ancho < 760 && marca.getParent() == tarjeta) tarjeta.remove(marca);
                else if (ancho >= 760 && marca.getParent() != tarjeta) tarjeta.add(marca, 0);
                tarjeta.setBounds((getWidth() - ancho) / 2, (getHeight() - alto) / 2, ancho, alto);
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = suave(g);
                g2.setPaint(new GradientPaint(0, 0, new Color(234, 244, 246), getWidth(), getHeight(), new Color(244, 247, 251)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(221, 237, 240));
                g2.fillOval(-150, -160, 440, 440);
                g2.fillOval(getWidth() - 230, getHeight() - 220, 420, 420);
                int x = tarjeta.getX(), y = tarjeta.getY();
                for (int i = 12; i > 0; i--) {
                    g2.setColor(new Color(28, 60, 72, 2));
                    g2.fillRoundRect(x - i, y - i + 6, tarjeta.getWidth() + i * 2, tarjeta.getHeight() + i * 2, 36, 36);
                }
                g2.dispose();
            }
        };
        fondo.setPreferredSize(new Dimension(320, 554));
        fondo.add(tarjeta);
        JScrollPane scroll = new JScrollPane(fondo);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        setContentPane(scroll);
        getRootPane().setDefaultButton(ingresar);
        usuario.addActionListener(e -> contrasena.requestFocusInWindow());
        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) { usuario.requestFocusInWindow(); }
        });
        setMinimumSize(new Dimension(920, 650));
        setSize(1080, 720);
        setLocationRelativeTo(null);
    }

    private JPanel crearMarca() {
        JPanel marca = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = suave(g);
                g2.setPaint(new GradientPaint(0, 0, new Color(20, 46, 66), getWidth(), getHeight(), new Color(0, 112, 119)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillOval(getWidth() - 160, -110, 310, 310);
                g2.drawOval(-110, getHeight() - 180, 330, 330);
                g2.dispose();
            }
        };
        marca.setLayout(new BoxLayout(marca, BoxLayout.Y_AXIS));
        marca.setBorder(BorderFactory.createEmptyBorder(40, 36, 32, 36));
        marca.add(Box.createVerticalGlue());
        JLabel simbolo = new JLabel(new TemaZeroLoss.IconoMarca(104));
        simbolo.getAccessibleContext().setAccessibleName("Logo ZeroLoss: crecimiento y equilibrio");
        centrado(marca, simbolo);
        marca.add(Box.createVerticalStrut(20));
        centrado(marca, etiqueta("ZeroLoss", 38, Color.WHITE, true));
        marca.add(Box.createVerticalStrut(24));
        centrado(marca, etiqueta("Cada venta cuenta.", 23, Color.WHITE, true));
        marca.add(Box.createVerticalStrut(5));
        centrado(marca, etiqueta("Tu negocio, en equilibrio.", 19, new Color(187, 232, 230), false));
        marca.add(Box.createVerticalStrut(24));
        centrado(marca, etiqueta("Ventas, control y decisiones claras.", 13, new Color(187, 211, 221), false));
        marca.add(Box.createVerticalGlue());
        marca.add(Box.createVerticalStrut(24));
        centrado(marca, etiqueta("SISTEMA DE BALANCE DIARIO", 10, new Color(164, 201, 211), true));
        return marca;
    }

    private JPanel crearFormulario() {
        JPanel formulario = new JPanel();
        formulario.setBackground(Color.WHITE);
        formulario.setLayout(new BoxLayout(formulario, BoxLayout.Y_AXIS));
        formulario.setBorder(BorderFactory.createEmptyBorder(36, 38, 30, 38));
        formulario.add(Box.createVerticalGlue());
        agregar(formulario, etiqueta("TE DAMOS LA BIENVENIDA", 11, ACENTO, true));
        formulario.add(Box.createVerticalStrut(10));
        agregar(formulario, etiqueta("Inicia sesi\u00f3n", 29, TINTA, true));
        formulario.add(Box.createVerticalStrut(10));
        agregar(formulario, etiqueta("Ingresa tus datos para continuar.", 14, GRIS, false));
        formulario.add(Box.createVerticalStrut(30));
        agregarCampo(formulario, "Usuario", usuario, false);
        formulario.add(Box.createVerticalStrut(20));
        agregarCampo(formulario, "Contrase\u00f1a", contrasena, true);
        formulario.add(Box.createVerticalStrut(24));
        ingresar.setText("Ingresar");
        ingresar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        ingresar.setForeground(Color.WHITE);
        ingresar.setContentAreaFilled(false);
        ingresar.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        ingresar.setPreferredSize(new Dimension(300, 48));
        ingresar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        ingresar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        agregar(formulario, ingresar);
        formulario.add(Box.createVerticalStrut(12));
        agregar(formulario, estado);
        formulario.add(Box.createVerticalGlue());
        return formulario;
    }

    private void agregarCampo(JPanel formulario, String nombre, JTextField campo, boolean clave) {
        JLabel label = etiqueta(nombre, 13, TINTA, true);
        label.setLabelFor(campo);
        agregar(formulario, label);
        formulario.add(Box.createVerticalStrut(8));
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        campo.setForeground(TINTA);
        campo.setCaretColor(ACENTO);
        campo.setOpaque(false);
        campo.setBorder(BorderFactory.createEmptyBorder(6, 13, 6, 8));
        campo.getAccessibleContext().setAccessibleName(nombre);
        campo.setToolTipText(clave ? "Ingresa tu contrase\u00f1a" : "Ingresa tu nombre de usuario");
        JPanel contenedor = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = suave(g);
                g2.setColor(campo.isEnabled() ? new Color(248, 250, 252) : new Color(239, 242, 245));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
        contenedor.setOpaque(false);
        contenedor.setBorder(new AbstractBorder() {
            @Override public Insets getBorderInsets(Component c) { return new Insets(2, 2, 2, 2); }
            @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                Graphics2D g2 = suave(g);
                g2.setColor(campo.hasFocus() ? ACENTO : new Color(214, 223, 232));
                g2.setStroke(new BasicStroke(campo.hasFocus() ? 2f : 1f));
                g2.drawRoundRect(x + 1, y + 1, w - 3, h - 3, 14, 14);
                g2.dispose();
            }
        });
        campo.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { contenedor.repaint(); }
            @Override public void focusLost(FocusEvent e) { contenedor.repaint(); }
        });
        contenedor.add(campo, BorderLayout.CENTER);
        if (clave) {
            verContrasena.setFont(new Font("Segoe UI", Font.BOLD, 11));
            verContrasena.setForeground(ACENTO);
            verContrasena.setContentAreaFilled(false);
            verContrasena.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 12));
            verContrasena.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            verContrasena.getAccessibleContext().setAccessibleName("Mostrar contrase\u00f1a");
            verContrasena.addActionListener(e -> {
                boolean mostrar = contrasena.getEchoChar() != 0;
                contrasena.setEchoChar(mostrar ? (char)0 : ecoOculto);
                verContrasena.setText(mostrar ? "Ocultar" : "Mostrar");
                verContrasena.getAccessibleContext().setAccessibleName((mostrar ? "Ocultar" : "Mostrar") + " contrase\u00f1a");
            });
            contenedor.add(verContrasena, BorderLayout.EAST);
        }
        contenedor.setPreferredSize(new Dimension(300, 48));
        contenedor.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        agregar(formulario, contenedor);
    }

    private static JLabel etiqueta(String texto, int tamano, Color color, boolean negrita) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", negrita ? Font.BOLD : Font.PLAIN, tamano));
        label.setForeground(color);
        return label;
    }
    private static void agregar(JPanel panel, JComponent componente) {
        componente.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(componente);
    }
    private static void centrado(JPanel panel, JComponent componente) {
        componente.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(componente);
    }
    private static Graphics2D suave(Graphics g) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return g2;
    }

    private static final class BotonAcceso extends JButton {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            g2.setColor(!isEnabled() ? new Color(143, 168, 176) : getModel().isPressed()
                    ? new Color(0, 88, 96) : getModel().isRollover() ? new Color(0, 139, 143) : ACENTO);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            if (hasFocus()) {
                g2.setColor(Color.WHITE);
                g2.drawRoundRect(4, 4, getWidth() - 9, getHeight() - 9, 10, 10);
            }
            g2.dispose();
            super.paintComponent(g);
        }
        @Override protected void paintBorder(Graphics g) { }
    }

    @Override public void setOcupada(boolean valor) {
        super.setOcupada(valor);
        usuario.setEnabled(!valor);
        contrasena.setEnabled(!valor);
        verContrasena.setEnabled(!valor);
        ingresar.setText(valor ? "Ingresando..." : "Ingresar");
        estado.setText(valor ? "Validando tus datos..." : " ");
    }
    @Override public String getUsername() { return usuario.getText(); }
    @Override public char[] getContrasena() { return contrasena.getPassword(); }
    @Override public void limpiarContrasena() {
        contrasena.setText("");
        contrasena.setEchoChar(ecoOculto);
        verContrasena.setText("Mostrar");
        verContrasena.getAccessibleContext().setAccessibleName("Mostrar contrase" + (char)241 + "a");
    }
}
