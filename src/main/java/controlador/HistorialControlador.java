package controlador;

import vista.contratos.*;
import servicio.ServicioHistorial;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Consulta evidencia sin exponer acciones de editar o eliminar historial.
 */
public final class HistorialControlador extends ControladorBase<VistaHistorial> {
    private final ServicioHistorial historial;

    // Las dos acciones tienen la autorizacion centralizada en el servicio.
    public HistorialControlador(VistaHistorial vista, ServicioHistorial historial, EjecutorTareas tareas) {
        super(vista, tareas);
        this.historial = Objects.requireNonNull(historial);
        vista.alAccionar(AccionVista.CARGAR, this::cargarHistorial);
        vista.alAccionar(AccionVista.FILTRAR, this::filtrarPorFecha);
    }

    public void cargarHistorial() {
        ejecutar(historial::listarTodos, vista::mostrarRegistros);
    }

    public void filtrarPorFecha() {
        preparar(() -> {
            LocalDate fecha = fecha(vista.getFecha());
            ejecutar(() -> historial.listarPorFecha(fecha), vista::mostrarRegistros);
        });
    }
}
