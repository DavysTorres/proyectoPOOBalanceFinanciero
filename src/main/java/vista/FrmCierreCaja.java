package vista;

import modelo.CierreDiario;
import util.FormatoMoneda;
import vista.contratos.*;
import javax.swing.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class FrmCierreCaja extends FrmModulo implements VistaCierre {
    private final JTextField fecha = texto("Fecha (AAAA-MM-DD):", LocalDate.now().toString());
    private final JLabel esperado = campo("Efectivo del sistema:", new JLabel("Sin consultar"));
    private final JTextField contado = texto("Efectivo contado ($):", "");
    private final JLabel diferencia = campo("Diferencia del cierre:", new JLabel("—"));
    public FrmCierreCaja() {
        super("Cierre de caja", "Administrador y jefe · Compare el efectivo contado con el registrado.");
        accion("Consultar efectivo", AccionVista.CALCULAR);
        accion("Cerrar jornada", AccionVista.CERRAR);
        estado.setText("Al cerrar la jornada se bloquean nuevos movimientos y ajustes para esa fecha.");
        fecha.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void limpiar() { esperado.setText("Sin consultar"); diferencia.setText("—"); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { limpiar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { limpiar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { limpiar(); }
        });
    }
    @Override public String getFecha() { return leer(fecha); }
    @Override public String getEfectivoContado() { return leer(contado); }
    @Override public void mostrarEfectivoSistema(BigDecimal efectivo) { esperado.setText(FormatoMoneda.mostrar(efectivo)); }
    @Override public void mostrarCierre(CierreDiario cierre) {
        mostrarEfectivoSistema(cierre.getEfectivoSistema());
        contado.setText(cierre.getEfectivoContado().toPlainString());
        diferencia.setText(FormatoMoneda.mostrar(cierre.getDiferencia()));
        estado.setText("Jornada del " + cierre.getFecha() + " cerrada por " + cierre.getUsuario().getNombre() + ".");
    }
}
