package controlador;

import vista.contratos.*;
import servicio.ServicioVentas;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Solicita un ajuste con motivo; el servicio escribe total e historial atomicamente.
 */
public final class ModificarVentaControlador extends ControladorBase<VistaModificarVenta> {
    private final ServicioVentas ventas;

    // La busqueda permite revisar una venta antes de enviar el ajuste.
    public ModificarVentaControlador(VistaModificarVenta vista, ServicioVentas ventas, EjecutorTareas tareas) {
        super(vista, tareas);
        this.ventas = Objects.requireNonNull(ventas);
        vista.alAccionar(AccionVista.BUSCAR, this::buscarVenta);
        vista.alAccionar(AccionVista.MODIFICAR, this::modificar);
    }

    public void buscarVenta() {
        preparar(() -> {
            int id = entero(vista.getVentaId());
            ejecutar(() -> ventas.buscarVenta(id), vista::mostrarVenta);
        });
    }

    public void modificar() {
        preparar(() -> {
            int id = entero(vista.getVentaId());
            BigDecimal total = decimal(vista.getNuevoTotal());
            String motivo = vista.getMotivo();
            ejecutar(() -> ventas.modificarVenta(id, total, motivo), v -> {
                vista.mostrarVenta(v);
                vista.mostrarInformacion("Venta modificada y motivo registrado.");
                informarCambio();
            });
        });
    }
}
