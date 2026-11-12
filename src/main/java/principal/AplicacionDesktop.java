package principal;

import controlador.*;
import servicio.*;
import modelo.Usuario;
import vista.*;
import util.Mensajes;
import conexion.conexionDB;

import java.util.*;

/**
 * Punto de composicion: une ventanas y controladores, y administra el cierre de la aplicacion.
 */
public final class AplicacionDesktop implements Navegacion {
    private final ServiciosAplicacion servicios;
    private final EjecutorSwing tareas = new EjecutorSwing();
    private final AplicacionControladores controladores;
    private final Map<Pantalla, FrmBase> ventanas = new EnumMap<>(Pantalla.class);
    private FrmBase entrada;
    private FrmPrincipal principal;

    public AplicacionDesktop() {
        servicios = new ServiciosAplicacion();
        controladores = new AplicacionControladores(servicios, tareas);
        controladores.alCambiarDatos(this::actualizarPrincipal);
    }

    // Consulta el primer uso sin bloquear el hilo de Swing ni crear usuarios automaticamente.
    public void iniciar() {
        tareas.ejecutar(() -> servicios.getInicializacion().necesitaConfiguracionInicial(), inicial -> {
            if (inicial) mostrarInicializacion();
            else volverAlLogin();
        }, error -> {
            volverAlLogin();
            entrada.mostrarError("No se pudo abrir la base de datos. Revise la configuracion y las tablas indicadas en README.");
        });
    }

    private void mostrarInicializacion() {
        FrmInicializacion vista = new FrmInicializacion();
        vista.conectar(controladores.crearInicializacion(vista, this));
        entrada = vista;
        vista.alCerrar(this::salir);
        vista.mostrar();
    }

    @Override
    public void volverAlLogin() {
        servicios.cerrarSesion();
        cerrarVentanas();
        FrmLogin vista = new FrmLogin();
        vista.conectar(controladores.crearLogin(vista, this));
        entrada = vista;
        vista.alCerrar(this::salir);
        vista.mostrar();
    }

    @Override
    public void abrirMenu(Usuario usuario) {
        cerrarVentanas();
        FrmPrincipal vista = new FrmPrincipal(usuario);
        PrincipalControlador controlador = controladores.crearPrincipal(vista, this);
        vista.conectar(controlador);
        principal = vista;
        vista.alCerrar(this::salir);
        vista.mostrar();
        controlador.cargarSemaforo();
    }

    // Conserva los formularios al navegar para no perder carritos ni datos pendientes.
    @Override
    public void abrir(Pantalla pantalla) {
        if (principal == null || (principal.getModuloActual() != null && principal.getModuloActual().estaOcupada())) return;
        FrmBase existente = ventanas.get(pantalla);
        if (existente != null) {
            principal.mostrarModulo(existente);
            return;
        }
        switch (pantalla) {
            case VENTA: {
                FrmVenta v = new FrmVenta();
                VentaControlador c = controladores.crearVenta(v);
                presentar(pantalla, v, c);
                c.buscarProductos();
                break;
            }
            case COMPRA_GASTO: {
                FrmCompraGasto v = new FrmCompraGasto();
                CompraGastoControlador c = controladores.crearCompraGasto(v);
                presentar(pantalla, v, c);
                c.cargarProductos();
                break;
            }
            case BALANCE:
            case REPORTES: {
                FrmBalance v = pantalla == Pantalla.REPORTES ? new FrmReportes() : new FrmBalance();
                BalanceControlador c = controladores.crearBalance(v);
                presentar(pantalla, v, c);
                c.cargarBalance();
                break;
            }
            case MARGEN: {
                FrmMargen v = new FrmMargen();
                MargenControlador c = controladores.crearMargen(v);
                presentar(pantalla, v, c);
                c.cargarRanking();
                break;
            }
            case HISTORIAL: {
                FrmHistorial v = new FrmHistorial();
                HistorialControlador c = controladores.crearHistorial(v);
                presentar(pantalla, v, c);
                c.cargarHistorial();
                break;
            }
            case MODIFICAR_VENTA: {
                FrmModificarVenta v = new FrmModificarVenta();
                presentar(pantalla, v, controladores.crearModificarVenta(v));
                break;
            }
            case CIERRE: {
                FrmCierreCaja v = new FrmCierreCaja();
                CierreControlador c = controladores.crearCierre(v);
                presentar(pantalla, v, c);
                c.calcularEfectivoSistema();
                break;
            }
            case USUARIOS: {
                FrmUsuarios v = new FrmUsuarios();
                UsuarioControlador c = controladores.crearUsuario(v);
                presentar(pantalla, v, c);
                c.listar();
                break;
            }
            case PRODUCTOS: {
                FrmProductos v = new FrmProductos();
                ProductoControlador c = controladores.crearProducto(v);
                presentar(pantalla, v, c);
                c.listar();
                break;
            }
            case COSTOS_FIJOS: {
                FrmCostosFijos v = new FrmCostosFijos();
                CostoFijoControlador c = controladores.crearCostoFijo(v);
                presentar(pantalla, v, c);
                c.listar();
                break;
            }
            case CONFIGURACION: {
                FrmConfiguracion v = new FrmConfiguracion();
                ConfiguracionControlador c = controladores.crearConfiguracion(v);
                presentar(pantalla, v, c);
                c.cargar();
                break;
            }
            default:
                throw new IllegalArgumentException("Pantalla desconocida");
        }
    }

    private void presentar(Pantalla pantalla, FrmBase vista, ControladorBase<?> controlador) {
        ventanas.put(pantalla, vista);
        vista.conectar(controlador);
        vista.alCerrar(() -> {
            if (pantalla == Pantalla.VENTA) {
                vista.setOcupada(true);
                tareas.ejecutar(() -> {
                            servicios.getVentas().cancelarVenta();
                            return null;
                        },
                        resultado -> {
                            principal.retirarModulo(vista);
                            vista.cerrar();
                            ventanas.remove(pantalla);
                            actualizarPrincipal();
                        },
                        error -> {
                            vista.setOcupada(false);
                            vista.mostrarError("No se pudo cancelar la venta.");
                        });
            } else {
                principal.retirarModulo(vista);
                vista.cerrar();
                ventanas.remove(pantalla);
                actualizarPrincipal();
            }
        });
        principal.mostrarModulo(vista);
    }

    // Cerrar un formulario de movimientos permite refrescar la pantalla principal.
    private void actualizarPrincipal() {
        if (principal == null || !principal.isDisplayable()) return;
        if (!Sesion.getInstancia().hayUsuario()) {
            volverAlLogin();
            return;
        }
        Usuario usuario = Sesion.getInstancia().getUsuarioActual();
        if (principal.getRolMostrado() != usuario.getRol()) abrirMenu(usuario);
        else principal.solicitarActualizacion();
    }

    private void cerrarVentanas() {
        for (FrmBase ventana : ventanas.values()) ventana.cerrar();
        ventanas.clear();
        if (entrada != null) {
            entrada.cerrar();
            entrada = null;
        }
        if (principal != null) {
            principal.cerrar();
            principal = null;
        }
    }

    // Espera operaciones aceptadas y cierra la conexion desde la misma cola de trabajo.
    public void salir() {
        if ((entrada != null && entrada.estaOcupada()) || (principal != null && principal.estaOcupada())) {
            Mensajes.informacion(principal, "Espere a que termine la operacion.");
            return;
        }
        for (FrmBase v : ventanas.values())
            if (v.estaOcupada()) {
                v.mostrarInformacion("Espere a que termine la operacion.");
                return;
            }
        tareas.ejecutar(() -> {
                    servicios.cerrarSesion();
                    conexionDB.getInstance().desconectar();
                    return null;
                },
                resultado -> {
                    cerrarVentanas();
                    controladores.close();
                },
                error -> Mensajes.error(principal, "No se pudo cerrar la conexion. Intente de nuevo."));
    }
}
