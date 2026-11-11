package controlador;

import servicio.ServiciosAplicacion;
import vista.contratos.*;

import java.util.Objects;

/**
 * Composicion: comparte servicios y una sola cola de trabajo entre todas las ventanas.
 */
public final class AplicacionControladores implements AutoCloseable {
    private final ServiciosAplicacion servicios;
    private final EjecutorTareas tareas;
    private Runnable alCambiarDatos = () -> {
    };

    public void alCambiarDatos(Runnable accion) {
        alCambiarDatos = Objects.requireNonNull(accion);
    }

    private <T extends ControladorBase<?>> T registrar(T controlador) {
        controlador.alCambiarDatos(() -> alCambiarDatos.run());
        return controlador;
    }

    // Se crea una instancia en el inicio de la aplicacion, no una por boton.
    public AplicacionControladores(ServiciosAplicacion servicios) {
        this(servicios, new EjecutorSwing());
    }

    // Las pruebas reciben un ejecutor inmediato y servicios con una base temporal.
    public AplicacionControladores(ServiciosAplicacion servicios, EjecutorTareas tareas) {
        this.servicios = Objects.requireNonNull(servicios);
        this.tareas = Objects.requireNonNull(tareas);
    }

    // Conecta una vista nueva a su servicio; no abre ventanas ni conexiones.
    public LoginControlador crearLogin(VistaLogin vista, Navegacion navegacion) {
        return registrar(new LoginControlador(vista, servicios.getAutenticacion(), navegacion, tareas));
    }

    // Conecta una vista nueva a su servicio; no abre ventanas ni conexiones.
    public PrincipalControlador crearPrincipal(VistaPrincipal vista, Navegacion navegacion) {
        return registrar(new PrincipalControlador(vista, servicios, navegacion, tareas));
    }

    // Conecta una vista nueva a su servicio; no abre ventanas ni conexiones.
    public VentaControlador crearVenta(VistaVenta vista) {
        return registrar(new VentaControlador(vista, servicios.getVentas(), servicios.getProductos(), tareas));
    }

    // Conecta una vista nueva a su servicio; no abre ventanas ni conexiones.
    public CompraGastoControlador crearCompraGasto(VistaCompraGasto vista) {
        return registrar(new CompraGastoControlador(vista, servicios.getCompras(), servicios.getProductos(), tareas));
    }

    // Conecta una vista nueva a su servicio; no abre ventanas ni conexiones.
    public BalanceControlador crearBalance(VistaBalance vista) {
        return registrar(new BalanceControlador(vista, servicios.getBalance(), servicios.getExportacion(), tareas));
    }

    // Conecta una vista nueva a su servicio; no abre ventanas ni conexiones.
    public MargenControlador crearMargen(VistaMargen vista) {
        return registrar(new MargenControlador(vista, servicios.getMargen(), servicios.getProductos(), tareas));
    }

    // Conecta una vista nueva a su servicio; no abre ventanas ni conexiones.
    public HistorialControlador crearHistorial(VistaHistorial vista) {
        return registrar(new HistorialControlador(vista, servicios.getHistorial(), tareas));
    }

    // Conecta una vista nueva a su servicio; no abre ventanas ni conexiones.
    public ModificarVentaControlador crearModificarVenta(VistaModificarVenta vista) {
        return registrar(new ModificarVentaControlador(vista, servicios.getVentas(), tareas));
    }

    // Conecta una vista nueva a su servicio; no abre ventanas ni conexiones.
    public CierreControlador crearCierre(VistaCierre vista) {
        return registrar(new CierreControlador(vista, servicios.getCierre(), tareas));
    }

    // Conecta una vista nueva a su servicio; no abre ventanas ni conexiones.
    public UsuarioControlador crearUsuario(VistaUsuarios vista) {
        return registrar(new UsuarioControlador(vista, servicios.getUsuarios(), servicios.getAutenticacion(), tareas));
    }

    // Conecta una vista nueva a su servicio; no abre ventanas ni conexiones.
    public ProductoControlador crearProducto(VistaProductos vista) {
        return registrar(new ProductoControlador(vista, servicios.getProductos(), tareas));
    }

    // Conecta una vista nueva a su servicio; no abre ventanas ni conexiones.
    public CostoFijoControlador crearCostoFijo(VistaCostosFijos vista) {
        return registrar(new CostoFijoControlador(vista, servicios.getCostosFijos(), tareas));
    }

    // Conecta una vista nueva a su servicio; no abre ventanas ni conexiones.
    public ConfiguracionControlador crearConfiguracion(VistaConfiguracion vista) {
        return registrar(new ConfiguracionControlador(vista, servicios.getConfiguracion(), tareas));
    }

    // El primer administrador tambien se crea a traves de un controlador.
    public InicializacionControlador crearInicializacion(VistaInicializacion vista, Navegacion navegacion) {
        return registrar(new InicializacionControlador(vista, servicios.getInicializacion(), navegacion, tareas));
    }

    // Al salir, deja terminar el trabajo aceptado; no interrumpe transacciones a mitad.
    @Override
    public void close() {
        if (tareas instanceof AutoCloseable) {
            try {
                ((AutoCloseable) tareas).close();
            } catch (Exception e) {
                throw new IllegalStateException("No se pudo cerrar la cola de trabajo", e);
            }
        }
    }
}
