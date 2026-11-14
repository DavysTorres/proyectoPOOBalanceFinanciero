package dao;

import excepciones.AccesoDatosException;
import conexion.conexionDB;

import modelo.*;

import java.math.BigDecimal;


import java.sql.*;
import java.time.*;


/**
 * Pruebas reales JDBC en una base temporal; nunca crea tablas en la base de la aplicacion.
 */
public class PruebaDAO {
    private static int correctas, fallidas;
    private static Connection c;
    private static Usuario usuario;
    private static Producto producto;
    private static Venta venta;
    private static final LocalDate FECHA = LocalDate.of(2026, 11, 6);

    // Cada caso puede fallar sin ocultar los resultados de los siguientes.
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
            // No imprime mensajes JDBC que pudieran incluir URLs o configuracion privada.
            System.out.println("FALLO: " + nombre + " (" + e.getClass().getSimpleName() + ")");
        }
    }

    // Aserciones sencillas sin JUnit ni herramientas de simulacion.
    private static void verificar(boolean ok) {
        if (!ok) throw new AssertionError("Resultado inesperado");
    }

    private static void monto(String esperado, BigDecimal actual) {
        verificar(new BigDecimal(esperado).compareTo(actual) == 0);
    }

    private static void rechaza(Caso prueba) throws Exception {
        try {
            prueba.ejecutar();
        } catch (AccesoDatosException | IllegalArgumentException e) {
            return;
        }
        throw new AssertionError("Se acepto una operacion invalida");
    }

    // Una consulta auxiliar de prueba verifica el efecto real en las tablas.
    private static int contar(String tabla) throws SQLException {
        // Los nombres solo provienen de constantes de este archivo, nunca de entradas externas.
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
            r.next();
            return r.getInt(1);
        }
    }

    // Fabrica objetos nuevos de prueba sin asignar identificadores artificiales.
    private static Producto producto(String nombre, int stock) {
        return new Producto(0, nombre, new BigDecimal("10000"), new BigDecimal("6000"), stock, 2);
    }

    private static Venta nuevaVenta(Producto p, int cantidad) {
        Venta v = new Venta(usuario);
        v.setFechaHora(FECHA.atTime(12, 0));
        v.agregarDetalle(p, cantidad);
        return v;
    }

    // Ejecuta la bateria sobre tablas vacias del esquema temporal.
    private static void pruebas() {
        caso("Guarda y reconstruye los tres perfiles mediante Factory", () -> {
            UsuarioDAO d = new UsuarioDAO(c);
            usuario = new Cajero(0, "Ana", "ana", "hash_solo_prueba", true, FECHA.atStartOfDay());
            d.guardar(usuario);
            Usuario jefe = new Jefe(0, "Luis", "luis", "hash_solo_prueba", true, FECHA.atStartOfDay());
            Usuario admin = new Administrador(0, "Ada", "ada", "hash_solo_prueba", true, FECHA.atStartOfDay());
            d.guardar(jefe);
            d.guardar(admin);
            verificar(usuario.getId() > 0 && d.buscarPorId(usuario.getId()) instanceof Cajero);
            verificar(d.buscarPorId(jefe.getId()) instanceof Jefe);
            verificar(d.buscarPorId(admin.getId()) instanceof Administrador);
        });
        caso("Usuario actualiza datos y hash", () -> {
            UsuarioDAO d = new UsuarioDAO(c);
            usuario.setNombre("Ana Maria");
            d.actualizar(usuario);
            d.actualizarPassword(usuario.getId(), "hash_nuevo_prueba");
            verificar(d.buscarPorUsername("ana").getNombre().equals("Ana Maria"));
            verificar(d.buscarPorId(usuario.getId()).getPasswordHash().equals("hash_nuevo_prueba"));
        });
        caso("Username unico rechaza duplicados", () -> rechaza(() ->
                new UsuarioDAO(c).guardar(new Cajero(0, "Otra", "ana", "hash_prueba", true, FECHA.atStartOfDay()))));
        caso("Consulta parametrizada no interpreta texto como SQL", () ->
                verificar(new UsuarioDAO(c).buscarPorUsername("ana' OR '1'='1") == null));
        caso("Baja logica de usuario conserva su registro", () -> {
            UsuarioDAO d = new UsuarioDAO(c);
            d.cambiarEstado(usuario.getId(), false);
            verificar(d.listarActivos().size() == 2 && d.listarTodos().size() == 3);
            verificar(d.buscarPorId(usuario.getId()) != null);
            d.cambiarEstado(usuario.getId(), true);
        });
        caso("Guarda, busca y actualiza producto sin perder stock", () -> {
            ProductoDAO d = new ProductoDAO(c);
            producto = producto("Cafe", 20);
            d.guardar(producto);
            producto.setNombre("Cafe premium");
            d.actualizar(producto);
            verificar(d.buscarPorNombre("premium").size() == 1);
            verificar(d.buscarPorId(producto.getId()).getStock() == 20);
        });
        caso("Stock bajo, costo y stock manual funcionan", () -> {
            ProductoDAO d = new ProductoDAO(c);
            d.actualizarStock(producto.getId(), 2);
            verificar(d.listarStockBajo().size() == 1);
            d.actualizarStock(producto.getId(), 20);
            d.actualizarCosto(producto.getId(), new BigDecimal("6500"));
            monto("6500", d.buscarPorId(producto.getId()).getCostoCompra());
            d.actualizarCosto(producto.getId(), new BigDecimal("6000"));
        });
        caso("Venta guarda cabecera, detalles y stock en conjunto", () -> {
            venta = nuevaVenta(producto, 2);
            new VentaDAO(c).guardarConDetalles(venta);
            verificar(venta.getId() > 0);
            verificar(new ProductoDAO(c).buscarPorId(producto.getId()).getStock() == 18);
            Venta leida = new VentaDAO(c).buscarPorId(venta.getId());
            monto("20000", leida.getTotal());
            monto("12000", leida.calcularCostoTotal());
            verificar(leida.getDetalles().get(0).getId() > 0);
        });
        caso("Rollback de venta revierte incluso la primera linea", () -> {
            Producto sinStock = producto("Agotado", 0);
            new ProductoDAO(c).guardar(sinStock);
            int antes = contar("ventas"), lineas = contar("detalle_venta");
            Venta v = nuevaVenta(producto, 1);
            v.agregarDetalle(sinStock, 1);
            rechaza(() -> new VentaDAO(c).guardarConDetalles(v));
            verificar(v.getId() == 0 && contar("ventas") == antes && contar("detalle_venta") == lineas);
            verificar(new ProductoDAO(c).buscarPorId(producto.getId()).getStock() == 18);
            verificar(c.getAutoCommit());
        });
        caso("Producto inactivo no se puede vender", () -> {
            Producto p = producto("Inactivo", 10);
            p.setActivo(false);
            new ProductoDAO(c).guardar(p);
            rechaza(() -> new VentaDAO(c).guardarConDetalles(nuevaVenta(p, 1)));
        });
        caso("Compra guarda detalles y aumenta stock y costo", () -> {
            Compra compra = new Compra(usuario, "Proveedor");
            compra.setFechaHora(FECHA.atTime(10, 0));
            compra.agregarDetalle(producto, 3, new BigDecimal("7000"));
            CompraDAO d = new CompraDAO(c);
            d.guardarConDetalles(compra);
            verificar(new ProductoDAO(c).buscarPorId(producto.getId()).getStock() == 21);
            monto("6142.8571", new ProductoDAO(c).buscarPorId(producto.getId()).getCostoCompra());
            monto("21000", d.buscarPorId(compra.getId()).getTotal());
            verificar(d.listarPorFecha(FECHA).size() == 1);
        });
        caso("Venta leida conserva valores historicos tras cambiar catalogo", () -> {
            ProductoDAO p = new ProductoDAO(c);
            Producto actual = p.buscarPorId(producto.getId());
            actual.setPrecioVenta(new BigDecimal("20000"));
            p.actualizar(actual);
            Venta v = new VentaDAO(c).buscarPorId(venta.getId());
            monto("20000", v.getTotal());
            monto("12000", v.calcularCostoTotal());
        });
        caso("Compra con desbordamiento revierte stock y cabecera", () -> {
            Producto p = producto("Limite", Integer.MAX_VALUE);
            new ProductoDAO(c).guardar(p);
            Compra compra = new Compra(usuario, "Proveedor");
            compra.agregarDetalle(producto, 1, BigDecimal.ONE);
            compra.agregarDetalle(p, 1, BigDecimal.ONE);
            int antes = contar("compras");
            rechaza(() -> new CompraDAO(c).guardarConDetalles(compra));
            verificar(contar("compras") == antes && compra.getId() == 0);
            verificar(new ProductoDAO(c).buscarPorId(producto.getId()).getStock() == 21);
        });
        caso("Savepoint no confirma una transaccion del servicio", () -> {
            c.setAutoCommit(false);
            try {
                new VentaDAO(c).guardarConDetalles(nuevaVenta(producto, 1));
                verificar(!c.getAutoCommit());
                c.rollback();
            } finally {
                c.setAutoCommit(true);
            }
            verificar(new ProductoDAO(c).buscarPorId(producto.getId()).getStock() == 21);
        });
        caso("Fallo anidado conserva cambios previos del servicio", () -> {
            c.setAutoCommit(false);
            try {
                new ConfiguracionDAO(c).guardar("prueba_savepoint", "antes");
                rechaza(() -> new VentaDAO(c).guardarConDetalles(nuevaVenta(producto, 9999)));
                verificar(new ConfiguracionDAO(c).obtener("prueba_savepoint").equals("antes"));
                verificar(!c.getAutoCommit());
                c.rollback();
            } finally {
                c.setAutoCommit(true);
            }
            verificar(new ConfiguracionDAO(c).obtener("prueba_savepoint") == null);
        });
        caso("Gastos recuperan su tipo concreto y suma variable", () -> {
            GastoDAO d = new GastoDAO(c);
            Gasto fijo = new GastoFijo(0, usuario, "Arriendo", new BigDecimal("390000"), FECHA, 30);
            Gasto variable = new GastoVariable(0, usuario, "Transporte", new BigDecimal("12000"), FECHA);
            d.guardar(fijo);
            d.guardar(variable);
            verificar(d.buscarPorId(fijo.getId()) instanceof GastoFijo);
            verificar(d.buscarPorId(variable.getId()) instanceof GastoVariable);
            monto("13000", d.buscarPorId(fijo.getId()).calcularMontoDiario());
            monto("12000", d.sumarPorTipoYFecha("VARIABLE", FECHA));
        });
        caso("Costos fijos excluyen inactivos", () -> {
            CostoFijoDAO d = new CostoFijoDAO(c);
            CostoFijo activo = new CostoFijo(0, "Arriendo", new BigDecimal("390000"), true);
            d.guardar(activo);
            d.guardar(new CostoFijo(0, "Viejo", BigDecimal.ONE, false));
            monto("390000", d.sumarMontoMensualActivos());
            activo.setMontoMensual(new BigDecimal("400000"));
            d.actualizar(activo);
            monto("400000", d.buscarPorId(activo.getId()).getMontoMensual());
            verificar(d.listarActivos().size() == 1);
        });
        caso("Configuracion guarda y recupera el negocio completo", () -> {
            ConfiguracionDAO d = new ConfiguracionDAO(c);
            d.guardarNegocio(new Negocio("Tienda", 30, LocalTime.of(8, 0), LocalTime.of(18, 0)));
            Negocio n = d.cargarNegocio();
            verificar(n.getNombre().equals("Tienda") && n.getDiasOperacion() == 30);
            monto("70", n.getUmbralAmarillo());
            monto("15", n.getUmbralMargenBajo());
            d.guardar("nombre", "Tienda nueva");
            verificar(d.cargarNegocio().getNombre().equals("Tienda nueva"));
        });
        caso("Total modificado persiste y se registra su motivo", () -> {
            VentaDAO v = new VentaDAO(c);
            HistorialCambiosDAO h = new HistorialCambiosDAO(c);
            c.setAutoCommit(false);
            try {
                v.actualizarTotal(venta.getId(), new BigDecimal("19000"));
                int id = h.guardar(new RegistroCambio(0, venta, usuario, FECHA.atTime(13, 0),
                        new BigDecimal("20000"), new BigDecimal("19000"), "Descuento autorizado"));
                verificar(id > 0);
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
            monto("19000", v.buscarPorId(venta.getId()).getTotal());
            monto("19000", v.sumarIngresosPorFecha(FECHA));
            monto("12000", v.buscarPorId(venta.getId()).calcularCostoTotal());
            verificar(h.listarPorVenta(venta.getId()).size() == 1 && h.listarPorFecha(FECHA).size() == 1);
            monto("20000", h.listarTodos().get(0).getValorAnterior());
        });
        caso("Cierre conserva diferencia y fecha unica", () -> {
            CierreDiarioDAO d = new CierreDiarioDAO(c);
            CierreDiario cierre = new CierreDiario(0, FECHA, usuario, new BigDecimal("19000"),
                    new BigDecimal("12000"), BigDecimal.ZERO, new BigDecimal("7000"), BigDecimal.ZERO,
                    new BigDecimal("19000"), new BigDecimal("18500"));
            d.guardar(cierre);
            monto("-500", d.buscarPorFecha(FECHA).getDiferencia());
            verificar(d.existeCierre(FECHA) && d.listarPorRango(FECHA, FECHA).size() == 1);
            cierre.setId(0);
            rechaza(() -> d.guardar(cierre));
        });
        caso("Consultas de dia vacio devuelven cero y listas vacias", () -> {
            LocalDate otro = FECHA.minusDays(1);
            monto("0", new VentaDAO(c).sumarIngresosPorFecha(otro));
            verificar(new VentaDAO(c).listarPorFecha(otro).isEmpty());
            verificar(new CompraDAO(c).listarPorFecha(otro).isEmpty());
            verificar(new GastoDAO(c).listarPorFecha(otro).isEmpty());
            verificar(new CierreDiarioDAO(c).buscarPorFecha(otro) == null);
        });
        caso("Listados por fecha excluyen el dia siguiente", () -> {
            Venta v = nuevaVenta(producto, 1);
            v.setFechaHora(FECHA.plusDays(1).atStartOfDay());
            new VentaDAO(c).guardarConDetalles(v);
            verificar(new VentaDAO(c).listarPorFecha(FECHA).size() == 1);
            verificar(new VentaDAO(c).listarPorFecha(FECHA.plusDays(1)).size() == 1);
        });
        caso("Base rechaza stock negativo y claves foraneas inexistentes", () -> {
            try (PreparedStatement ps = c.prepareStatement("UPDATE productos SET stock=-1 WHERE id=?")) {
                ps.setInt(1, producto.getId());
                boolean rechazo = false;
                try {
                    ps.executeUpdate();
                } catch (SQLException e) {
                    rechazo = true;
                }
                verificar(rechazo);
            }
            Usuario inexistente = new Cajero(Integer.MAX_VALUE, "X", "x", "hash", true, FECHA.atStartOfDay());
            Venta v = new Venta(inexistente);
            v.agregarDetalle(producto, 1);
            rechaza(() -> new VentaDAO(c).guardarConDetalles(v));
        });
        caso("Venta guarda y consulta instalaciones con usuario_id sin cambiar sus datos previos", () -> {
            int anteriores = contar("ventas");
            Producto p = producto("Compatibilidad usuario_id", 5);
            new ProductoDAO(c).guardar(p);
            try (Statement cambio = c.createStatement()) { cambio.executeUpdate("ALTER TABLE ventas RENAME COLUMN cajero_id TO usuario_id"); }
            try {
                VentaDAO dao = new VentaDAO(c);
                verificar(dao.columnaCajero().equals("usuario_id"));
                Venta nueva = nuevaVenta(p, 2);
                dao.guardarConDetalles(nueva);
                Venta leida = dao.buscarPorId(nueva.getId());
                verificar(leida.getCajero().getId() == usuario.getId());
                monto("20000", leida.getTotal());
                verificar(new ProductoDAO(c).buscarPorId(p.getId()).getStock() == 3);
                verificar(contar("ventas") == anteriores + 1);
                verificar(dao.listarPorFecha(FECHA).stream().anyMatch(v -> v.getId() == nueva.getId()));
            } finally {
                try (Statement cambio = c.createStatement()) { cambio.executeUpdate("ALTER TABLE ventas RENAME COLUMN usuario_id TO cajero_id"); }
            }
            verificar(new VentaDAO(c).columnaCajero().equals("cajero_id"));
        });
    }

    // Comprueba tambien la ruta real de los DAO sin conexion inyectada.
    // La conexion del Singleton se dirige solo al mismo esquema temporal de la prueba.
    private static void probarAdministradorConexion(String esquemaTemporal) throws SQLException {
        conexionDB administrador = conexionDB.getInstance();
        administrador.desconectar();
        Connection compartida = administrador.getConnection();
        compartida.setCatalog(esquemaTemporal);
        caso("DAO normales comparten el unico administrador de conexion", () -> {
            UsuarioDAO usuarios = new UsuarioDAO();
            ProductoDAO productos = new ProductoDAO();
            verificar(usuarios.conexion() == compartida);
            verificar(productos.conexion() == compartida);
            verificar(administrador.getConnection() == compartida);
            verificar(usuarios.listarTodos().size() == 3);
        });
        caso("Transaccion del administrador permite rollback entre DAO", () -> {
            administrador.iniciarTransaccion();
            try {
                new ConfiguracionDAO().guardar("prueba_administrador", "pendiente");
                verificar(!compartida.getAutoCommit());
            } finally {
                administrador.revertir();
            }
            verificar(compartida.getAutoCommit());
            verificar(new ConfiguracionDAO().obtener("prueba_administrador") == null);
        });
        caso("Transaccion del administrador permite confirmar", () -> {
            administrador.iniciarTransaccion();
            try {
                new ConfiguracionDAO().guardar("prueba_administrador", "confirmada");
                administrador.confirmar();
            } catch (Exception e) {
                administrador.revertir();
                throw e;
            }
            verificar(compartida.getAutoCommit());
            verificar(new ConfiguracionDAO().obtener("prueba_administrador").equals("confirmada"));
        });
        caso("Desconectar descarta cambios y no reconecta al cerrar otra vez", () -> {
            administrador.iniciarTransaccion();
            new ConfiguracionDAO().guardar("prueba_descartar", "pendiente");
            administrador.desconectar();
            verificar(compartida.isClosed());
            administrador.desconectar();
            verificar(new ConfiguracionDAO(c).obtener("prueba_descartar") == null);
        });
        caso("DAO con conexion cerrada falla sin sustituirla silenciosamente", () -> {
            ConfiguracionDAO anterior = new ConfiguracionDAO(compartida);
            rechaza(() -> anterior.obtener("nombre"));
            verificar(anterior.conexion() == compartida);
        });
        caso("Confirmar sin transaccion no abre una conexion nueva", () -> {
            boolean rechazo = false;
            try {
                administrador.confirmar();
            } catch (IllegalStateException e) {
                rechazo = true;
            }
            verificar(rechazo);
        });
    }

    // Crea y elimina solamente un esquema con nombre aleatorio generado en esta ejecucion.
    public static void main(String[] args) throws Exception {
        soporte.BaseTemporal.ejecutar("balance_dao_prueba_", (conexion, temporal) -> {
            c = conexion;
            pruebas();
            try {
                probarAdministradorConexion(temporal);
            } finally {
                conexionDB.getInstance().desconectar();
            }
        });
        System.out.println("RESULTADO DAO: " + correctas + " correctas, " + fallidas + " fallidas");
        if (fallidas > 0) System.exit(1);
    }
}
