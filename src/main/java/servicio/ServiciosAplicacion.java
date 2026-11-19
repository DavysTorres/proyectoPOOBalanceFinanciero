package servicio;

import dao.*;

import java.time.Clock;
import java.util.Objects;

/**
 * Composicion de la aplicacion: una instancia compartida por todos los controladores.
 */
public final class ServiciosAplicacion {
    // Cada responsabilidad tiene su servicio; no se crea otro Singleton de conexion o de servicios.
    private final ServicioAutenticacion autenticacion;
    private final ServicioUsuarios usuarios;
    private final ServicioProductos productos;
    private final ServicioCostosFijos costosFijos;
    private final ServicioConfiguracion configuracion;
    private final ServicioBalance balance;
    private final ServicioCierre cierre;
    private final ServicioVentas ventas;
    private final ServicioCompras compras;
    private final ServicioHistorial historial;
    private final AnalizadorMargen margen;
    private final ServicioExportacion exportacion;
    private final ServicioInicializacion inicializacion;

    // Construccion normal para la interfaz, que no consulta MySQL hasta la primera accion.
    public ServiciosAplicacion() {
        this(new UsuarioDAO(), new ProductoDAO(), new VentaDAO(), new CompraDAO(), new GastoDAO(),
                new CostoFijoDAO(), new ConfiguracionDAO(), new CierreDiarioDAO(), new HistorialCambiosDAO(),
                Clock.systemDefaultZone());
    }

    // Las pruebas suministran DAO con una misma conexion temporal y un reloj controlado.
    public ServiciosAplicacion(UsuarioDAO usuarioDAO, ProductoDAO productoDAO, VentaDAO ventaDAO, CompraDAO compraDAO,
                               GastoDAO gastoDAO, CostoFijoDAO costoDAO, ConfiguracionDAO configuracionDAO,
                               CierreDiarioDAO cierreDAO, HistorialCambiosDAO historialDAO, Clock reloj) {
        Objects.requireNonNull(reloj);
        autenticacion = new ServicioAutenticacion(usuarioDAO, reloj);
        usuarios = new ServicioUsuarios(usuarioDAO, autenticacion, reloj);
        productos = new ServicioProductos(productoDAO, reloj);
        costosFijos = new ServicioCostosFijos(costoDAO, reloj);
        configuracion = new ServicioConfiguracion(configuracionDAO, reloj);
        balance = new ServicioBalance(ventaDAO, gastoDAO, costoDAO, configuracionDAO, productoDAO, cierreDAO, reloj);
        cierre = new ServicioCierre(cierreDAO, ventaDAO, compraDAO, gastoDAO, balance, reloj);
        ventas = new ServicioVentas(ventaDAO, productoDAO, historialDAO, cierre, reloj);
        compras = new ServicioCompras(compraDAO, gastoDAO, productoDAO, costoDAO, cierre, reloj);
        historial = new ServicioHistorial(historialDAO, reloj);
        margen = new AnalizadorMargen(productoDAO, reloj);
        exportacion = new ServicioExportacion();
        inicializacion = new ServicioInicializacion(usuarioDAO, usuarios, configuracion);
    }

    // Comparte el servicio de autenticacion sin reconstruir sus dependencias ni perder su estado.
    public ServicioAutenticacion getAutenticacion() {
        return autenticacion;
    }

    // Comparte el servicio de usuarios sin reconstruir sus dependencias ni perder su estado.
    public ServicioUsuarios getUsuarios() {
        return usuarios;
    }

    // Comparte el servicio de productos sin reconstruir sus dependencias ni perder su estado.
    public ServicioProductos getProductos() {
        return productos;
    }

    // Comparte el servicio de costosFijos sin reconstruir sus dependencias ni perder su estado.
    public ServicioCostosFijos getCostosFijos() {
        return costosFijos;
    }

    // Comparte el servicio de configuracion sin reconstruir sus dependencias ni perder su estado.
    public ServicioConfiguracion getConfiguracion() {
        return configuracion;
    }

    // Comparte el servicio de balance sin reconstruir sus dependencias ni perder su estado.
    public ServicioBalance getBalance() {
        return balance;
    }

    // Comparte el servicio de cierre sin reconstruir sus dependencias ni perder su estado.
    public ServicioCierre getCierre() {
        return cierre;
    }

    // Comparte el servicio de ventas sin reconstruir sus dependencias ni perder su estado.
    public ServicioVentas getVentas() {
        return ventas;
    }

    // Comparte el servicio de compras sin reconstruir sus dependencias ni perder su estado.
    public ServicioCompras getCompras() {
        return compras;
    }

    // Comparte el servicio de historial sin reconstruir sus dependencias ni perder su estado.
    public ServicioHistorial getHistorial() {
        return historial;
    }

    // Comparte el servicio de margen sin reconstruir sus dependencias ni perder su estado.
    public AnalizadorMargen getMargen() {
        return margen;
    }

    // Comparte el servicio de exportacion sin reconstruir sus dependencias ni perder su estado.
    public ServicioExportacion getExportacion() {
        return exportacion;
    }

    public ServicioInicializacion getInicializacion() {
        return inicializacion;
    }

    // Una sesion nueva nunca hereda el borrador de venta de otra persona.
    public void cerrarSesion() {
        ventas.descartarCarritoAlCerrarSesion();
        autenticacion.cerrarSesion();
    }
}
