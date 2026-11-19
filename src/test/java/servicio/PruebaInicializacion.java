package servicio;

import dao.*;
import modelo.*;
import excepciones.AccesoDatosException;
import soporte.BaseTemporal;

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
        BaseTemporal.ejecutar("balance_vacio_prueba_", (c, nombre) -> {
            for (String tabla : new String[]{"usuarios", "productos", "ventas", "compras", "gastos", "costos_fijos", "cierres_diarios", "configuracion"}) {
                try (java.sql.Statement consulta = c.createStatement();
                     java.sql.ResultSet filas = consulta.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
                    filas.next();
                    if (filas.getInt(1) != 0) throw new AssertionError("La instalacion contiene datos de fabrica: " + tabla);
                }
            }
            System.out.println("OK: Instalacion vacia sin datos financieros ni usuarios de fabrica");
        });
        Sesion.getInstancia().cerrar();
        System.out.println("RESULTADO INICIALIZACION SIN DATOS DE FABRICA: 4 correctas");
    }
}
