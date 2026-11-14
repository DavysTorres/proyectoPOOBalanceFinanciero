package vista;

import javax.swing.*;
import java.awt.*;

/** Comprueba cambios de columnas y altura sin servicios ni base de datos. */
public final class RevisionResponsiva {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel campos = new JPanel(new FormularioZeroLoss.Campos());
            for (int i = 0; i < 6; i++) {
                campos.add(new JLabel("Campo " + i));
                campos.add(new JTextField());
            }
            int altoEstrecho = 0;
            for (int ancho : new int[]{280, 640, 1280, 280}) {
                campos.setSize(ancho, 1000);
                campos.doLayout();
                int alto = campos.getPreferredSize().height;
                for (Component c : campos.getComponents()) {
                    if (c.getX() < 0 || c.getX() + c.getWidth() > ancho
                            || c.getY() + c.getHeight() > alto)
                        throw new AssertionError("Campo fuera del formulario a " + ancho);
                }
                if (ancho == 280) altoEstrecho = alto;
                else if (alto >= altoEstrecho) throw new AssertionError("No se reorganizaron las columnas");
            }
            JPanel tarjetas = new JPanel(new FormularioZeroLoss.Rejilla(290, 2, 18));
            for (int i = 0; i < 4; i++) tarjetas.add(new JLabel("Indicador"));
            tarjetas.setSize(320, 600);
            tarjetas.doLayout();
            if (tarjetas.getComponent(1).getY() <= tarjetas.getComponent(0).getY())
                throw new AssertionError("Las tarjetas deben apilarse");
            tarjetas.setSize(1000, 600);
            tarjetas.doLayout();
            if (tarjetas.getComponent(1).getY() != tarjetas.getComponent(0).getY())
                throw new AssertionError("Las tarjetas deben compartir fila");
            System.out.println("OK: campos y tarjetas se adaptan al reducir y ampliar el ancho");
        });
    }
}
