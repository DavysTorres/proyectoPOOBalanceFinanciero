package controlador;

import dao.*;
import modelo.*;
import servicio.*;
import soporte.BaseTemporal;
import vista.contratos.*;

import java.math.BigDecimal;
import java.nio.file.*;
import java.sql.Connection;
import java.time.*;

/**
 * Prueba controladores con vistas de memoria y servicios/DAO reales, sin ventanas ni datos de produccion.
 */
public final class PruebaControlador {
    private static int correctas, fallidas;
    private static ServiciosAplicacion servicios;
    private static AplicacionControladores controladores;
    private static Connection conexion;
    private static Usuario admin, jefe, cajero;
    private static Producto producto;
    private static Venta venta;
    private static final RevisionControlador.NavegacionPrueba NAVEGACION = new RevisionControlador.NavegacionPrueba();
    private static final Clock RELOJ = Clock.fixed(Instant.parse("2026-11-06T15:00:00Z"), ZoneId.of("America/Bogota"));

    private interface Caso {
        void ejecutar() throws Exception;
    }

    private static void caso(String nombre, Caso prueba) {
        try {
            prueba.ejecutar();
            correctas++;
            System.out.println("OK: " + nombre);
        } catch (Throwable e) {
            fallidas++;
            System.out.println("FALLO: " + nombre + " -> " + e);
        }
    }

    private static BigDecimal d(String valor) {
        return new BigDecimal(valor);
    }

    private static void verificar(boolean valor) {
        RevisionControlador.verificar(valor);
    }

    private static void monto(String esperado, BigDecimal actual) {
        verificar(d(esperado).compareTo(actual) == 0);
    }

    private static void correcto(VistaPrueba vista) {
        if (vista.error != null) throw new AssertionError(vista.error);
        verificar(!vista.ocupada);
    }

    private static void como(Usuario usuario) {
        Sesion.getInstancia().iniciar(usuario);
    }

    // Los botones de la vista de prueba disparan exactamente los listeners registrados.
    private static void flujo() {
        caso("LoginControlador autentica realmente y solicita el menu correcto", () -> {
            admin = servicios.getUsuarios().crearAdministradorInicial("Ada", "ada", "ClaveAdmin123");
            VistaPrueba v = new VistaPrueba();
            v.username = "ada";
            v.clave = "ClaveAdmin123".toCharArray();
            controladores.crearLogin(v, NAVEGACION);
            v.pulsar(AccionVista.INGRESAR);
            correcto(v);
            verificar(NAVEGACION.usuario.getRol() == Rol.ADMINISTRADOR && v.claveLimpiada);
        });
        caso("ConfiguracionControlador guarda y recupera Negocio", () -> {
            VistaPrueba v = new VistaPrueba();
            v.nombre = "Tienda";
            controladores.crearConfiguracion(v);
            v.pulsar(AccionVista.GUARDAR);
            v.pulsar(AccionVista.CARGAR);
            correcto(v);
            verificar(v.negocioMostrado.getDiasOperacion() == 30);
        });
        caso("CostoFijoControlador crea lista y edita el catalogo", () -> {
            VistaPrueba v = new VistaPrueba();
            v.concepto = "Arriendo";
            v.monto = "11700000";
            controladores.crearCostoFijo(v);
            v.pulsar(AccionVista.CREAR);
            v.pulsar(AccionVista.LISTAR);
            correcto(v);
            verificar(v.costosMostrados.size() == 1);
            v.id = v.costosMostrados.get(0).getId();
            v.pulsar(AccionVista.EDITAR);
            correcto(v);
        });
        caso("UsuarioControlador crea los dos perfiles y no conserva las claves", () -> {
            VistaPrueba v = new VistaPrueba();
            v.nombre = "Luis";
            v.username = "luis";
            v.rol = Rol.JEFE;
            v.clave = "ClaveJefe123".toCharArray();
            controladores.crearUsuario(v);
            v.pulsar(AccionVista.CREAR);
            correcto(v);
            v.nombre = "Ana";
            v.username = "ana";
            v.rol = Rol.CAJERO;
            v.clave = "ClaveCajero123".toCharArray();
            v.pulsar(AccionVista.CREAR);
            v.pulsar(AccionVista.LISTAR);
            correcto(v);
            for (Usuario u : v.usuariosMostrados) {
                if (u.getRol() == Rol.JEFE) jefe = u;
                if (u.getRol() == Rol.CAJERO) cajero = u;
            }
            verificar(jefe != null && cajero != null && v.claveLimpiada);
        });
        caso("ProductoControlador crea edita y lista el catalogo", () -> {
            VistaPrueba v = new VistaPrueba();
            v.nombre = "Cafe";
            controladores.crearProducto(v);
            v.pulsar(AccionVista.CREAR);
            correcto(v);
            producto = v.productoMostrado;
            v.id = producto.getId();
            v.pulsar(AccionVista.EDITAR);
            v.pulsar(AccionVista.LISTAR);
            correcto(v);
            verificar(v.productosMostrados.size() == 1);
        });
        caso("PrincipalControlador presenta semaforo sin dar cifras al cajero", () -> {
            como(cajero);
            VistaPrueba v = new VistaPrueba();
            controladores.crearPrincipal(v, NAVEGACION);
            v.pulsar(AccionVista.ACTUALIZAR);
            correcto(v);
            verificar(v.estadoMostrado instanceof EstadoRojo && v.usuarioMostrado.getId() == cajero.getId());
        });
        caso("VentaControlador cobra sin duplicar el movimiento de stock", () -> {
            como(cajero);
            VistaPrueba v = new VistaPrueba();
            v.seleccionado = producto;
            v.cantidad = "85";
            controladores.crearVenta(v);
            v.pulsar(AccionVista.NUEVA_VENTA);
            v.pulsar(AccionVista.AGREGAR);
            v.pulsar(AccionVista.COBRAR);
            correcto(v);
            venta = v.ventaCobrada;
            monto("850000", venta.getTotal());
            verificar(new ProductoDAO(conexion).buscarPorId(producto.getId()).getStock() == 15 && v.ventaMostrada == null);
        });
        caso("BalanceControlador carga el ejemplo y exporta CSV real", () -> {
            como(jefe);
            VistaPrueba v = new VistaPrueba();
            controladores.crearBalance(v);
            v.pulsar(AccionVista.CARGAR);
            correcto(v);
            monto("975000", v.balanceMostrado.getPuntoEquilibrio());
            Path carpeta = Files.createTempDirectory("controlador_csv_");
            Path archivo = carpeta.resolve("balance.csv");
            try {
                v.ruta = archivo.toString();
                v.pulsar(AccionVista.EXPORTAR);
                correcto(v);
                verificar(Files.readString(archivo).contains("975000"));
            } finally {
                Files.deleteIfExists(archivo);
                Files.deleteIfExists(carpeta);
            }
        });
        caso("CompraGastoControlador construye una compra y la guarda una sola vez", () -> {
            como(jefe);
            VistaPrueba v = new VistaPrueba();
            v.seleccionado = producto;
            v.cantidad = "10";
            controladores.crearCompraGasto(v);
            v.pulsar(AccionVista.AGREGAR);
            monto("60000", v.compraMostrada.getTotal());
            v.pulsar(AccionVista.GUARDAR_COMPRA);
            v.pulsar(AccionVista.LISTAR);
            correcto(v);
            verificar(v.compraMostrada == null && v.comprasMostradas.size() == 1);
            verificar(new ProductoDAO(conexion).buscarPorId(producto.getId()).getStock() == 25);
        });
        caso("CompraGastoControlador registra gasto con usuario y fecha del servicio", () -> {
            como(jefe);
            VistaPrueba v = new VistaPrueba();
            v.monto = "1000";
            controladores.crearCompraGasto(v);
            v.pulsar(AccionVista.GUARDAR_GASTO);
            v.pulsar(AccionVista.LISTAR);
            correcto(v);
            verificar(v.gastosMostrados.size() == 1 && v.gastosMostrados.get(0).getUsuario().getId() == jefe.getId());
        });
        caso("MargenControlador muestra ranking y consejo desde el analizador", () -> {
            como(jefe);
            VistaPrueba v = new VistaPrueba();
            v.seleccionado = producto;
            controladores.crearMargen(v);
            v.pulsar(AccionVista.CARGAR);
            v.pulsar(AccionVista.CONSEJO);
            correcto(v);
            verificar(v.rankingMostrado.size() == 1 && v.consejoMostrado.contains("4000"));
        });
        caso("ModificarVentaControlador busca ajusta y presenta el nuevo total", () -> {
            como(jefe);
            VistaPrueba v = new VistaPrueba();
            v.ventaId = Integer.toString(venta.getId());
            v.nuevoTotal = "840000";
            controladores.crearModificarVenta(v);
            v.pulsar(AccionVista.BUSCAR);
            v.pulsar(AccionVista.MODIFICAR);
            correcto(v);
            monto("840000", v.ventaMostrada.getTotal());
            verificar(new ProductoDAO(conexion).buscarPorId(producto.getId()).getStock() == 25);
        });
        caso("HistorialControlador lista y filtra la evidencia guardada", () -> {
            como(jefe);
            VistaPrueba v = new VistaPrueba();
            controladores.crearHistorial(v);
            v.pulsar(AccionVista.CARGAR);
            v.pulsar(AccionVista.FILTRAR);
            correcto(v);
            verificar(v.registrosMostrados.size() == 1);
            monto("850000", v.registrosMostrados.get(0).getValorAnterior());
        });
        caso("UsuarioControlador restablece una clave mediante el servicio", () -> {
            como(admin);
            VistaPrueba v = new VistaPrueba();
            v.id = cajero.getId();
            v.clave = "ClaveNueva123".toCharArray();
            controladores.crearUsuario(v);
            v.pulsar(AccionVista.RESTABLECER_CLAVE);
            correcto(v);
            Usuario leido = new UsuarioDAO(conexion).buscarPorId(cajero.getId());
            verificar(servicios.getAutenticacion().verificarClave("ClaveNueva123", leido.getPasswordHash()));
        });
        caso("CierreControlador calcula la caja neta y presenta el descuadre", () -> {
            como(jefe);
            VistaPrueba v = new VistaPrueba();
            v.monto = "778500";
            controladores.crearCierre(v);
            v.pulsar(AccionVista.CALCULAR);
            correcto(v);
            monto("779000", v.efectivoMostrado);
            v.pulsar(AccionVista.CERRAR);
            correcto(v);
            monto("-500", v.cierreMostrado.getDiferencia());
        });
        caso("Controlador presenta DiaCerrado sin anunciar un nuevo cobro", () -> {
            como(cajero);
            VistaPrueba v = new VistaPrueba();
            controladores.crearVenta(v);
            v.pulsar(AccionVista.NUEVA_VENTA);
            verificar(v.error.contains("cerrada") && v.ventaCobrada == null && !v.ocupada);
        });
        caso("Administracion lista productos desactivados y conserva historial", () -> {
            como(admin);
            VistaPrueba v = new VistaPrueba();
            v.id = producto.getId();
            controladores.crearProducto(v);
            v.pulsar(AccionVista.DESACTIVAR);
            v.pulsar(AccionVista.LISTAR);
            correcto(v);
            verificar(v.productosMostrados.size() == 1 && !v.productosMostrados.get(0).isActivo());
            monto("840000", servicios.getBalance().obtenerBalanceDelDia(LocalDate.of(2026, 11, 6)).getIngresos());
        });
        caso("PrincipalControlador cierra sesion y solicita volver al login", () -> {
            como(jefe);
            VistaPrueba v = new VistaPrueba();
            controladores.crearPrincipal(v, NAVEGACION);
            v.pulsar(AccionVista.CERRAR_SESION);
            correcto(v);
            verificar(!Sesion.getInstancia().hayUsuario() && NAVEGACION.login);
        });
    }

    // Se reutiliza la limpieza de pruebas anterior y se mantienen intactas las tablas reales.
    public static void main(String[] args) throws Exception {
        if (RevisionControlador.ejecutar() > 0) System.exit(1);
        if (args.length > 0 && args[0].equals("--sin-mysql")) return;
        try {
            BaseTemporal.ejecutar("balance_controlador_prueba_", (c, esquema) -> {
                conexion = c;
                servicios = new ServiciosAplicacion(new UsuarioDAO(c), new ProductoDAO(c), new VentaDAO(c), new CompraDAO(c),
                        new GastoDAO(c), new CostoFijoDAO(c), new ConfiguracionDAO(c), new CierreDiarioDAO(c), new HistorialCambiosDAO(c), RELOJ);
                try (AplicacionControladores aplicacion = new AplicacionControladores(servicios, new RevisionControlador.Inmediato())) {
                    controladores = aplicacion;
                    flujo();
                }
            });
        } finally {
            Sesion.getInstancia().cerrar();
        }
        System.out.println("RESULTADO CONTROLADOR MYSQL: " + correctas + " correctas, " + fallidas + " fallidas");
        if (fallidas > 0) System.exit(1);
    }
}
