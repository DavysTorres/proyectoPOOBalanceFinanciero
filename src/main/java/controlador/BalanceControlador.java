package controlador;

import vista.contratos.*;
import servicio.*;
import modelo.BalanceDia;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Carga un balance y exporta exactamente los datos que quedaron visibles.
 */
public final class BalanceControlador extends ControladorBase<VistaBalance> {
    private final ServicioBalance balance;
    private final ServicioExportacion exportacion;
    private BalanceDia balanceMostrado;

    // Se registran eventos de fecha y exportacion sin repetir formulas.
    public BalanceControlador(VistaBalance vista, ServicioBalance balance, ServicioExportacion exportacion, EjecutorTareas tareas) {
        super(vista, tareas);
        this.balance = Objects.requireNonNull(balance);
        this.exportacion = Objects.requireNonNull(exportacion);
        vista.alAccionar(AccionVista.CARGAR, this::cargarBalance);
        vista.alAccionar(AccionVista.EXPORTAR, this::exportar);
    }

    // La vista debe respetar isPuntoEquilibrioCalculable al presentar la meta.
    public void cargarBalance() {
        preparar(() -> {
            LocalDate fecha = fecha(vista.getFecha());
            balanceMostrado = null;
            vista.mostrarBalance(null);
            ejecutar(() -> balance.obtenerBalanceDelDia(fecha), b -> {
                balanceMostrado = b;
                vista.mostrarBalance(b);
            });
        });
    }

    // Cancelar el selector de archivo no intenta crear un archivo con una ruta vacia.
    public void exportar() {
        preparar(() -> {
            BalanceDia b = seleccionado(balanceMostrado);
            String ruta = vista.getRutaExportacion();
            if (ruta == null || ruta.isBlank()) return;
            ejecutar(() -> {
                        exportacion.exportar(b, ruta);
                        return null;
                    },
                    ignorado -> vista.mostrarInformacion("Informe CSV exportado."));
        });
    }
}
