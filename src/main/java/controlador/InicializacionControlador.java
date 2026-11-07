package controlador;

import vista.contratos.*;
import servicio.ServicioInicializacion;
import modelo.Negocio;

import java.util.Arrays;
import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Completa el primer uso desde una ventana y sin pedir claves por consola.
 */
public final class InicializacionControlador extends ControladorBase<VistaInicializacion> {
    private final ServicioInicializacion inicializacion;
    private final Navegacion navegacion;

    public InicializacionControlador(VistaInicializacion vista, ServicioInicializacion inicializacion, Navegacion navegacion, EjecutorTareas tareas) {
        super(vista, tareas);
        this.inicializacion = inicializacion;
        this.navegacion = navegacion;
        vista.alAccionar(AccionVista.GUARDAR, this::guardar);
    }

    public void guardar() {
        preparar(() -> {
            String nombre = vista.getNombreAdministrador(), usuario = vista.getUsername(), negocio = vista.getNombre();
            int dias = entero(vista.getDiasOperacion());
            LocalTime apertura = hora(vista.getHoraApertura()), cierre = hora(vista.getHoraCierre());
            BigDecimal amarillo = decimal(vista.getUmbralAmarillo()), margen = decimal(vista.getUmbralMargenBajo());
            char[] copia = vista.getContrasena();
            String clave;
            try {
                clave = copia == null ? "" : new String(copia);
            } finally {
                if (copia != null) Arrays.fill(copia, '\0');
                vista.limpiarContrasena();
            }
            ejecutar(() -> inicializacion.inicializar(nombre, usuario, clave, new Negocio(negocio, dias, apertura, cierre, amarillo, margen)),
                    navegacion::abrirMenu);
        });
    }
}
