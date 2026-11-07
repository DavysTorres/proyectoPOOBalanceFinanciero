package servicio;

import dao.UsuarioDAO;
import excepciones.*;
import modelo.*;

import java.math.BigDecimal;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

/**
 * Casos unitarios de negocio sin MySQL, JUnit ni librerias de simulacion.
 */
public final class RevisionServicio {
    private static int correctas, fallidas;
    private static final LocalDate FECHA = LocalDate.of(2026, 11, 6);
    private static final CalculadoraFinanciera CALC = new CalculadoraFinanciera();

    private interface Caso {
        void ejecutar() throws Exception;
    }

    // Aisla la sesion en cada caso para detectar accesos no autorizados.
    private static void probar(String nombre, Caso caso) {
        Sesion.getInstancia().cerrar();
        try {
            caso.ejecutar();
            correctas++;
            System.out.println("OK: " + nombre);
        } catch (Throwable e) {
            fallidas++;
            System.out.println("FALLO: " + nombre + " -> " + e);
        } finally {
            Sesion.getInstancia().cerrar();
        }
    }

    private static void verificar(boolean condicion) {
        if (!condicion) throw new AssertionError("Resultado inesperado");
    }

    private static BigDecimal d(String valor) {
        return new BigDecimal(valor);
    }

    private static void monto(String esperado, BigDecimal actual) {
        verificar(d(esperado).compareTo(actual) == 0);
    }

    private static void rechaza(Class<? extends Throwable> tipo, Caso caso) throws Exception {
        try {
            caso.ejecutar();
        } catch (Throwable e) {
            if (tipo.isInstance(e)) return;
            throw new AssertionError("Excepcion inesperada: " + e);
        }
        throw new AssertionError("La operacion debio rechazarse");
    }

    // Datos basicos para comprobar herencia y permisos sin persistencia.
    private static Cajero cajero() {
        return new Cajero(1, "Ana", "ana", "hash_prueba", true, FECHA.atStartOfDay());
    }

    private static Administrador admin() {
        return new Administrador(2, "Ada", "ada", "hash_prueba", true, FECHA.atStartOfDay());
    }

    private static Producto producto() {
        return new Producto(1, "Cafe", d("10000"), d("6000"), 100, 2);
    }

    private static Negocio negocio() {
        return new Negocio("Tienda", 30, LocalTime.of(8, 0), LocalTime.of(18, 0));
    }

    private static BalanceDia balance(String ingresos, String costos, String fijos, String equilibrio) {
        BigDecimal i = d(ingresos), v = d(costos), f = d(fijos), p = d(equilibrio);
        return new BalanceDia(FECHA, i, v, i.subtract(v), f, i.subtract(v).subtract(f), p,
                CALC.calcularFaltante(p, i), CALC.calcularPorcentajeAvance(i, p));
    }

    // Sustituye solamente la busqueda de usuarios; sigue usandose la autenticacion real.
    private static final class UsuariosMemoria extends UsuarioDAO {
        private final Usuario usuario;
        private int cambiosClave;

        UsuariosMemoria(Usuario usuario) {
            this.usuario = usuario;
        }

        @Override
        public Usuario buscarPorUsername(String nombre) {
            return nombre.equalsIgnoreCase(usuario.getUsername()) ? usuario : null;
        }

        @Override
        public void actualizarPassword(int id, String hash) {
            verificar(id == usuario.getId());
            cambiosClave++;
            usuario.setPasswordHash(hash);
        }
    }

    // Reloj mutable de prueba: evita sleeps para verificar los cinco minutos de bloqueo.
    private static final class Reloj extends Clock {
        private Instant instante = Instant.parse("2026-11-06T15:00:00Z");

        public ZoneId getZone() {
            return ZoneId.of("America/Bogota");
        }

        public Clock withZone(ZoneId zona) {
            return Clock.fixed(instante, zona);
        }

        public Instant instant() {
            return instante;
        }

        void avanzar(Duration duracion) {
            instante = instante.plus(duracion);
        }
    }

    // Devuelve el numero de fallos para que el script pueda detenerse antes de MySQL.
    public static int ejecutar() throws Exception {
        probar("Cuentas SHA-256 antiguas migran solo con la clave correcta", () -> {
            String[] claves = {"admin123", "jefe123", "cajero123"};
            String[] hashes = {
                    "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9",
                    "e577c004a29a6a084d8e9bc25242bfe08eee016313058aea0cb987f91bf7b1f8",
                    "1ed4353e845e2e537e017c0fac3a0d402d231809b7989e90da15191c1148a93f"};
            for (int i = 0; i < claves.length; i++) {
                Usuario u = cajero();
                u.setPasswordHash(hashes[i]);
                UsuariosMemoria dao = new UsuariosMemoria(u);
                ServicioAutenticacion a = new ServicioAutenticacion(dao, new Reloj());
                rechaza(CredencialesInvalidasException.class, () -> a.iniciarSesion("ana", "incorrecta"));
                verificar(dao.cambiosClave == 0 && u.getPasswordHash().equals(hashes[i]));
                verificar(a.iniciarSesion("ana", claves[i]) == u);
                verificar(dao.cambiosClave == 1 && u.getPasswordHash().startsWith("pbkdf2-sha256$"));
                verificar(a.verificarClave(claves[i], u.getPasswordHash()));
                a.iniciarSesion("ana", claves[i]);
                verificar(dao.cambiosClave == 1);
            }
        });
        probar("Sesion exige inicio y permiso", () -> {
            rechaza(PermisoDenegadoException.class, () -> Sesion.getInstancia().exigirPermiso(Permiso.REGISTRAR_VENTA));
            Sesion.getInstancia().iniciar(cajero());
            Sesion.getInstancia().exigirPermiso(Permiso.REGISTRAR_VENTA);
            rechaza(PermisoDenegadoException.class, () -> Sesion.getInstancia().exigirPermiso(Permiso.REGISTRAR_COMPRA));
        });
        probar("Sesion rechaza usuario desactivado", () -> {
            Usuario u = cajero();
            u.desactivar();
            rechaza(PermisoDenegadoException.class, () -> Sesion.getInstancia().iniciar(u));
        });
        probar("No hay permisos despues de cerrar sesion", () -> {
            Sesion.getInstancia().iniciar(admin());
            Sesion.getInstancia().cerrar();
            verificar(!Sesion.getInstancia().hayUsuario());
            rechaza(PermisoDenegadoException.class, () -> new ServicioUsuarios().listarUsuarios());
        });
        probar("Cajero no administra productos ni costos", () -> {
            Sesion.getInstancia().iniciar(cajero());
            rechaza(PermisoDenegadoException.class, () -> new ServicioProductos().crearProducto("X", d("10"), d("5"), 1, 0));
            rechaza(PermisoDenegadoException.class, () -> new ServicioCostosFijos().crear("X", d("10")));
            rechaza(PermisoDenegadoException.class, () -> new ServicioConfiguracion().obtenerNegocio());
        });
        probar("Cajero no consulta cifras ni historial", () -> {
            Sesion.getInstancia().iniciar(cajero());
            rechaza(PermisoDenegadoException.class, () -> new ServicioBalance().obtenerBalanceDelDia(FECHA));
            rechaza(PermisoDenegadoException.class, () -> new ServicioHistorial().listarTodos());
        });
        probar("Cajero no registra compras, modifica ventas ni cierra", () -> {
            Sesion.getInstancia().iniciar(cajero());
            rechaza(PermisoDenegadoException.class, () -> new ServicioCompras().registrarCompra(new Compra(cajero(), "P")));
            rechaza(PermisoDenegadoException.class, () -> new ServicioVentas().modificarVenta(1, d("1"), "Motivo"));
            rechaza(PermisoDenegadoException.class, () -> new ServicioCierre().cerrarDia(FECHA, d("0")));
        });
        probar("Calculadora reproduce las formulas del ejemplo", () -> {
            Venta v = new Venta(cajero());
            v.agregarDetalle(producto(), 85);
            BigDecimal ingresos = CALC.calcularIngresos(Arrays.asList(v));
            BigDecimal costos = CALC.calcularCostosVariables(Arrays.asList(v), Collections.emptyList());
            BigDecimal margen = CALC.calcularMargenContribucion(ingresos, costos);
            monto("850000", ingresos);
            monto("510000", costos);
            monto("340000", margen);
            monto("0.4", CALC.calcularRazonMargen(margen, ingresos));
            monto("975000", CALC.calcularPuntoEquilibrio(d("390000"), d("0.4")));
            monto("125000", CALC.calcularFaltante(d("975000"), ingresos));
            monto("87.18", CALC.calcularPorcentajeAvance(ingresos, d("975000")));
            monto("-50000", CALC.calcularResultado(margen, d("390000")));
        });
        probar("Costo variable excluye gastos fijos y no usa compras", () -> {
            Venta v = new Venta(cajero());
            v.agregarDetalle(producto(), 1);
            List<Gasto> gastos = Arrays.asList(new GastoVariable(0, cajero(), "Transporte", d("1000"), FECHA),
                    new GastoFijo(0, cajero(), "Arriendo", d("30000"), FECHA, 30));
            monto("7000", CALC.calcularCostosVariables(Arrays.asList(v), gastos));
        });
        probar("Ingresos respetan total ajustado e historial de costos", () -> {
            Venta v = new Venta(cajero());
            v.agregarDetalle(producto(), 2);
            v.restaurarTotalRegistrado(d("19000"));
            monto("19000", CALC.calcularIngresos(Arrays.asList(v)));
            monto("12000", CALC.calcularCostosVariables(Arrays.asList(v), Collections.emptyList()));
        });
        probar("Sin operaciones las sumas y la razon son cero", () -> {
            monto("0", CALC.calcularIngresos(Collections.emptyList()));
            monto("0", CALC.calcularCostosVariables(Collections.emptyList(), Collections.emptyList()));
            monto("0", CALC.calcularRazonMargen(d("0"), d("0")));
        });
        probar("Costo fijo diario valida divisor y redondea", () -> {
            monto("33.33", CALC.calcularCostoFijoDiario(d("100"), 3));
            rechaza(DatosInvalidosException.class, () -> CALC.calcularCostoFijoDiario(d("1"), 0));
            rechaza(DatosInvalidosException.class, () -> CALC.calcularCostoFijoDiario(d("1"), 32));
        });
        probar("Equilibrio rechaza razon cero o negativa", () -> {
            rechaza(DatosInvalidosException.class, () -> CALC.calcularPuntoEquilibrio(d("100"), d("0")));
            rechaza(DatosInvalidosException.class, () -> CALC.calcularPuntoEquilibrio(d("100"), d("-0.2")));
        });
        probar("Faltante y avance manejan meta cubierta y costos cero", () -> {
            monto("0", CALC.calcularFaltante(d("100"), d("150")));
            monto("150", CALC.calcularPorcentajeAvance(d("150"), d("100")));
            monto("100", CALC.calcularPorcentajeAvance(d("0"), d("0")));
        });
        probar("Proyeccion usa fraccion temporal y rechaza division por cero", () -> {
            monto("200", CALC.proyectarCierre(d("100"), 0.5));
            rechaza(DatosInvalidosException.class, () -> CALC.proyectarCierre(d("100"), 0));
            rechaza(DatosInvalidosException.class, () -> CALC.proyectarCierre(d("100"), Double.NaN));
        });
        probar("Semaforo distingue rojo amarillo y verde", () -> {
            Diagnostico d = new Diagnostico();
            verificar(d.evaluar(balance("69", "0", "100", "100"), negocio()) instanceof EstadoRojo);
            verificar(d.evaluar(balance("70", "0", "100", "100"), negocio()) instanceof EstadoAmarillo);
            verificar(d.evaluar(balance("100", "0", "100", "100"), negocio()) instanceof EstadoVerde);
        });
        probar("Semaforo no marca verde una meta no calculable", () -> {
            BalanceDia b = new BalanceDia(FECHA, d("0"), d("0"), d("0"), d("100"), d("-100"),
                    d("0"), d("0"), d("0"), false, false);
            verificar(new Diagnostico().evaluar(b, negocio()) instanceof EstadoRojo);
            verificar(new EstadoRojo().getMensaje(b).contains("No hay margen"));
        });
        probar("Balance invalida una meta numerica cuando no es calculable", () ->
                rechaza(IllegalArgumentException.class, () -> new BalanceDia(FECHA, d("0"), d("0"), d("0"),
                        d("100"), d("-100"), d("10"), d("0"), d("0"), false, false)));
        probar("Fraccion del horario se acota antes durante y despues", () -> {
            Diagnostico d = new Diagnostico();
            verificar(d.fraccionDelDia(LocalTime.of(7, 0), negocio()) == 0);
            verificar(d.fraccionDelDia(LocalTime.of(13, 0), negocio()) == 0.5);
            verificar(d.fraccionDelDia(LocalTime.of(23, 0), negocio()) == 1);
        });
        probar("Proyeccion antes de abrir no falla", () ->
                verificar(new Diagnostico().generarMensajeProyeccion(balance("0", "0", "100", "100"), 0).contains("no ha comenzado")));
        probar("Saldo esperado negativo no se oculta como cero", () -> {
            CierreDiario c = new CierreDiario(0, FECHA, cajero(), d("0"), d("10"), d("0"), d("-10"),
                    d("0"), d("-10"), d("0"));
            monto("10", c.getDiferencia());
            verificar(c.hayDescuadre());
            rechaza(IllegalArgumentException.class, () -> c.setEfectivoContado(d("-1")));
        });
        probar("Ranking no cambia la lista original", () -> {
            Sesion.getInstancia().iniciar(admin());
            Producto alto = new Producto(2, "Alto", d("100"), d("10"), 1, 0), bajo = producto();
            List<Producto> original = new ArrayList<>(Arrays.asList(bajo, alto));
            List<Producto> ranking = new AnalizadorMargen().generarRanking(original);
            verificar(ranking.get(0) == alto && original.get(0) == bajo);
        });
        probar("CSV verifica permisos antes de tocar el archivo", () -> {
            Sesion.getInstancia().iniciar(cajero());
            rechaza(PermisoDenegadoException.class, () -> new ServicioExportacion().exportar(balance("1", "0", "0", "0"), "nunca.csv"));
        });
        probar("CSV exporta UTF8 y no sobrescribe otro informe", () -> {
            Sesion.getInstancia().iniciar(admin());
            Path carpeta = Files.createTempDirectory("balance_csv_"), archivo = carpeta.resolve("informe.csv");
            try {
                ServicioExportacion exportacion = new ServicioExportacion();
                exportacion.exportar(balance("850000", "510000", "390000", "975000"), archivo.toString());
                String csv = Files.readString(archivo, StandardCharsets.UTF_8);
                verificar(csv.startsWith("\uFEFFfecha;") && csv.contains("975000"));
                rechaza(IllegalStateException.class, () -> exportacion.exportar(balance("1", "0", "0", "0"), archivo.toString()));
                rechaza(DatosInvalidosException.class, () -> exportacion.exportar(balance("1", "0", "0", "0"), "informe.xlsx"));
            } finally {
                Files.deleteIfExists(archivo);
                Files.deleteIfExists(carpeta);
            }
        });
        probar("Hashes usan sal y verifican espacios sin recortarlos", () -> {
            ServicioAutenticacion a = new ServicioAutenticacion();
            String hash = a.cifrarClave(" Clave123 ");
            verificar(!hash.equals(a.cifrarClave(" Clave123 ")));
            verificar(a.verificarClave(" Clave123 ", hash));
            verificar(!a.verificarClave("Clave123", hash));
            verificar(!a.verificarClave("hash_prueba", "hash_prueba"));
        });
        probar("Clave corta y hash malformado se rechazan", () -> {
            ServicioAutenticacion a = new ServicioAutenticacion();
            rechaza(DatosInvalidosException.class, () -> a.cifrarClave("123"));
            verificar(!a.verificarClave("Clave123", "pbkdf2-sha256$600000$???$???"));
        });
        probar("Bloqueo temporal no se evade cambiando mayusculas", () -> {
            Reloj reloj = new Reloj();
            Usuario u = cajero();
            ServicioAutenticacion a = new ServicioAutenticacion(new UsuariosMemoria(u), reloj);
            u.setPasswordHash(a.cifrarClave("Clave123"));
            rechaza(CredencialesInvalidasException.class, () -> a.iniciarSesion("ana", "mala"));
            rechaza(CredencialesInvalidasException.class, () -> a.iniciarSesion("ANA", "mala"));
            rechaza(UsuarioBloqueadoException.class, () -> a.iniciarSesion("Ana", "mala"));
            rechaza(UsuarioBloqueadoException.class, () -> a.iniciarSesion("ana", "Clave123"));
            reloj.avanzar(Duration.ofMinutes(5));
            verificar(a.iniciarSesion("ana", "Clave123").getId() == u.getId());
        });
        probar("Autenticacion invalida no conserva una sesion anterior", () -> {
            Usuario u = cajero();
            ServicioAutenticacion a = new ServicioAutenticacion(new UsuariosMemoria(u), new Reloj());
            Sesion.getInstancia().iniciar(admin());
            rechaza(CredencialesInvalidasException.class, () -> a.iniciarSesion("desconocido", "cualquiera"));
            verificar(!Sesion.getInstancia().hayUsuario());
        });
        probar("Usuario inactivo no inicia sesion aunque conozca la clave", () -> {
            Usuario u = cajero();
            ServicioAutenticacion a = new ServicioAutenticacion(new UsuariosMemoria(u), new Reloj());
            u.setPasswordHash(a.cifrarClave("Clave123"));
            u.desactivar();
            rechaza(CredencialesInvalidasException.class, () -> a.iniciarSesion("ana", "Clave123"));
        });
        System.out.println("RESULTADO SERVICIO UNITARIO: " + correctas + " correctas, " + fallidas + " fallidas");
        return fallidas;
    }

    // Punto de entrada opcional para verificar solo logica sin base de datos.
    public static void main(String[] args) throws Exception {
        if (ejecutar() > 0) System.exit(1);
    }
}
