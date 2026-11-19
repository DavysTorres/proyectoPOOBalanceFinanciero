package vista;

import modelo.RegistroCambio;
import util.FormatoMoneda;
import vista.contratos.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public final class FrmHistorial extends FrmModulo implements VistaHistorial {
    private final JTextField fecha = texto("Fecha (AAAA-MM-DD):", LocalDate.now().toString());
    private final DefaultTableModel modelo = EstiloZeroLoss.tabla("Registro", "Venta", "Usuario", "Fecha y hora", "Anterior", "Nuevo", "Motivo");
    public FrmHistorial() {
        super("Historial de cambios", "Administrador y jefe · Consulta de ajustes de ventas.");
        cuerpo.add(new JScrollPane(tabla(modelo)), BorderLayout.CENTER);
        accion("Ver todo", AccionVista.CARGAR);
        JButton filtrar = accion("Filtrar por fecha", AccionVista.FILTRAR);
        fecha.addActionListener(e -> filtrar.doClick());
        setSize(1050, 620);
    }
    @Override public String getFecha() { return leer(fecha); }
    @Override public void mostrarRegistros(List<RegistroCambio> registros) {
        modelo.setRowCount(0);
        for (RegistroCambio r : registros) modelo.addRow(new Object[]{r.getId(), r.getVenta().getId(), r.getUsuario().getNombre(),
                r.getFechaHora(), FormatoMoneda.mostrar(r.getValorAnterior()), FormatoMoneda.mostrar(r.getValorNuevo()), r.getMotivo()});
        estado.setText(registros.isEmpty() ? "No hay cambios para esta consulta." : registros.size() + " cambios registrados.");
    }
}
