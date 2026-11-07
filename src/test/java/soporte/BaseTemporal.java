package soporte;

import util.ConfiguracionEntorno;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.util.UUID;

/**
 * Infraestructura compartida de pruebas: evita duplicar la creacion y limpieza del esquema.
 */
public final class BaseTemporal {
    private BaseTemporal() {
    }

    // El escenario recibe una conexion dirigida exclusivamente a la base temporal.
    @FunctionalInterface
    public interface Escenario {
        void ejecutar(Connection conexion, String esquema) throws Exception;
    }

    // No toca tablas de la base configurada en DB_URL; solo crea y elimina un esquema aleatorio.
    public static void ejecutar(String prefijo, Escenario escenario) throws Exception {
        if (!prefijo.matches("balance_[a-z]+_prueba_"))
            throw new IllegalArgumentException("Prefijo de prueba invalido");
        ConfiguracionEntorno env = new ConfiguracionEntorno();
        String url = env.get("DB_URL"), usuario = env.get("DB_USER"), clave = env.get("DB_PASSWORD");
        if (url == null || usuario == null || clave == null)
            throw new IllegalStateException("Falta configurar DB_URL, DB_USER o DB_PASSWORD para las pruebas");
        String temporal = prefijo + UUID.randomUUID().toString().replace("-", "");
        boolean creada = false;
        DriverManager.setLoginTimeout(5);
        try (Connection c = DriverManager.getConnection(url, usuario, clave)) {
            try {
                try (Statement s = c.createStatement()) {
                    s.executeUpdate("CREATE DATABASE " + temporal);
                }
                creada = true;
                c.setCatalog(temporal);
                String esquema = Files.readString(Path.of("sql/esquema.sql"), StandardCharsets.UTF_8).replaceAll("(?m)^--.*$", "");
                for (String sql : esquema.split(";"))
                    if (!sql.trim().isEmpty())
                        try (Statement s = c.createStatement()) {
                            s.execute(sql);
                        }
                escenario.ejecutar(c, temporal);
            } finally {
                if (creada && temporal.matches(prefijo + "[a-f0-9]{32}")) {
                    if (!c.getAutoCommit()) {
                        c.rollback();
                        c.setAutoCommit(true);
                    }
                    try (Statement s = c.createStatement()) {
                        s.executeUpdate("DROP DATABASE " + temporal);
                    }
                    System.out.println("Base temporal eliminada; base original sin cambios.");
                }
            }
        } catch (SQLException e) {
            // No imprime URLs, usuarios ni detalles privados del driver.
            throw new IllegalStateException("No se completo la prueba MySQL; SQLState=" + e.getSQLState());
        }
    }
}
