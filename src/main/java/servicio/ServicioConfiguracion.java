package servicio;

import dao.ConfiguracionDAO;
import modelo.*;

import java.time.Clock;
import java.util.Objects;

/**
 * Controla el acceso administrativo a la configuracion validada por Negocio.
 */
public class ServicioConfiguracion extends ServicioBase {
    private final ConfiguracionDAO configuracion;

    // Reutiliza el DAO responsable de guardar el conjunto de claves atomicamente.
    public ServicioConfiguracion() {
        this(new ConfiguracionDAO(), Clock.systemDefaultZone());
    }

    public ServicioConfiguracion(ConfiguracionDAO configuracion, Clock reloj) {
        super(reloj);
        this.configuracion = Objects.requireNonNull(configuracion);
    }

    // Solo el administrador puede consultar y modificar esta seccion.
    public Negocio obtenerNegocio() {
        sesion.exigirPermiso(Permiso.CONFIGURAR);
        return configuracion.cargarNegocio();
    }

    public void guardarNegocio(Negocio negocio) {
        sesion.exigirPermiso(Permiso.CONFIGURAR);
        configuracion.guardarNegocio(Objects.requireNonNull(negocio));
    }
}
