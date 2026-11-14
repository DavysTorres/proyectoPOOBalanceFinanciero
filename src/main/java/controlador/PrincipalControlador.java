package controlador;

import vista.contratos.*;
import servicio.*;
import modelo.Permiso;

import java.util.Objects;

/**
 * Controla menu, semaforo y cierre de sesion sin cargar ventanas concretas.
 */
public final class PrincipalControlador extends ControladorBase<VistaPrincipal> {
    private final ServiciosAplicacion servicios;
    private final Navegacion navegacion;

    // Los botones se conectan una vez al construir el controlador.
    public PrincipalControlador(VistaPrincipal vista, ServiciosAplicacion servicios, Navegacion navegacion, EjecutorTareas tareas) {
        super(vista, tareas);
        this.servicios = Objects.requireNonNull(servicios);
        this.navegacion = Objects.requireNonNull(navegacion);
        vista.alAccionar(AccionVista.ACTUALIZAR, this::cargarSemaforo);
        vista.alAccionar(AccionVista.CERRAR_SESION, this::cerrarSesion);
        vista.alAccionar(AccionVista.ABRIR_VENTA, this::abrirVenta);
        vista.alAccionar(AccionVista.ABRIR_BALANCE, this::abrirBalance);
        vista.alAccionar(AccionVista.ABRIR_COMPRA_GASTO, this::abrirCompraGasto);
        vista.alAccionar(AccionVista.ABRIR_MARGEN, this::abrirMargen);
        vista.alAccionar(AccionVista.ABRIR_HISTORIAL, this::abrirHistorial);
        vista.alAccionar(AccionVista.ABRIR_MODIFICAR_VENTA, this::abrirModificarVenta);
        vista.alAccionar(AccionVista.ABRIR_CIERRE, this::abrirCierre);
        vista.alAccionar(AccionVista.ABRIR_USUARIOS, this::abrirUsuarios);
        vista.alAccionar(AccionVista.ABRIR_PRODUCTOS, this::abrirProductos);
        vista.alAccionar(AccionVista.ABRIR_COSTOS_FIJOS, this::abrirCostosFijos);
        vista.alAccionar(AccionVista.ABRIR_CONFIGURACION, this::abrirConfiguracion);
        vista.alAccionar(AccionVista.ABRIR_REPORTES, this::abrirReportes);
    }

    // Solo se entrega un estado, para que el cajero no reciba las cifras financieras restringidas.
    public void cargarSemaforo() {
        preparar(() -> {
            vista.iniciarCargaResumen();
            ejecutar(() -> {
                try {
                    var usuario = Sesion.getInstancia().getUsuarioActual();
                    modelo.BalanceDia balance = usuario != null && usuario.tienePermiso(Permiso.VER_BALANCE)
                            ? servicios.getBalance().obtenerBalanceDelDia(java.time.LocalDate.now()) : null;
                    var resumen = balance == null ? servicios.getBalance().obtenerResumenDelDia()
                            : servicios.getBalance().resumirBalance(balance);
                    return new DatosInicio(resumen, balance, null);
                } catch (Exception e) {
                    return new DatosInicio(null, null, e);
                }
            }, datos -> {
                if (datos.error() != null) {
                    vista.mostrarFalloResumen();
                    mostrarError(datos.error());
                    return;
                }
                vista.mostrarUsuario(Sesion.getInstancia().getUsuarioActual());
                vista.mostrarResumenSemaforo(datos.resumen());
                vista.mostrarBalanceDashboard(datos.balance());
            });
        });
    }

    private record DatosInicio(ResumenSemaforo resumen, modelo.BalanceDia balance, Exception error) { }

    public void actualizarSemaforo() {
        cargarSemaforo();
    }

    // Comprobar el permiso al navegar mejora la interfaz; el servicio lo vuelve a exigir al operar.
    private void abrir(Pantalla pantalla, Permiso permiso) {
        ejecutar(() -> {
            Sesion.getInstancia().exigirPermiso(permiso);
            return pantalla;
        }, navegacion::abrir);
    }

    public void abrirVenta() {
        abrir(Pantalla.VENTA, Permiso.REGISTRAR_VENTA);
    }

    public void abrirCompraGasto() {
        abrir(Pantalla.COMPRA_GASTO, Permiso.REGISTRAR_COMPRA);
    }

    public void abrirBalance() {
        abrir(Pantalla.BALANCE, Permiso.VER_BALANCE);
    }

    public void abrirMargen() {
        abrir(Pantalla.MARGEN, Permiso.VER_BALANCE);
    }

    public void abrirHistorial() {
        abrir(Pantalla.HISTORIAL, Permiso.VER_HISTORIAL);
    }

    public void abrirModificarVenta() {
        abrir(Pantalla.MODIFICAR_VENTA, Permiso.MODIFICAR_VENTA);
    }

    public void abrirCierre() {
        abrir(Pantalla.CIERRE, Permiso.CERRAR_CAJA);
    }

    public void abrirUsuarios() {
        abrir(Pantalla.USUARIOS, Permiso.GESTIONAR_USUARIOS);
    }

    public void abrirProductos() {
        abrir(Pantalla.PRODUCTOS, Permiso.GESTIONAR_PRODUCTOS);
    }

    public void abrirCostosFijos() {
        abrir(Pantalla.COSTOS_FIJOS, Permiso.DEFINIR_COSTOS_FIJOS);
    }

    public void abrirConfiguracion() {
        abrir(Pantalla.CONFIGURACION, Permiso.CONFIGURAR);
    }

    public void abrirReportes() {
        abrir(Pantalla.REPORTES, Permiso.VER_REPORTES);
    }

    // El carrito se cancela antes de cerrar la sesion; ninguna cancelacion mueve stock.
    public void cerrarSesion() {
        ejecutar(() -> {
            servicios.cerrarSesion();
            return null;
        }, ignorado -> navegacion.volverAlLogin());
    }
}
