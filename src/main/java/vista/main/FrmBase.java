package vista.main;

import javax.swing.*;
import java.awt.*;

public class FrmBase extends JFrame {
    public FrmBase(String titulo) {
        setTitle(titulo + " - ZeroLoss");
        getContentPane().setBackground(new Color(245, 245, 245));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Configuración de fuente global para la aplicación
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
    }
}
