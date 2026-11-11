package servicio;

import dao.*;
import modelo.*;
import excepciones.AccesoDatosException;
import soporte.BaseTemporal;

import java.math.BigDecimal;
import java.time.*;

/**
 * Verifica que el primer uso no deje un usuario sin configuracion si algo falla.
 */
public final class PruebaInicializacion {
    public static void main(String[] args) throws Exception {
        BaseTemporal.ejecutar("balance_inicio_prueba_", (c, nombre) -> {
            Clock reloj = Clock.fixed(Instant.parse("2026-11-06T15:00:00Z"), ZoneId.of("America/Bogota"));
            UsuarioDAO usuarios = new UsuarioDAO(c);
            ServiciosAplicacion servicios = new ServiciosAplicacion(usuarios, new ProductoDAO(c), new VentaDAO(c),
                    new CompraDAO(c), new GastoDAO(c), new CostoFijoDAO(c), new ConfiguracionDAO(c),
                    new CierreDiarioDAO(c), new HistorialCambiosDAO(c), reloj);
            Negocio negocio = new Negocio("Tienda", 30, LocalTime.of(8, 0), LocalTime.of(18, 0));
            if (!servicios.getInicializacion().necesitaConfiguracionInicial()) throw new AssertionError();
            ServicioConfiguracion fallo = new ServicioConfiguracion(new ConfiguracionDAO(c), reloj) {
                @Override
                public void guardarNegocio(Negocio n) {
                    throw new AccesoDatosException("Fallo de prueba");
                }
            };
            try {
                new ServicioInicializacion(usuarios, servicios.getUsuarios(), fallo).inicializar("Ada", "ada", "ClaveAdmin123", negocio);
                throw new AssertionError("Se esperaba rollback");
            } catch (AccesoDatosException esperado) {
                if (!usuarios.listarTodos().isEmpty() || Sesion.getInstancia().hayUsuario())
                    throw new AssertionError("Quedaron datos parciales");
            }
            System.out.println("OK: Inicializacion fallida revierte usuario y sesion");
            Usuario u = servicios.getInicializacion().inicializar("Ada", "ada", "ClaveAdmin123", negocio);
            if (u.getId() <= 0 || !Sesion.getInstancia().hayUsuario()) throw new AssertionError();
            if (!servicios.getConfiguracion().obtenerNegocio().getNombre().equals("Tienda")) throw new AssertionError();
            System.out.println("OK: Inicializacion completa guarda administrador y negocio");
            if (servicios.getInicializacion().necesitaConfiguracionInicial()) throw new AssertionError();
            System.out.println("OK: Primer uso no se repite si existen usuarios");
        });
        Sesion.getInstancia().cerrar();
        BaseTemporal.ejecutar("balance_ejemplo_prueba_", (c, nombre) -> {
            String script = java.nio.file.Files.readString(java.nio.file.Path.of("sql/datos_ejemplo.sql")).replaceAll("(?m)^--.*$", "");
            for (String sentencia : script.split(";"))
                if (!sentencia.isBlank())
                    try (java.sql.Statement s = c.createStatement()) {
                        s.execute(sentencia);
                    }
            if (new ProductoDAO(c).listarTodos().size() != 15 || new UsuarioDAO(c).listarTodos().size() != 3)
                throw new AssertionError();
            if (new CostoFijoDAO(c).listarActivos().size() != 3) throw new AssertionError();
            ServicioAutenticacion auth = new ServicioAutenticacion(new UsuarioDAO(c), Clock.systemDefaultZone());
            if (auth.iniciarSesion("demo_admin", "DemoPoo2026!").getRol() != Rol.ADMINISTRADOR)
                throw new AssertionError();
            if (new VentaDAO(c).sumarIngresosPorFecha(LocalDate.of(2026, 11, 5)).compareTo(new BigDecimal("30000")) != 0)
                throw new AssertionError();
            if (new VentaDAO(c).listarPorFecha(LocalDate.of(2026, 11, 6)).size() != 1) throw new AssertionError();
            System.out.println("OK: Datos de ejemplo incluyen roles, 15 productos, costos y dos dias con ventas");
        });
        Sesion.getInstancia().cerrar();
        System.out.println("RESULTADO INICIALIZACION Y EJEMPLO: 4 correctas");
    }
}
