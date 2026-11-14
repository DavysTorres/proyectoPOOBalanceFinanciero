package controlador;

import modelo.*;
import servicio.*;
import excepciones.*;
import vista.contratos.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;

/**
 * Pruebas sin ventanas ni MySQL, con servicios sustituidos por objetos sencillos de prueba.
 */
public final class RevisionControlador {
    private static int correctas, fallidas;
    private static final EjecutorTareas DIRECTO = new Inmediato();

    private interface Caso {
        void ejecutar() throws Exception;
    }

    private static void caso(String nombre, Caso prueba) {
        Sesion.getInstancia().cerrar();
        try {
            prueba.ejecutar();
            correctas++;
            System.out.println("OK: " + nombre);
        } catch (Throwable e) {
            fallidas++;
            System.out.println("FALLO: " + nombre + " -> " + e);
        } finally {
            Sesion.getInstancia().cerrar();
        }
    }

    static void verificar(boolean valor) {
        if (!valor) throw new AssertionError("Resultado inesperado");
    }

    private static BigDecimal d(String valor) {
        return new BigDecimal(valor);
    }

    static Usuario cajero() {
        return new Cajero(1, "Ana", "ana", "hash_prueba", true, LocalDateTime.now());
    }

    private static Usuario admin() {
        return new Administrador(2, "Ada", "ada", "hash_prueba", true, LocalDateTime.now());
    }

    static Producto producto() {
        return new Producto(1, "Cafe", d("10000"), d("6000"), 100, 2);
    }

    // El ejecutor sin hilos hace deterministas las aserciones de la logica de presentacion.
    static final class Inmediato implements EjecutorTareas {
        public <T> void ejecutar(Operacion<T> op, Consumer<T> exito, Consumer<Exception> error) {
            try {
                exito.accept(op.ejecutar());
            } catch (Exception e) {
                error.accept(e);
            }
        }
    }

    // Retiene el trabajo para simular un usuario que pulsa de nuevo antes de recibir respuesta.
    private static final class Manual implements EjecutorTareas {
        Runnable pendiente;
        int envios;

        public <T> void ejecutar(Operacion<T> op, Consumer<T> exito, Consumer<Exception> error) {
            envios++;
            pendiente = () -> {
                try {
                    exito.accept(op.ejecutar());
                } catch (Exception e) {
                    error.accept(e);
                }
            };
        }

        void resolver() {
            pendiente.run();
        }
    }

    static final class NavegacionPrueba implements Navegacion {
        Usuario usuario;
        Pantalla pantalla;
        boolean login;

        public void abrirMenu(Usuario usuario) {
            this.usuario = usuario;
        }

        public void abrir(Pantalla pantalla) {
            this.pantalla = pantalla;
        }

        public void volverAlLogin() {
            login = true;
        }
    }

    // Estos objetos no acceden a DAO: registran las peticiones del controlador.
    private static final class Autenticacion extends ServicioAutenticacion {
        int llamadas;
        RuntimeException fallo;

        public Usuario iniciarSesion(String username, String clave) {
            llamadas++;
            if (fallo != null) throw fallo;
            verificar(username.equals("ana") && clave.equals("Clave123"));
            Usuario u = cajero();
            Sesion.getInstancia().iniciar(u);
            return u;
        }
    }

    private static final class Productos extends ServicioProductos {
        int creados, consultas;

        public Producto crearProducto(String n, BigDecimal p, BigDecimal c, int s, int m) {
            creados++;
            return new Producto(1, n, p, c, s, m);
        }

        public List<Producto> buscarProductos(String texto) {
            consultas++;
            return Arrays.asList(producto());
        }

        public List<Producto> listarProductos() {
            consultas++;
            return Arrays.asList(producto());
        }
    }

    private static final class Ventas extends ServicioVentas {
        Venta actual, guardada;
        int cobros, ajustes, buscado;

        public Venta iniciarVenta() {
            actual = new Venta(cajero());
            return actual;
        }

        public void agregarProducto(Producto p, int cantidad) throws StockInsuficienteException {
            if (cantidad > p.getStock()) throw new StockInsuficienteException("Stock insuficiente");
            actual.agregarDetalle(p, cantidad);
        }

        public void quitarProducto(Producto p) {
            actual.quitarDetalle(p);
        }

        public void reducirCantidad(Producto p) {
            actual.reducirCantidad(p);
        }

        public Venta cobrar() {
            cobros++;
            actual.setId(42);
            guardada = actual;
            actual = null;
            return guardada;
        }

        public void cancelarVenta() {
            actual = null;
        }

        public Venta buscarVenta(int id) {
            buscado = id;
            return guardada;
        }

        public Venta modificarVenta(int id, BigDecimal total, String motivo) {
            ajustes++;
            verificar(id == 42 && motivo.equals("Ajuste autorizado"));
            guardada.restaurarTotalRegistrado(total);
            return guardada;
        }
    }

    private static final class Balances extends ServicioBalance {
        RuntimeException fallo;
        LocalDate consultada;
        final BalanceDia balance = new BalanceDia(LocalDate.of(2026, 11, 6), d("100"), d("60"), d("40"), d("20"),
                d("20"), d("50"), d("0"), d("200"));

        public BalanceDia obtenerBalanceDelDia(LocalDate fecha) {
            consultada = fecha;
            if (fallo != null) throw fallo;
            return balance;
        }
    }

    private static final class Exportacion extends ServicioExportacion {
        int envios;
        BalanceDia recibido;

        public void exportar(BalanceDia b, String ruta) {
            envios++;
            recibido = b;
        }
    }

    private static final class Configuracion extends ServicioConfiguracion {
        int guardados;
        Negocio recibido;

        public void guardarNegocio(Negocio n) {
            guardados++;
            recibido = n;
        }
    }

    public static int ejecutar() throws Exception {
        caso("Login conecta el boton y borra la copia de la clave", () -> {
            VistaPrueba v = new VistaPrueba();
            char[] clave = v.clave;
            Autenticacion a = new Autenticacion();
            NavegacionPrueba n = new NavegacionPrueba();
            new LoginControlador(v, a, n, DIRECTO);
            v.pulsar(AccionVista.INGRESAR);
            verificar(a.llamadas == 1 && n.usuario.getRol() == Rol.CAJERO && v.claveLimpiada);
            for (char caracter : clave) verificar(caracter == '\0');
            verificar(v.estadosOcupada.equals(Arrays.asList(true, false)));
        });
        caso("Campos vacios no consumen intentos de autenticacion", () -> {
            VistaPrueba v = new VistaPrueba();
            v.username = " ";
            Autenticacion a = new Autenticacion();
            new LoginControlador(v, a, new NavegacionPrueba(), DIRECTO).iniciarSesion();
            verificar(a.llamadas == 0 && v.error != null && !v.ocupada);
        });
        caso("Credenciales incorrectas no abren menu y restauran controles", () -> {
            VistaPrueba v = new VistaPrueba();
            Autenticacion a = new Autenticacion();
            a.fallo = new CredencialesInvalidasException("Usuario o clave incorrectos");
            NavegacionPrueba n = new NavegacionPrueba();
            new LoginControlador(v, a, n, DIRECTO).iniciarSesion();
            verificar(n.usuario == null && v.error.contains("incorrectos") && !v.ocupada);
        });
        caso("Bloqueo temporal se presenta como error de negocio", () -> {
            VistaPrueba v = new VistaPrueba();
            Autenticacion a = new Autenticacion();
            a.fallo = new UsuarioBloqueadoException("Usuario bloqueado");
            new LoginControlador(v, a, new NavegacionPrueba(), DIRECTO).iniciarSesion();
            verificar(v.error.equals("Usuario bloqueado"));
        });
        caso("Errores JDBC no filtran SQL ni detalles privados", () -> {
            VistaPrueba v = new VistaPrueba();
            Autenticacion a = new Autenticacion();
            a.fallo = new AccesoDatosException("SELECT password_hash FROM usuarios: secreto");
            new LoginControlador(v, a, new NavegacionPrueba(), DIRECTO).iniciarSesion();
            verificar(v.error.contains("conexion") && !v.error.contains("SELECT") && !v.error.contains("secreto"));
        });
        caso("Errores tecnicos inesperados tampoco exponen su mensaje", () -> {
            VistaPrueba v = new VistaPrueba();
            Autenticacion a = new Autenticacion();
            a.fallo = new IllegalStateException("dato_privado");
            new LoginControlador(v, a, new NavegacionPrueba(), DIRECTO).iniciarSesion();
            verificar(v.error.equals("No se pudo completar la operacion."));
        });
        caso("Importes con coma decimal llegan al servicio como BigDecimal", () -> {
            VistaPrueba v = new VistaPrueba();
            v.precio = "10000,50";
            Productos p = new Productos();
            new ProductoControlador(v, p, DIRECTO).crear();
            verificar(v.productoMostrado.getPrecioVenta().compareTo(d("10000.50")) == 0 && p.creados == 1);
        });
        caso("Un importe ambiguo no dispara escritura", () -> {
            VistaPrueba v = new VistaPrueba();
            v.precio = "1.000,50";
            Productos p = new Productos();
            new ProductoControlador(v, p, DIRECTO).crear();
            verificar(p.creados == 0 && v.error != null);
        });
        caso("Stock con fraccion y edicion sin seleccion se rechazan", () -> {
            VistaPrueba v = new VistaPrueba();
            Productos p = new Productos();
            ProductoControlador ctrl = new ProductoControlador(v, p, DIRECTO);
            v.stock = "1.5";
            ctrl.crear();
            verificar(p.creados == 0 && v.error != null);
            v.error = null;
            ctrl.editar();
            verificar(v.error.contains("Seleccione"));
        });
        caso("Un segundo clic no duplica una operacion pendiente", () -> {
            VistaPrueba v = new VistaPrueba();
            Productos p = new Productos();
            Manual m = new Manual();
            ProductoControlador ctrl = new ProductoControlador(v, p, m);
            v.nombre = "Original";
            ctrl.crear();
            v.nombre = "Cambiado";
            ctrl.crear();
            verificar(m.envios == 1 && v.ocupada && p.creados == 0);
            m.resolver();
            verificar(p.creados == 1 && v.productoMostrado.getNombre().equals("Original") && !v.ocupada);
        });
        caso("Una ventana desvinculada no recibe respuestas tardias", () -> {
            VistaPrueba v = new VistaPrueba();
            Productos p = new Productos();
            Manual m = new Manual();
            ProductoControlador ctrl = new ProductoControlador(v, p, m);
            ctrl.crear();
            ctrl.desvincular();
            m.resolver();
            ctrl.crear();
            verificar(p.creados == 1 && v.productoMostrado == null && m.envios == 1);
        });
        caso("Venta registra eventos y presenta el carrito calculado por el modelo", () -> {
            VistaPrueba v = new VistaPrueba();
            v.seleccionado = producto();
            v.cantidad = "3";
            Ventas s = new Ventas();
            new VentaControlador(v, s, new Productos(), DIRECTO);
            v.pulsar(AccionVista.NUEVA_VENTA);
            v.pulsar(AccionVista.AGREGAR);
            v.pulsar(AccionVista.REDUCIR);
            verificar(v.ventaMostrada.getTotal().compareTo(d("20000")) == 0);
            v.pulsar(AccionVista.QUITAR);
            verificar(v.ventaMostrada.estaVacia());
            verificar(v.seleccionado.getStock() == 100);
        });
        caso("Stock insuficiente se comunica sin marcar la venta como cobrada", () -> {
            VistaPrueba v = new VistaPrueba();
            v.seleccionado = producto();
            v.cantidad = "101";
            new VentaControlador(v, new Ventas(), new Productos(), DIRECTO);
            v.pulsar(AccionVista.NUEVA_VENTA);
            v.pulsar(AccionVista.AGREGAR);
            verificar(v.error.contains("Stock insuficiente") && v.ventaCobrada == null && !v.ocupada);
        });
        caso("Cobro confirma antes de limpiar y presenta numero de venta", () -> {
            VistaPrueba v = new VistaPrueba();
            v.seleccionado = producto();
            Ventas s = new Ventas();
            new VentaControlador(v, s, new Productos(), DIRECTO);
            v.pulsar(AccionVista.NUEVA_VENTA);
            v.pulsar(AccionVista.AGREGAR);
            v.pulsar(AccionVista.COBRAR);
            verificar(s.cobros == 1 && v.ventaMostrada == null && v.ventaCobrada.getId() == 42);
        });
        caso("Primer producto inicia la venta y el efectivo insuficiente no registra cobro", () -> {
            VistaPrueba v = new VistaPrueba();
            v.seleccionado = producto();
            Ventas s = new Ventas();
            new VentaControlador(v, s, new Productos(), DIRECTO);
            v.pulsar(AccionVista.AGREGAR);
            verificar(s.actual != null && s.actual.getDetalles().size() == 1);
            v.efectivoRecibido = "1";
            v.pulsar(AccionVista.COBRAR);
            verificar(s.cobros == 0 && s.actual != null && v.error.contains("no alcanza"));
            v.efectivoRecibido = "20000";
            v.pulsar(AccionVista.COBRAR);
            verificar(s.cobros == 1);
            v.pulsar(AccionVista.AGREGAR);
            verificar(s.actual != null && s.actual.getDetalles().size() == 1);
        });
        caso("Fallo al primer agregado libera el carrito sin dejar una venta bloqueada", () -> {
            VistaPrueba v = new VistaPrueba(); v.seleccionado = producto(); v.cantidad = "101";
            Ventas s = new Ventas(); new VentaControlador(v, s, new Productos(), DIRECTO);
            v.pulsar(AccionVista.AGREGAR);
            verificar(s.actual == null && v.error.contains("Stock insuficiente"));
            v.cantidad = "1"; v.pulsar(AccionVista.AGREGAR);
            verificar(s.actual != null && s.actual.getTotal().compareTo(d("10000")) == 0);
            v.pulsar(AccionVista.AUMENTAR);
            verificar(s.actual.getTotal().compareTo(d("20000")) == 0);
        });
        caso("Cancelar y buscar se delegan a los servicios", () -> {
            VistaPrueba v = new VistaPrueba();
            Ventas s = new Ventas();
            Productos p = new Productos();
            new VentaControlador(v, s, p, DIRECTO);
            v.pulsar(AccionVista.NUEVA_VENTA);
            v.pulsar(AccionVista.CANCELAR);
            v.pulsar(AccionVista.BUSCAR);
            verificar(s.actual == null && v.ventaMostrada == null && p.consultas == 1 && v.productosMostrados.size() == 1);
        });
        caso("Cajero navega a venta pero no a administracion ni cifras", () -> {
            Sesion.getInstancia().iniciar(cajero());
            VistaPrueba v = new VistaPrueba();
            NavegacionPrueba n = new NavegacionPrueba();
            new PrincipalControlador(v, new ServiciosAplicacion(), n, DIRECTO);
            v.pulsar(AccionVista.ABRIR_VENTA);
            verificar(n.pantalla == Pantalla.VENTA);
            v.pulsar(AccionVista.ABRIR_USUARIOS);
            verificar(n.pantalla == Pantalla.VENTA && v.error != null);
            v.pulsar(AccionVista.ABRIR_BALANCE);
            verificar(n.pantalla == Pantalla.VENTA);
        });
        caso("Administrador puede solicitar todas las secciones del menu", () -> {
            Sesion.getInstancia().iniciar(admin());
            VistaPrueba v = new VistaPrueba();
            NavegacionPrueba n = new NavegacionPrueba();
            new PrincipalControlador(v, new ServiciosAplicacion(), n, DIRECTO);
            v.pulsar(AccionVista.ABRIR_PRODUCTOS);
            verificar(n.pantalla == Pantalla.PRODUCTOS);
            v.pulsar(AccionVista.ABRIR_CIERRE);
            verificar(n.pantalla == Pantalla.CIERRE);
            v.pulsar(AccionVista.ABRIR_CONFIGURACION);
            verificar(n.pantalla == Pantalla.CONFIGURACION);
            v.pulsar(AccionVista.ABRIR_REPORTES);
            verificar(n.pantalla == Pantalla.REPORTES);
        });
        caso("Cerrar sesion vuelve al login sin mantener autenticacion", () -> {
            Sesion.getInstancia().iniciar(cajero());
            VistaPrueba v = new VistaPrueba();
            NavegacionPrueba n = new NavegacionPrueba();
            new PrincipalControlador(v, new ServiciosAplicacion(), n, DIRECTO).cerrarSesion();
            verificar(!Sesion.getInstancia().hayUsuario() && n.login);
        });
        caso("Fecha invalida no consulta el balance", () -> {
            VistaPrueba v = new VistaPrueba();
            v.fecha = "06/11/2026";
            Balances b = new Balances();
            new BalanceControlador(v, b, new Exportacion(), DIRECTO).cargarBalance();
            verificar(b.consultada == null && v.error.contains("AAAA-MM-DD"));
        });
        caso("Exportar exige un balance cargado y usa el objeto presentado", () -> {
            VistaPrueba v = new VistaPrueba();
            v.ruta = "informe.csv";
            Balances b = new Balances();
            Exportacion e = new Exportacion();
            BalanceControlador ctrl = new BalanceControlador(v, b, e, DIRECTO);
            ctrl.exportar();
            verificar(e.envios == 0 && v.error != null);
            v.error = null;
            ctrl.cargarBalance();
            ctrl.exportar();
            verificar(e.envios == 1 && e.recibido == v.balanceMostrado && v.error == null);
        });
        caso("Cancelar selector no exporta y un fallo de carga invalida datos anteriores", () -> {
            VistaPrueba v = new VistaPrueba();
            Balances b = new Balances();
            Exportacion e = new Exportacion();
            BalanceControlador ctrl = new BalanceControlador(v, b, e, DIRECTO);
            ctrl.cargarBalance();
            ctrl.exportar();
            verificar(e.envios == 0);
            b.fallo = new AccesoDatosException("Fallo");
            ctrl.cargarBalance();
            v.ruta = "informe.csv";
            ctrl.exportar();
            verificar(v.balanceMostrado == null && e.envios == 0);
        });
        caso("Buscar y modificar venta llevan id, total y motivo al servicio", () -> {
            VistaPrueba v = new VistaPrueba();
            v.ventaId = "42";
            Ventas s = new Ventas();
            s.guardada = new Venta(cajero());
            s.guardada.setId(42);
            new ModificarVentaControlador(v, s, DIRECTO);
            v.pulsar(AccionVista.BUSCAR);
            v.pulsar(AccionVista.MODIFICAR);
            verificar(s.buscado == 42 && s.ajustes == 1 && v.ventaMostrada.getTotal().compareTo(d("19000")) == 0);
        });
        caso("Configuracion convierte texto y conserva las validaciones del modelo", () -> {
            VistaPrueba v = new VistaPrueba();
            Configuracion s = new Configuracion();
            ConfiguracionControlador ctrl = new ConfiguracionControlador(v, s, DIRECTO);
            ctrl.guardar();
            verificar(s.guardados == 1 && s.recibido.getDiasOperacion() == 30);
            v.cierre = "07:00";
            ctrl.guardar();
            verificar(s.guardados == 1 && v.error != null);
        });
        caso("La composicion construye los trece controladores sin consultar MySQL", () -> {
            AplicacionControladores a = new AplicacionControladores(new ServiciosAplicacion(), DIRECTO);
            NavegacionPrueba n = new NavegacionPrueba();
            a.crearLogin(new VistaPrueba(), n);
            a.crearPrincipal(new VistaPrueba(), n);
            a.crearVenta(new VistaPrueba());
            a.crearCompraGasto(new VistaPrueba());
            a.crearBalance(new VistaPrueba());
            a.crearMargen(new VistaPrueba());
            a.crearHistorial(new VistaPrueba());
            a.crearModificarVenta(new VistaPrueba());
            a.crearCierre(new VistaPrueba());
            a.crearUsuario(new VistaPrueba());
            a.crearProducto(new VistaPrueba());
            a.crearCostoFijo(new VistaPrueba());
            a.crearConfiguracion(new VistaPrueba());
            a.close();
        });
        caso("Buscar una venta desde el controlador tambien exige permiso real", () -> {
            Sesion.getInstancia().iniciar(cajero());
            VistaPrueba v = new VistaPrueba();
            new ModificarVentaControlador(v, new ServicioVentas(), DIRECTO).buscarVenta();
            verificar(v.error != null && v.ventaMostrada == null && !v.ocupada);
        });
        caso("Listado administrativo real no se entrega al cajero", () -> {
            Sesion.getInstancia().iniciar(cajero());
            VistaPrueba v = new VistaPrueba();
            new ProductoControlador(v, new ServicioProductos(), DIRECTO).listar();
            verificar(v.error != null && v.productosMostrados == null && !v.ocupada);
        });
        caso("La cola Swing serializa tareas y entrega resultados en el EDT", () -> {
            CountDownLatch terminadas = new CountDownLatch(2);
            AtomicBoolean correcto = new AtomicBoolean(true);
            List<Integer> orden = Collections.synchronizedList(new ArrayList<>());
            try (EjecutorSwing e = new EjecutorSwing()) {
                SwingUtilities.invokeAndWait(() -> {
                    for (int i = 1; i <= 2; i++) {
                        final int valor = i;
                        e.ejecutar(() -> {
                            if (SwingUtilities.isEventDispatchThread()) correcto.set(false);
                            orden.add(valor);
                            return valor;
                        }, resultado -> {
                            if (!SwingUtilities.isEventDispatchThread()) correcto.set(false);
                            terminadas.countDown();
                        }, error -> {
                            correcto.set(false);
                            terminadas.countDown();
                        });
                    }
                });
                verificar(terminadas.await(5, TimeUnit.SECONDS));
                verificar(correcto.get() && orden.equals(Arrays.asList(1, 2)));
            }
        });
        caso("El semaforo se refresca solo despues de una escritura exitosa", () -> {
            VistaPrueba v = new VistaPrueba();
            Productos p = new Productos();
            Manual m = new Manual();
            ProductoControlador ctrl = new ProductoControlador(v, p, m);
            int[] cambios = {0};
            ctrl.alCambiarDatos(() -> cambios[0]++);
            ctrl.crear();
            verificar(cambios[0] == 0);
            m.resolver();
            verificar(cambios[0] == 1);
            v.precio = "-1";
            ctrl.crear();
            m.resolver();
            verificar(cambios[0] == 1);
        });
        System.out.println("RESULTADO CONTROLADOR UNITARIO: " + correctas + " correctas, " + fallidas + " fallidas");
        return fallidas;
    }

    public static void main(String[] args) throws Exception {
        if (ejecutar() > 0) System.exit(1);
    }
}
