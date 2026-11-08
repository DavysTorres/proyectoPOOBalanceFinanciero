package principal;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        
        // Opcional: Activar el diseño nativo de Windows para que se vea más moderno
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            System.out.println("No se pudo aplicar el tema nativo.");
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // 1. Instanciar y mostrar UNICAMENTE la ventana de Login al arrancar
                FrmLogin login = new FrmLogin();
                login.setVisible(true);
            }
        });
    }
}