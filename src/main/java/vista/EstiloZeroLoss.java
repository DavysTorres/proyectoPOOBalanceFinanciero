package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Elementos comunes de los formularios proporcionados. */
final class EstiloZeroLoss {
    private EstiloZeroLoss() { }

    static JButton botonMenu(String texto) {
        JButton boton = new JButton(texto);
        TemaZeroLoss.boton(boton, TemaZeroLoss.TipoBoton.SECUNDARIO);
        return boton;
    }

    static DefaultTableModel tabla(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int fila, int columna) { return false; }
        };
    }

    static void campo(JPanel panel, String texto, JComponent componente) {
        if (!(panel.getLayout() instanceof FormularioZeroLoss.Campos)) panel.setLayout(new FormularioZeroLoss.Campos());
        JLabel label = new JLabel(texto);
        label.setLabelFor(componente);
        panel.add(label);
        panel.add(componente);
    }
}
