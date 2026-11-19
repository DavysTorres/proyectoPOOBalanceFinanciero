package vista;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

/** Paleta y componentes visuales compartidos por el acceso y el escritorio. */
final class TemaZeroLoss {
    static final Color TINTA = new Color(28, 43, 65);
    static final Color SUAVE = new Color(104, 119, 139);
    static final Color PROFUNDO = new Color(20, 46, 66);
    static final Color PETROLEO = new Color(0, 112, 119);
    static final Color ACENTO = new Color(0, 121, 128);
    static final Color TURQUESA = new Color(91, 212, 195);
    static final Color FONDO = new Color(244, 247, 251);
    enum TipoBoton { PRINCIPAL, SECUNDARIO, NAVEGACION }
    private TemaZeroLoss() { }

    static Graphics2D suave(Graphics g) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return g2;
    }

    static JPanel degradado(boolean oscuro) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) { pintarFondo(g, this, oscuro); }
        };
    }
    static void pintarFondo(Graphics g, Component c, boolean oscuro) {
        Graphics2D g2 = suave(g);
        g2.setPaint(new GradientPaint(0, 0, oscuro ? PROFUNDO : new Color(234, 244, 246),
                c.getWidth(), c.getHeight(), oscuro ? PETROLEO : FONDO));
        g2.fillRect(0, 0, c.getWidth(), c.getHeight());
        g2.setColor(oscuro ? new Color(255, 255, 255, 9) : new Color(221, 237, 240, 100));
        g2.fillOval(c.getWidth() - 150, -130, 300, 300);
        g2.drawOval(-140, c.getHeight() - 210, 350, 350);
        g2.dispose();
    }

    static JPanel tarjeta() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = suave(g);
                g2.setColor(new Color(28, 60, 72, 10));
                g2.fillRoundRect(0, 4, getWidth(), getHeight() - 4, 22, 22);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(1, 0, getWidth() - 2, getHeight() - 5, 22, 22);
                g2.setColor(new Color(225, 236, 239));
                g2.drawRoundRect(1, 0, getWidth() - 3, getHeight() - 6, 22, 22);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(22, 24, 25, 24));
        return panel;
    }

    static void boton(JButton boton, TipoBoton tipo) {
        boolean nav = tipo == TipoBoton.NAVEGACION;
        boton.putClientProperty("navegacion", nav);
        boton.setUI(new BasicButtonUI() {
            @Override public void paint(Graphics g, JComponent c) {
                JButton b = (JButton)c;
                Graphics2D g2 = suave(g);
                boolean activo = Boolean.TRUE.equals(b.getClientProperty("activo"));
                Color fondo = nav ? activo ? new Color(255, 255, 255, 24) : new Color(255, 255, 255, 0)
                        : tipo == TipoBoton.PRINCIPAL ? ACENTO : new Color(235, 245, 246);
                if (!b.isEnabled()) fondo = nav ? new Color(255, 255, 255, 3) : new Color(231, 237, 241);
                else if (b.getModel().isPressed()) fondo = nav ? new Color(255, 255, 255, 40) : tipo == TipoBoton.PRINCIPAL ? new Color(0, 96, 104) : new Color(195, 224, 227);
                else if (b.getModel().isRollover()) fondo = nav ? new Color(255, 255, 255, 18)
                        : tipo == TipoBoton.PRINCIPAL ? new Color(0, 139, 143) : new Color(217, 237, 239);
                g2.setColor(fondo);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 12, 12);
                if (nav && activo) {
                    g2.setColor(TURQUESA);
                    g2.fillRoundRect(0, 8, 3, c.getHeight() - 16, 3, 3);
                }
                if (b.hasFocus()) {
                    g2.setColor(nav || tipo == TipoBoton.PRINCIPAL ? TURQUESA : ACENTO);
                    g2.drawRoundRect(2, 2, c.getWidth() - 5, c.getHeight() - 5, 10, 10);
                }
                g2.dispose();
                super.paint(g, c);
            }
        });
        boton.setFont(new Font("Segoe UI", nav ? Font.PLAIN : Font.BOLD, 13));
        boton.setForeground(nav ? new Color(224, 240, 241) : tipo == TipoBoton.PRINCIPAL ? Color.WHITE : ACENTO);
        boton.setOpaque(false);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setRolloverEnabled(true);
        boton.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    static final class IconoMarca implements Icon {
        private final int tamano;
        IconoMarca(int tamano) { this.tamano = tamano; }
        public int getIconWidth() { return tamano; }
        public int getIconHeight() { return tamano; }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = suave(g);
            g2.translate(x, y);
            g2.scale(tamano / 104.0, tamano / 104.0);
            g2.setColor(new Color(255, 255, 255, 22));
            g2.fillRoundRect(0, 0, 104, 104, 30, 30);
            g2.setColor(TURQUESA);
            g2.fillRoundRect(24, 55, 12, 25, 5, 5);
            g2.fillRoundRect(46, 43, 12, 37, 5, 5);
            g2.fillRoundRect(68, 28, 12, 52, 5, 5);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawPolyline(new int[]{25, 46, 76}, new int[]{42, 29, 20}, 3);
            g2.drawLine(65, 18, 77, 20);
            g2.drawLine(77, 20, 73, 32);
            g2.dispose();
        }
    }
}
