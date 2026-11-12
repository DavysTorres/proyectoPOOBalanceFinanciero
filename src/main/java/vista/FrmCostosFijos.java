package vista;

import modelo.CostoFijo;
import util.FormatoMoneda;
import vista.contratos.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public final class FrmCostosFijos extends FrmModulo implements VistaCostosFijos {
    private final JTextField concepto = texto("Concepto:", "");
    private final JTextField monto = texto("Monto mensual ($):", "");
    private final JCheckBox activo = campo("Estado:", new JCheckBox("Activo", true));
    private final DefaultTableModel modelo = EstiloZeroLoss.tabla("Número", "Concepto", "Monto mensual", "Activo");
    private final JTable tabla = tabla(modelo);
    private List<CostoFijo> costos = List.of();
    public FrmCostosFijos() {
        super("Costos fijos", "Administrador · Configure los costos mensuales del negocio.");
        cuerpo.add(new JScrollPane(tabla), BorderLayout.CENTER);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                CostoFijo c = costos.get(tabla.convertRowIndexToModel(tabla.getSelectedRow()));
                concepto.setText(c.getConcepto()); monto.setText(c.getMontoMensual().toPlainString()); activo.setSelected(c.isActivo());
            }
        });
        local("Limpiar", () -> { tabla.clearSelection(); concepto.setText(""); monto.setText(""); activo.setSelected(true); });
        accion("Crear", AccionVista.CREAR);
        accion("Guardar cambios", AccionVista.EDITAR);
        accion("Desactivar", AccionVista.DESACTIVAR);
        accion("Actualizar lista", AccionVista.LISTAR);
        estado.setText("Los costos activos se distribuyen según los días de operación configurados.");
    }
    @Override public Integer getIdSeleccionado() { return idSeleccionado(tabla); }
    @Override public String getConcepto() { return leer(concepto); }
    @Override public String getMontoMensual() { return leer(monto); }
    @Override public boolean isActivo() { return activo.isSelected(); }
    @Override public void mostrarCostos(List<CostoFijo> lista) {
        costos = new ArrayList<>(lista); modelo.setRowCount(0);
        for (CostoFijo c : lista) modelo.addRow(new Object[]{c.getId(), c.getConcepto(), FormatoMoneda.mostrar(c.getMontoMensual()), c.isActivo() ? "Sí" : "No"});
    }
}
