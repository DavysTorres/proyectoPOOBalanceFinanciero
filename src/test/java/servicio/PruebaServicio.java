package servicio;

import dao.*;
import modelo.*;
import excepciones.*;
import soporte.BaseTemporal;

import java.sql.Connection;
import java.math.BigDecimal;
import java.time.*;

/**
 * Simula una jornada completa mediante servicios y DAO reales en un esquema temporal.
 */
public final class PruebaServicio {
    private static int correctas, fallidas;
    private static Connection c;
    private static ServiciosAplicacion servicios;
    private static Usuario admin, jefe, cajero;
    private static Producto producto;
    private static Venta venta;
    private static final LocalDate FECHA = LocalDate.of(2026, 11, 6);
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-06T15:00:00Z"), ZoneId.of("America/Bogota"));

    private interface Caso {
        void ejecutar() throws Exception;
    }

    // Informa fallos con su tipo y mensaje de negocio, sin imprimir credenciales JDBC.
    private static void caso(String nombre, Caso prueba) {
        try {
            prueba.ejecutar();
            correctas++;
            System.out.println("OK: " + nombre);
        } catch (Throwable e) {
            fallidas++;
            System.out.println("FALLO: " + nombre + " (" + e.getClass().getSimpleName() + "): " + e.getMessage());
        }
    }

    private static BigDecimal d(String valor) {
        return new BigDecimal(valor);
    }

    private static void verificar(boolean valor) {
        if (!valor) throw new AssertionError("Resultado inesperado");
    }

    private static void monto(String esperado, BigDecimal actual) {
        verificar(d(esperado).compareTo(actual) == 0);
    }

    private static void rechaza(Class<? extends Throwable> tipo, Caso prueba) throws Exception {
        try {
            prueba.ejecutar();
        } catch (Throwable e) {
            if (tipo.isInstance(e)) return;
            throw new AssertionError("Excepcion inesperada: " + e);
        }
        throw new AssertionError("Se acepto una operacion invalida");
    }

    // Cambiar la sesion con usuarios persistidos simplifica casos de permisos; el login se prueba aparte.
    private static void como(Usuario u) {
        Sesion.getInstancia().iniciar(u);
    }

    private static void jornada() {
        caso("Primera inicializacion crea solo un administrador con hash real", () -> {
            admin = servicios.getUsuarios().crearAdministradorInicial("Ada", "ada", "ClaveAdmin123");
            verificar(admin.getId() > 0 && admin instanceof Administrador);
            verificar(servicios.getAutenticacion().verificarClave("ClaveAdmin123", admin.getPasswordHash()));
            rechaza(DatosInvalidosException.class, () -> servicios.getUsuarios().crearAdministradorInicial("Otra", "otra", "ClaveOtra123"));
        });
        caso("Login real y configuracion administrativa", () -> {
            Usuario u = servicios.getAutenticacion().iniciarSesion("ada", "ClaveAdmin123");
            verificar(u.getId() == admin.getId());
            servicios.getConfiguracion().guardarNegocio(new Negocio("Tienda", 30, LocalTime.of(8, 0), LocalTime.of(18, 0)));
            servicios.getCostosFijos().crear("Arriendo", d("11700000"));
            monto("390000", new CostoFijoDAO(c).listarActivos().get(0).calcularMontoDiario(30));
        });
        caso("Administrador crea perfiles, producto y rechaza username repetido", () -> {
            como(admin);
            jefe = servicios.getUsuarios().crearUsuario("Luis", "luis", "ClaveJefe123", Rol.JEFE);
            cajero = servicios.getUsuarios().crearUsuario("Ana", "ana", "ClaveCajero123", Rol.CAJERO);
            producto = servicios.getProductos().crearProducto("Cafe", d("10000"), d("6000"), 100, 2);
            verificar(jefe instanceof Jefe && cajero instanceof Cajero);
            rechaza(DatosInvalidosException.class, () -> servicios.getUsuarios().crearUsuario("Otra", "ANA", "ClaveOtra123", Rol.CAJERO));
        });
        caso("No se puede desactivar el ultimo administrador", () -> {
            como(admin);
            rechaza(DatosInvalidosException.class, () -> servicios.getUsuarios().desactivarUsuario(admin.getId()));
        });
        caso("Cambiar rol reconstruye una subclase coherente", () -> {
            como(admin);
            Usuario temporal = servicios.getUsuarios().crearUsuario("Temporal", "temporal", "ClaveTemporal123", Rol.CAJERO);
            Usuario editado = servicios.getUsuarios().editarUsuario(temporal.getId(), "Temporal", "temporal", Rol.JEFE, true);
            verificar(editado instanceof Jefe && new UsuarioDAO(c).buscarPorId(temporal.getId()) instanceof Jefe);
            servicios.getUsuarios().desactivarUsuario(temporal.getId());
        });
        caso("Cajero no puede registrar compras ni ver cifras", () -> {
            como(cajero);
            rechaza(PermisoDenegadoException.class, () -> servicios.getCompras().registrarCompra(new Compra(cajero, "P")));
            rechaza(PermisoDenegadoException.class, () -> servicios.getBalance().obtenerBalanceDelDia(FECHA));
            verificar(servicios.getBalance().obtenerEstadoDelDia() instanceof EstadoRojo);
        });
        caso("Venta no permite superar stock sumando lineas repetidas", () -> {
            como(cajero);
            servicios.getVentas().iniciarVenta();
            servicios.getVentas().agregarProducto(producto, 60);
            rechaza(StockInsuficienteException.class, () -> servicios.getVentas().agregarProducto(producto, 41));
            monto("600000", servicios.getVentas().calcularTotalEnCurso());
            verificar(new ProductoDAO(c).buscarPorId(producto.getId()).getStock() == 100);
            servicios.getVentas().cancelarVenta();
        });
        caso("Cobrar guarda una venta y descuenta stock una sola vez", () -> {
            como(cajero);
            servicios.getVentas().iniciarVenta();
            servicios.getVentas().agregarProducto(producto, 85);
            venta = servicios.getVentas().cobrar();
            verificar(venta.getId() > 0);
            verificar(new ProductoDAO(c).buscarPorId(producto.getId()).getStock() == 15);
            rechaza(DatosInvalidosException.class, () -> servicios.getVentas().cobrar());
        });
        caso("Balance del servicio reproduce el ejemplo de la guia", () -> {
            como(jefe);
            BalanceDia b = servicios.getBalance().obtenerBalanceDelDia(FECHA);
            monto("850000", b.getIngresos());
            monto("510000", b.getCostosVariables());
            monto("975000", b.getPuntoEquilibrio());
            monto("-50000", b.getResultado());
            verificar(b.isPuntoEquilibrioCalculable() && b.isPuntoEquilibrioEstimado());
            verificar(new Diagnostico().evaluar(b, new ConfiguracionDAO(c).cargarNegocio()) instanceof EstadoAmarillo);
        });
        caso("Carrito no se puede cobrar desde otra sesion", () -> {
            como(cajero);
            servicios.getVentas().iniciarVenta();
            servicios.getVentas().agregarProducto(producto, 1);
            como(jefe);
            rechaza(PermisoDenegadoException.class, () -> servicios.getVentas().cobrar());
            como(cajero);
            servicios.getVentas().cancelarVenta();
        });
        caso("Cerrar sesion descarta el carrito sin trasladarlo a otro perfil", () -> {
            como(cajero);
            servicios.getVentas().iniciarVenta();
            servicios.getVentas().agregarProducto(producto, 1);
            servicios.cerrarSesion();
            verificar(!Sesion.getInstancia().hayUsuario());
            como(jefe);
            servicios.getVentas().iniciarVenta();
            servicios.getVentas().cancelarVenta();
            verificar(new ProductoDAO(c).buscarPorId(producto.getId()).getStock() == 15);
        });
        caso("Compra aumenta inventario una sola vez sin duplicar costo variable", () -> {
            como(jefe);
            Compra compra = new Compra(admin, "Proveedor");
            compra.agregarDetalle(producto, 10, d("6000"));
            servicios.getCompras().registrarCompra(compra);
            verificar(compra.getUsuario().getId() == jefe.getId());
            verificar(new ProductoDAO(c).buscarPorId(producto.getId()).getStock() == 25);
            monto("510000", servicios.getBalance().obtenerBalanceDelDia(FECHA).getCostosVariables());
        });
        caso("Gasto variable impacta resultado y caja", () -> {
            como(jefe);
            servicios.getCompras().registrarGasto(new GastoVariable(0, admin, "Transporte", d("10000"), FECHA.minusDays(1)));
            monto("520000", servicios.getBalance().obtenerBalanceDelDia(FECHA).getCostosVariables());
            monto("780000", servicios.getCierre().calcularEfectivoSistema(FECHA));
        });
        caso("Gasto fijo adicional se prorratea sin duplicar el catalogo", () -> {
            como(jefe);
            rechaza(DatosInvalidosException.class, () -> servicios.getCompras().registrarGasto(
                    new GastoFijo(0, jefe, "Arriendo", d("11700000"), FECHA, 30)));
            servicios.getCompras().registrarGasto(new GastoFijo(0, jefe, "Licencia", d("3000"), FECHA, 30));
            monto("390100", servicios.getBalance().obtenerBalanceDelDia(FECHA).getCostosFijosDia());
            monto("777000", servicios.getCierre().calcularEfectivoSistema(FECHA));
        });
        caso("Modificar venta sin motivo no cambia datos", () -> {
            como(jefe);
            rechaza(DatosInvalidosException.class, () -> servicios.getVentas().modificarVenta(venta.getId(), d("840000"), " "));
            monto("850000", new VentaDAO(c).buscarPorId(venta.getId()).getTotal());
        });
        caso("Ajuste de venta y motivo se guardan juntos", () -> {
            como(jefe);
            servicios.getVentas().modificarVenta(venta.getId(), d("840000"), "Descuento autorizado");
            monto("840000", new VentaDAO(c).buscarPorId(venta.getId()).getTotal());
            verificar(servicios.getHistorial().listarPorVenta(venta.getId()).size() == 1);
            verificar(new ProductoDAO(c).buscarPorId(producto.getId()).getStock() == 25);
        });
        caso("Fallo de historial revierte tambien el ajuste de la venta", () -> {
            como(jefe);
            HistorialCambiosDAO falla = new HistorialCambiosDAO(c) {
                @Override
                public int guardar(RegistroCambio registro) {
                    throw new AccesoDatosException("Fallo provocado en prueba");
                }
            };
            ServicioVentas s = new ServicioVentas(new VentaDAO(c), new ProductoDAO(c), falla, servicios.getCierre(), RELOJ);
            rechaza(AccesoDatosException.class, () -> s.modificarVenta(venta.getId(), d("1"), "Prueba rollback"));
            monto("840000", new VentaDAO(c).buscarPorId(venta.getId()).getTotal());
            verificar(c.getAutoCommit());
        });
        caso("Precio cambiado obliga a reiniciar el carrito antes de cobrar", () -> {
            como(cajero);
            servicios.getVentas().iniciarVenta();
            servicios.getVentas().agregarProducto(producto, 1);
            como(admin);
            servicios.getProductos().editarProducto(producto.getId(), "Cafe", d("11000"), d("6000"), 2);
            como(cajero);
            rechaza(DatosInvalidosException.class, () -> servicios.getVentas().cobrar());
            servicios.getVentas().cancelarVenta();
            como(admin);
            servicios.getProductos().editarProducto(producto.getId(), "Cafe", d("10000"), d("6000"), 2);
        });
        caso("Dia sin ventas no inventa una meta a partir del catalogo", () -> {
            como(jefe);
            BalanceDia b = servicios.getBalance().obtenerBalanceDelDia(FECHA.minusDays(1));
            verificar(!b.isPuntoEquilibrioCalculable() && !b.isPuntoEquilibrioEstimado());
            monto("0", b.getPuntoEquilibrio());
            monto("0", b.getIngresos());
        });
        caso("Sin catalogo positivo la meta no es calculable y el estado es rojo", () -> {
            ProductoDAO p = new ProductoDAO(c);
            Producto actual = p.buscarPorId(producto.getId());
            actual.setActivo(false);
            p.actualizar(actual);
            try {
                como(jefe);
                BalanceDia b = servicios.getBalance().obtenerBalanceDelDia(FECHA.minusDays(1));
                verificar(!b.isPuntoEquilibrioCalculable());
                verificar(new Diagnostico().evaluar(b, new Negocio("T", 30, LocalTime.of(8, 0), LocalTime.of(18, 0))) instanceof EstadoRojo);
            } finally {
                actual.setActivo(true);
                p.actualizar(actual);
            }
        });
        caso("Cierre guarda caja neta y detecta diferencia", () -> {
            como(jefe);
            CierreDiario cierre = servicios.getCierre().cerrarDia(FECHA, d("766500"));
            monto("767000", cierre.getEfectivoSistema());
            monto("-500", cierre.getDiferencia());
            monto("-70100", cierre.getResultado());
            verificar(cierre.hayDescuadre());
        });
        caso("Dia cerrado rechaza venta compra gasto ajuste y segundo cierre", () -> {
            como(jefe);
            rechaza(DiaCerradoException.class, () -> servicios.getVentas().iniciarVenta());
            Compra compra = new Compra(jefe, "Proveedor");
            compra.agregarDetalle(producto, 1, d("6000"));
            rechaza(DiaCerradoException.class, () -> servicios.getCompras().registrarCompra(compra));
            rechaza(DiaCerradoException.class, () -> servicios.getCompras().registrarGasto(
                    new GastoVariable(0, jefe, "Prueba", d("1"), FECHA)));
            rechaza(DiaCerradoException.class, () -> servicios.getVentas().modificarVenta(venta.getId(), d("1"), "Prueba"));
            rechaza(DiaCerradoException.class, () -> servicios.getCierre().cerrarDia(FECHA, d("0")));
            verificar(compra.getId() == 0);
        });
        caso("Cambiar costos y configuracion no altera el balance cerrado", () -> {
            como(admin);
            CostoFijo costo = new CostoFijoDAO(c).listarActivos().get(0);
            servicios.getCostosFijos().editar(costo.getId(), "Arriendo", d("30000"), true);
            servicios.getConfiguracion().guardarNegocio(new Negocio("Nueva", 31, LocalTime.of(9, 0), LocalTime.of(19, 0)));
            BalanceDia b = servicios.getBalance().obtenerBalanceDelDia(FECHA);
            monto("390100", b.getCostosFijosDia());
            monto("-70100", b.getResultado());
            monto("767000", servicios.getCierre().calcularEfectivoSistema(FECHA));
        });
        caso("Ranking y consultas del servicio funcionan con el catalogo real", () -> {
            como(jefe);
            verificar(servicios.getMargen().obtenerMasRentables(5).size() == 1);
            verificar(servicios.getMargen().detectarMargenBajo(50).size() == 1);
            verificar(servicios.getBalance().obtenerBalanceRango(FECHA.minusDays(1), FECHA).size() == 2);
            verificar(servicios.getCompras().listarComprasDelDia().size() == 1);
            verificar(servicios.getCompras().listarGastosDelDia().size() == 2);
        });
        caso("Administrador restablece clave y usuario desactivado no accede", () -> {
            como(admin);
            servicios.getAutenticacion().restablecerClave(cajero.getId(), "ClaveNueva123");
            servicios.getAutenticacion().iniciarSesion("ana", "ClaveNueva123");
            como(admin);
            servicios.getUsuarios().desactivarUsuario(cajero.getId());
            rechaza(CredencialesInvalidasException.class, () -> servicios.getAutenticacion().iniciarSesion("ana", "ClaveNueva123"));
        });
    }

    // El runner compartido crea y elimina solo su propia base; la sesion siempre se limpia.
    public static void main(String[] args) throws Exception {
        if (RevisionServicio.ejecutar() > 0) System.exit(1);
        if (args.length > 0 && args[0].equals("--sin-mysql")) return;
        try {
            BaseTemporal.ejecutar("balance_servicio_prueba_", (conexion, esquema) -> {
                c = conexion;
                servicios = new ServiciosAplicacion(new UsuarioDAO(c), new ProductoDAO(c), new VentaDAO(c),
                        new CompraDAO(c), new GastoDAO(c), new CostoFijoDAO(c), new ConfiguracionDAO(c),
                        new CierreDiarioDAO(c), new HistorialCambiosDAO(c), RELOJ);
                jornada();
            });
        } finally {
            Sesion.getInstancia().cerrar();
        }
        System.out.println("RESULTADO SERVICIO MYSQL: " + correctas + " correctas, " + fallidas + " fallidas");
        if (fallidas > 0) System.exit(1);
    }
}
