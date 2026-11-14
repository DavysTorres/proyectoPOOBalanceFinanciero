package controlador;

import vista.contratos.*;
import servicio.ServicioCierre;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Presenta el efectivo esperado y envia el conteo sin calcular diferencias en la vista.
 */
public final class CierreControlador extends ControladorBase<VistaCierre> {
    private final ServicioCierre cierre;

    // La fecha y el conteo son textos que se convierten antes de ejecutar trabajo JDBC.
    public CierreControlador(VistaCierre vista, ServicioCierre cierre, EjecutorTareas tareas) {
        super(vista, tareas);
        this.cierre = Objects.requireNonNull(cierre);
        vista.alAccionar(AccionVista.CALCULAR, this::calcularEfectivoSistema);
        vista.alAccionar(AccionVista.CERRAR, this::cerrar);
    }

    public void calcularEfectivoSistema() {
        preparar(() -> {
            LocalDate fecha = fecha(vista.getFecha());
            ejecutar(() -> cierre.calcularEfectivoSistema(fecha), vista::mostrarEfectivoSistema);
        });
    }

    public void cerrar() {
        preparar(() -> {
            LocalDate fecha = fecha(vista.getFecha());
            BigDecimal contado = decimal(vista.getEfectivoContado());
            ejecutar(() -> cierre.cerrarDia(fecha, contado), c -> {
                vista.mostrarCierre(c);
                vista.mostrarInformacion("Jornada cerrada.");
                informarCambio();
            });
        });
    }
}
