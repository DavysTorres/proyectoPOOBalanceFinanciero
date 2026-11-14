package util;

import java.awt.Component;
import javax.swing.JOptionPane;

/**
 * Dialogos comunes de Swing para mantener el mismo lenguaje en todas las ventanas.
 */
public final class Mensajes {
    private Mensajes() {
    }

    public static void error(Component padre, String texto) {
        JOptionPane.showMessageDialog(padre, texto, "Revise la operacion", JOptionPane.ERROR_MESSAGE);
    }

    public static void informacion(Component padre, String texto) {
        JOptionPane.showMessageDialog(padre, texto, "Balance diario", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirmar(Component padre, String texto) {
        return JOptionPane.showConfirmDialog(padre, texto, "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
