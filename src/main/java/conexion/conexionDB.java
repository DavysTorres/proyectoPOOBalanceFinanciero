package conexion;

import excepciones.AccesoDatosException;
import util.ConfiguracionEntorno;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Unico Singleton que abre y cierra la conexion de la aplicacion de escritorio.
 */
public final class conexionDB {
    // Se conserva el nombre publico existente para no romper las referencias del proyecto.
    private static final conexionDB INSTANCE = new conexionDB();
    private Connection connection;
    private boolean transaccionActiva;

    // Obtener el Singleton no conecta a MySQL; se conecta solo cuando se solicita.
    private conexionDB() {
    }

    // La inicializacion estatica de Java garantiza una sola instancia.
    public static conexionDB getInstance() {
        return INSTANCE;
    }

    // Reutiliza la conexion abierta y nunca reemplaza una transaccion explicita perdida.
    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            if (transaccionActiva)
                throw new SQLException("La conexion de la transaccion se cerro; no se puede continuar");
            connection = abrirConexion();
        }
        return connection;
    }

    // Lee la configuracion existente sin imprimir credenciales ni ocultar errores JDBC.
    private Connection abrirConexion() throws SQLException {
        ConfiguracionEntorno entorno = new ConfiguracionEntorno();
        String url = configuracionObligatoria(entorno, "DB_URL");
        String usuario = configuracionObligatoria(entorno, "DB_USER");
        String password = configuracionObligatoria(entorno, "DB_PASSWORD");
        // JDBC descubre automaticamente el driver MySQL que ya esta en el classpath.
        return DriverManager.getConnection(url, usuario, password);
    }

    // La configuracion se consulta al conectar, por lo que cerrar no necesita leer .env.
    private static String configuracionObligatoria(ConfiguracionEntorno entorno, String clave) {
        String valor = entorno.get(clave);
        if (valor == null || valor.isBlank())
            throw new IllegalStateException("Configura " + clave + " en .env o en el entorno");
        return valor;
    }

    // El servicio puede agrupar varios DAO; no se permite iniciar otra transaccion encima.
    public synchronized void iniciarTransaccion() {
        try {
            Connection c = getConnection();
            if (transaccionActiva || !c.getAutoCommit())
                throw new IllegalStateException("Ya existe una transaccion activa");
            c.setAutoCommit(false);
            transaccionActiva = true;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo iniciar la transaccion", e);
        }
    }

    // Exige la conexion original; confirmar nunca abre una conexion nueva por accidente.
    private Connection conexionDeTransaccion() throws SQLException {
        if (!transaccionActiva) throw new IllegalStateException("No hay transaccion activa");
        if (connection == null || connection.isClosed())
            throw new SQLException("La conexion de la transaccion no esta disponible");
        return connection;
    }

    // Confirma solo la transaccion explicita iniciada mediante este administrador.
    public synchronized void confirmar() {
        try {
            Connection c = conexionDeTransaccion();
            c.commit();
            c.setAutoCommit(true);
            transaccionActiva = false;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo confirmar la transaccion", e);
        }
    }

    // Revierte antes de restaurar autocommit para no confirmar cambios pendientes.
    public synchronized void revertir() {
        try {
            Connection c = conexionDeTransaccion();
            c.rollback();
            c.setAutoCommit(true);
            transaccionActiva = false;
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo revertir la transaccion", e);
        }
    }

    // No abre conexiones al cerrar y descarta cualquier trabajo aun sin confirmar.
    public synchronized void desconectar() {
        try {
            if (connection != null && !connection.isClosed()) {
                if (!connection.getAutoCommit()) connection.rollback();
                connection.close();
            }
            connection = null;
            transaccionActiva = false;
        } catch (SQLException e) {
            // Si el rollback falla, se intenta cerrar sin activar autocommit.
            try {
                if (connection != null) connection.close();
            } catch (SQLException cierre) {
                e.addSuppressed(cierre);
            }
            throw new AccesoDatosException("No se pudo cerrar correctamente la conexion", e);
        }
    }
}
