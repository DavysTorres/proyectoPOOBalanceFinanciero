package controlador;

import vista.contratos.*;
import servicio.ServicioCostosFijos;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Presentacion del catalogo de costos; el servicio y el modelo validan sus reglas.
 */
public final class CostoFijoControlador extends ControladorBase<VistaCostosFijos> {
    private final ServicioCostosFijos costos;

    // Registra crear, editar y baja logica; no existe eliminacion fisica.
    public CostoFijoControlador(VistaCostosFijos vista, ServicioCostosFijos costos, EjecutorTareas tareas) {
        super(vista, tareas);
        this.costos = Objects.requireNonNull(costos);
        vista.alAccionar(AccionVista.CREAR, this::crear);
        vista.alAccionar(AccionVista.EDITAR, this::editar);
        vista.alAccionar(AccionVista.DESACTIVAR, this::desactivar);
        vista.alAccionar(AccionVista.LISTAR, this::listar);
    }

    public void crear() {
        preparar(() -> {
            String concepto = vista.getConcepto();
            BigDecimal monto = decimal(vista.getMontoMensual());
            ejecutar(() -> costos.crear(concepto, monto), c -> {
                vista.mostrarInformacion("Costo fijo creado con numero " + c.getId());
                informarCambio();
            });
        });
    }

    public void editar() {
        preparar(() -> {
            int id = id(vista.getIdSeleccionado());
            String concepto = vista.getConcepto();
            BigDecimal monto = decimal(vista.getMontoMensual());
            boolean activo = vista.isActivo();
            ejecutar(() -> costos.editar(id, concepto, monto, activo), c -> {
                vista.mostrarInformacion("Costo fijo actualizado.");
                informarCambio();
            });
        });
    }

    public void desactivar() {
        preparar(() -> {
            int id = id(vista.getIdSeleccionado());
            ejecutar(() -> {
                costos.desactivar(id);
                return null;
            }, ignorado -> {
                vista.mostrarInformacion("Costo fijo desactivado.");
                informarCambio();
            });
        });
    }

    public void listar() {
        ejecutar(costos::listar, vista::mostrarCostos);
    }
}
