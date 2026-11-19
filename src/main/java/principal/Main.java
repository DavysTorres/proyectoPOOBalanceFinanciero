package principal;

import javax.swing.SwingUtilities;
<<<<<<< HEAD
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
=======

public class Main {
    public static void main(String[] args) {
        // Todos los componentes Swing se construyen en el hilo de la interfaz.
        SwingUtilities.invokeLater(() -> new AplicacionDesktop().iniciar());
    }
}
>>>>>>> c6b38029e9d5f9f3c350cb56d301388f5b5859f1
