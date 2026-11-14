package vista;

import servicio.*;
import javax.swing.*;
import java.awt.*;
import java.math.RoundingMode;

/** Estado y avance compartidos por los perfiles, sin cifras monetarias. */
public final class PanelSemaforo extends JPanel {
    private final JLabel lblIndicadorColor = new JLabel("\u25cf") {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getForeground());
            g2.fillOval(2, (getHeight() - 18) / 2, 18, 18);
            g2.dispose();
        }
    };
    private final JTextArea lblMensaje = new JTextArea("Calculando balance...", 2, 30);
    private final JProgressBar barraProgreso = new JProgressBar(0, 100);

    public PanelSemaforo() {
        super(new BorderLayout(12, 16));
        setOpaque(false);
        lblMensaje.setEditable(false);
        lblMensaje.setFocusable(false);
        lblMensaje.setLineWrap(true);
        lblMensaje.setWrapStyleWord(true);
        lblMensaje.setOpaque(false);
        lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMensaje.setForeground(new Color(53, 69, 91));
        barraProgreso.setFont(new Font("Segoe UI", Font.BOLD, 12));
        barraProgreso.setForeground(new Color(40, 141, 151));
        barraProgreso.setBackground(new Color(234, 240, 246));
        barraProgreso.setBorderPainted(false);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        lblIndicadorColor.setForeground(Color.LIGHT_GRAY);
        lblIndicadorColor.setPreferredSize(new Dimension(24, 24));
        barraProgreso.setStringPainted(true);
        barraProgreso.setString("Sin datos");
        barraProgreso.setPreferredSize(new Dimension(150, 24));
        add(lblIndicadorColor, BorderLayout.WEST);
        add(lblMensaje, BorderLayout.CENTER);
        add(barraProgreso, BorderLayout.SOUTH);
    }

    public void sinDatos(String mensaje) {
        lblIndicadorColor.setForeground(Color.LIGHT_GRAY);
        lblMensaje.setText(mensaje);
        barraProgreso.setValue(0);
        barraProgreso.setString("Sin datos");
    }

    public void actualizarEstado(Color color, String mensaje, int porcentaje) {
        lblIndicadorColor.setForeground(color);
        barraProgreso.setForeground(color);
        lblIndicadorColor.getAccessibleContext().setAccessibleName(mensaje);
        lblMensaje.setText(mensaje);
        lblMensaje.setToolTipText(mensaje);
        barraProgreso.setValue(Math.max(0, Math.min(100, porcentaje)));
        barraProgreso.setString(Math.max(0, porcentaje) + " %");
    }

    public void mostrarEstado(EstadoNegocio estado) {
        if (estado == null) {
            actualizarEstado(Color.LIGHT_GRAY, "Sin datos", 0);
        } else actualizarEstado(Color.decode(estado.getColor()), estado.getTitulo(), 0);
        barraProgreso.setString("Sin datos de avance");
    }

    public void mostrarResumen(ResumenSemaforo resumen) {
        if (resumen == null) { mostrarEstado(null); return; }
        mostrarEstado(resumen.getEstado());
        lblMensaje.setText(resumen.getMensaje());
        lblMensaje.setToolTipText(resumen.getMensaje());
        lblIndicadorColor.getAccessibleContext().setAccessibleName(resumen.getMensaje());
        if (!resumen.isCalculable()) {
            barraProgreso.setValue(0);
            barraProgreso.setString("No calculable");
        } else {
            barraProgreso.setValue(resumen.getPorcentaje().max(java.math.BigDecimal.ZERO)
                    .min(java.math.BigDecimal.valueOf(100)).intValue());
            barraProgreso.setString(resumen.getPorcentaje().setScale(2, RoundingMode.HALF_UP).toPlainString() + " %");
        }
    }
}
