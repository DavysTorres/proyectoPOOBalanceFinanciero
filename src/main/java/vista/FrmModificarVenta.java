package vista;

import modelo.*;
import util.FormatoMoneda;
import vista.contratos.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Revisión de una venta registrada y ajuste de su total con motivo obligatorio. */
public final class FrmModificarVenta extends FrmModulo implements VistaModificarVenta {
    private final JTextField ventaId = texto("Número de venta:", "");
    private final JTextField nuevoTotal = texto("Nuevo total ($):", "");
    private final JTextArea motivo = campo("Motivo del ajuste:", new JTextArea(3, 25));
    private final JLabel resumen = campo("Venta encontrada:", new JLabel("Busque una venta para revisar sus datos."));
    private final DefaultTableModel modelo = EstiloZeroLoss.tabla("Producto", "Cantidad", "Precio U.", "Subtotal");
    private final JButton modificar;
    private Integer idMostrado;

    public FrmModificarVenta() {
        super("Modificar venta", "Administrador y jefe · Consulte la venta antes de ajustar su total.");
        motivo.setLineWrap(true);
        motivo.setWrapStyleWord(true);
        motivo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(125, 145, 165)),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        motivo.setToolTipText("Explique el motivo del ajuste; este dato es obligatorio.");
        nuevoTotal.setToolTipText("Importe sin separadores de miles");
        cuerpo.add(new JScrollPane(tabla(modelo)), BorderLayout.CENTER);
        JButton buscar = accion("Buscar venta", AccionVista.BUSCAR);
        modificar = accion("Guardar ajuste", AccionVista.MODIFICAR);
        ventaId.addActionListener(e -> buscar.doClick());
        ventaId.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { invalidar(); }
            public void removeUpdate(DocumentEvent e) { invalidar(); }
            public void changedUpdate(DocumentEvent e) { invalidar(); }
        });
        invalidar();
        estado.setText("El ajuste conserva los productos y registra el total anterior, el nuevo y el motivo.");
    }
    private void invalidar() {
        idMostrado = null;
        modelo.setRowCount(0);
        resumen.setText("Busque una venta para revisar sus datos.");
        nuevoTotal.setText("");
        motivo.setText("");
        modificar.setEnabled(false);
    }
    @Override public String getVentaId() { return leer(ventaId); }
    @Override public String getNuevoTotal() { return leer(nuevoTotal); }
    @Override public String getMotivo() { return motivo.getText().trim(); }
    @Override public void mostrarVenta(Venta venta) {
        modelo.setRowCount(0);
        if (venta == null) { invalidar(); resumen.setText("No se encontró la venta."); return; }
        idMostrado = venta.getId();
        resumen.setText("#" + venta.getId() + " · " + venta.getFechaHora().toLocalDate() + " · Total: " + FormatoMoneda.mostrar(venta.getTotal()));
        nuevoTotal.setText(venta.getTotal().toPlainString());
        for (DetalleVenta d : venta.getDetalles()) modelo.addRow(new Object[]{d.getProducto().getNombre(), d.getCantidad(),
                FormatoMoneda.mostrar(d.getPrecioUnitario()), FormatoMoneda.mostrar(d.calcularSubtotal())});
        modificar.setEnabled(!estaOcupada());
    }
    @Override public void setOcupada(boolean ocupada) {
        super.setOcupada(ocupada);
        modificar.setEnabled(!ocupada && idMostrado != null && idMostrado.toString().equals(getVentaId()));
    }
}
