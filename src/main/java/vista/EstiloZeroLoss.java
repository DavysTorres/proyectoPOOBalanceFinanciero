package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Elementos comunes de los formularios proporcionados. */
final class EstiloZeroLoss {
    private EstiloZeroLoss() { }

    static JButton botonMenu(String texto) {
        JButton boton = new JButton(texto) {
            @Override protected void paintComponent(Graphics g) {
                if (Boolean.TRUE.equals(getClientProperty("navegacion"))) {
                    super.paintComponent(g);
                    return;
                }
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isArmed() ? new Color(220, 220, 220)
                        : getModel().isRollover() ? new Color(240, 240, 240) : Color.WHITE);
                g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
                g2.setColor(isEnabled() ? new Color(158, 97, 49) : Color.LIGHT_GRAY);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setForeground(new Color(25, 48, 80));
        boton.setFocusPainted(false);
        boton.setContentAreaFilled(false);
        boton.setBorderPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    static DefaultTableModel tabla(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
    }

    static void campo(JPanel panel, String texto, JComponent componente) {
        JLabel label = new JLabel(texto);
        label.setLabelFor(componente);
        panel.add(label);
        panel.add(componente);
    }
}
