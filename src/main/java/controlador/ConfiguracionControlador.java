package controlador;

import vista.contratos.*;
import servicio.ServicioConfiguracion;
import modelo.Negocio;

import java.time.LocalTime;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Convierte el formulario de configuracion a Negocio sin repetir sus validaciones.
 */
public final class ConfiguracionControlador extends ControladorBase<VistaConfiguracion> {
    private final ServicioConfiguracion configuracion;

    // Guardar el objeto completo permite que el DAO mantenga la operacion atomica.
    public ConfiguracionControlador(VistaConfiguracion vista, ServicioConfiguracion configuracion, EjecutorTareas tareas) {
        super(vista, tareas);
        this.configuracion = Objects.requireNonNull(configuracion);
        vista.alAccionar(AccionVista.CARGAR, this::cargar);
        vista.alAccionar(AccionVista.GUARDAR, this::guardar);
    }

    public void cargar() {
        ejecutar(configuracion::obtenerNegocio, vista::mostrarNegocio);
    }

    public void guardar() {
        preparar(() -> {
            String nombre = vista.getNombre();
            int dias = entero(vista.getDiasOperacion());
            LocalTime apertura = hora(vista.getHoraApertura()), cierre = hora(vista.getHoraCierre());
            BigDecimal amarillo = decimal(vista.getUmbralAmarillo()), margen = decimal(vista.getUmbralMargenBajo());
            ejecutar(() -> {
                Negocio negocio = new Negocio(nombre, dias, apertura, cierre, amarillo, margen);
                configuracion.guardarNegocio(negocio);
                return negocio;
            }, n -> {
                vista.mostrarNegocio(n);
                vista.mostrarInformacion("Configuracion guardada.");
                informarCambio();
            });
        });
    }
}
