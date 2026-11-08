package vista.main;

import javax.swing.*;
import java.awt.*;

public class PanelSemaforo extends JPanel {
    private final JLabel lblIndicadorColor;
    private final JLabel lblMensaje;
    private final JProgressBar barraProgreso;

    public PanelSemaforo() {
        setLayout(new BorderLayout(10, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Círculo de color (Semaforo)
        lblIndicadorColor = new JLabel("   ");
        lblIndicadorColor.setOpaque(true);
        lblIndicadorColor.setBackground(Color.LIGHT_GRAY); // Color por defecto
        lblIndicadorColor.setPreferredSize(new Dimension(20, 20));
        lblIndicadorColor.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        // Mensaje de estado
        lblMensaje = new JLabel("Calculando balance...");

        // Barra de progreso
        barraProgreso = new JProgressBar(0, 100);
        barraProgreso.setStringPainted(true);

        add(lblIndicadorColor, BorderLayout.WEST);
        add(lblMensaje, BorderLayout.CENTER);
        add(barraProgreso, BorderLayout.EAST);
    }

    // Métodos para que el controlador actualice los valores
    public void actualizarEstado(Color color, String mensaje, int porcentaje) {
        lblIndicadorColor.setBackground(color);
        lblMensaje.setText(mensaje);
        barraProgreso.setValue(porcentaje);
    }
}
