package principal;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Todos los componentes Swing se construyen en el hilo de la interfaz.
        SwingUtilities.invokeLater(() -> new AplicacionDesktop().iniciar());
    }
}
