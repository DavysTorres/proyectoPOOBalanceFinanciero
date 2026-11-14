package vista;

import modelo.Negocio;
import vista.contratos.*;
import javax.swing.*;
import java.time.format.DateTimeFormatter;

public class FrmConfiguracion extends FrmModulo implements VistaConfiguracion {
    private final JTextField nombre = texto("Nombre del negocio:", "");
    private final JTextField dias = texto("Días de operación por mes:", "30");
    private final JTextField apertura = texto("Hora de apertura (HH:mm):", "08:00");
    private final JTextField cierre = texto("Hora de cierre (HH:mm):", "18:00");
    private final JTextField amarillo = texto("Avance para semáforo amarillo (%):", "70");
    private final JTextField margen = texto("Umbral de margen bajo (%):", "15");

    public FrmConfiguracion() {
        this("Configuración del negocio", "Administrador · Ajuste la operación y los umbrales del negocio.");
        accion("Cargar configuración", AccionVista.CARGAR);
        accion("Guardar configuración", AccionVista.GUARDAR);
    }
    protected FrmConfiguracion(String titulo, String descripcion) {
        super(titulo, descripcion);
        estado.setText("Importes y porcentajes sin separadores de miles. Horarios en formato de 24 horas.");
    }
    @Override public String getNombre() { return leer(nombre); }
    @Override public String getDiasOperacion() { return leer(dias); }
    @Override public String getHoraApertura() { return leer(apertura); }
    @Override public String getHoraCierre() { return leer(cierre); }
    @Override public String getUmbralAmarillo() { return leer(amarillo); }
    @Override public String getUmbralMargenBajo() { return leer(margen); }
    @Override public void mostrarNegocio(Negocio n) {
        if (n == null) return;
        nombre.setText(n.getNombre()); dias.setText(Integer.toString(n.getDiasOperacion()));
        apertura.setText(n.getHoraApertura().format(DateTimeFormatter.ofPattern("HH:mm")));
        cierre.setText(n.getHoraCierre().format(DateTimeFormatter.ofPattern("HH:mm")));
        amarillo.setText(n.getUmbralAmarillo().toPlainString()); margen.setText(n.getUmbralMargenBajo().toPlainString());
    }
}
