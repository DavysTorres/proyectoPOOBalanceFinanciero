package vista;

import vista.contratos.AccionVista;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.*;

/** Cabecera, campos y acciones comunes de los módulos operativos y administrativos. */
abstract class FrmModulo extends FrmBase {
    protected final JPanel cuerpo = new JPanel(new BorderLayout(10, 12));
    protected final JPanel campos = new JPanel(new GridLayout(0, 2, 10, 8));
    protected final JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
    protected final JLabel estado = new JLabel(" ");
    private final java.util.List<JComponent> editores = new ArrayList<>();
    private final Map<JComponent, Boolean> habilitados = new IdentityHashMap<>();
    private final JButton volver = new JButton("\u2190");

    protected FrmModulo(String titulo, String descripcion) {
        super(titulo + " - ZeroLoss");
        JPanel principal = new JPanel(new BorderLayout(10, 15));
        principal.setBackground(Color.WHITE);
        principal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JPanel cabecera = new JPanel(new BorderLayout(10, 4));
        cabecera.setOpaque(false);
        volver.setToolTipText("Volver al menú");
        volver.setFocusPainted(false);
        volver.addActionListener(e -> solicitarCierre());
        cabecera.add(volver, BorderLayout.WEST);
        JLabel label = new JLabel(titulo);
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        label.setForeground(new Color(25, 48, 80));
        cabecera.add(label, BorderLayout.CENTER);
        cabecera.add(new JLabel(descripcion), BorderLayout.SOUTH);
        cabecera.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(158, 97, 49)));
        cuerpo.setOpaque(false);
        campos.setOpaque(false);
        acciones.setOpaque(false);
        cuerpo.add(campos, BorderLayout.NORTH);
        JPanel pie = new JPanel(new BorderLayout(5, 5));
        pie.setOpaque(false);
        pie.add(estado, BorderLayout.NORTH);
        pie.add(acciones, BorderLayout.SOUTH);
        principal.add(cabecera, BorderLayout.NORTH);
        principal.add(cuerpo, BorderLayout.CENTER);
        principal.add(pie, BorderLayout.SOUTH);
        setContentPane(principal);
        setMinimumSize(new Dimension(740, 560));
        setSize(850, 640);
        setLocationRelativeTo(null);
    }

    protected final <T extends JComponent> T campo(String etiqueta, T editor) {
        EstiloZeroLoss.campo(campos, etiqueta, editor);
        editores.add(editor);
        return editor;
    }
    protected final JTextField texto(String etiqueta, String inicial) { return campo(etiqueta, new JTextField(inicial)); }
    protected final JButton accion(String texto, AccionVista accion) {
        JButton boton = EstiloZeroLoss.botonMenu(texto);
        registrarBoton(accion, boton);
        acciones.add(boton);
        return boton;
    }
    protected final JButton local(String texto, Runnable manejar) {
        JButton boton = EstiloZeroLoss.botonMenu(texto);
        boton.addActionListener(e -> { if (!estaOcupada()) manejar.run(); });
        acciones.add(boton);
        editores.add(boton);
        return boton;
    }
    protected final JTable tabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setFillsViewportHeight(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setRowHeight(26);
        editores.add(tabla);
        return tabla;
    }
    protected static String leer(JTextField campo) { return campo.getText().trim(); }
    protected static Integer idSeleccionado(JTable tabla) {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? null : (Integer)tabla.getModel().getValueAt(tabla.convertRowIndexToModel(fila), 0);
    }
    @Override public void setOcupada(boolean valor) {
        boolean anterior = estaOcupada();
        super.setOcupada(valor);
        volver.setEnabled(!valor);
        if (valor && !anterior) {
            for (JComponent editor : editores) { habilitados.put(editor, editor.isEnabled()); editor.setEnabled(false); }
        } else if (!valor && anterior) {
            habilitados.forEach(JComponent::setEnabled);
            habilitados.clear();
        }
    }
}
