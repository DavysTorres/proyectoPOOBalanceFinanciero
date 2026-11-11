package dao;

import excepciones.AccesoDatosException;
import conexion.conexionDB;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Reutiliza JDBC mediante herencia; los DAO concretos deciden como reconstruir cada entidad.
 */
public abstract class DAOBase<T> {
    private Connection conexion;

    // El constructor normal obtiene la conexion solo al ejecutar la primera operacion.
    protected DAOBase() {
        this.conexion = null;
    }

    // Inyeccion sencilla para compartir transacciones y probar con una base aislada.
    protected DAOBase(Connection conexion) {
        this.conexion = Objects.requireNonNull(conexion);
    }

    // Conserva la misma conexion durante toda la vida del DAO, incluida una transaccion.
    // Si se cierra, debe crearse otro DAO; no se reconecta a mitad de una operacion.
    protected final Connection conexion() {
        if (conexion == null) {
            try {
                conexion = conexionDB.getInstance().getConnection();
            } catch (SQLException e) {
                throw new AccesoDatosException("No se pudo obtener la conexion del DAO", e);
            }
        }
        return conexion;
    }

    // Polimorfismo: cada entidad interpreta las columnas que le corresponden.
    protected abstract T mapear(ResultSet fila) throws SQLException;

    // PreparedStatement separa datos del SQL y evita concatenar entradas del usuario.
    private void parametros(PreparedStatement ps, Object... valores) throws SQLException {
        for (int i = 0; i < valores.length; i++) {
            Object valor = valores[i];
            if (valor instanceof LocalDate) valor = Date.valueOf((LocalDate) valor);
            if (valor instanceof LocalDateTime) valor = Timestamp.valueOf((LocalDateTime) valor);
            ps.setObject(i + 1, valor);
        }
    }

    // Cierra sentencia y resultados incluso si falla la reconstruccion de una fila.
    protected final List<T> consultar(String sql, Object... valores) {
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            parametros(ps, valores);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> entidades = new ArrayList<>();
                while (rs.next()) entidades.add(mapear(rs));
                return entidades;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar informacion", e);
        }
    }

    // Una busqueda individual devuelve null si no existe; una lista vacia nunca es un error.
    protected final T uno(String sql, Object... valores) {
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            parametros(ps, valores);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo consultar el registro", e);
        }
    }

    // Ejecuta cambios parametrizados sin apropiarse de la conexion.
    protected final int ejecutar(String sql, Object... valores) {
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            parametros(ps, valores);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo guardar el cambio", e);
        }
    }

    // Recupera la clave generada por MySQL para asociar detalles y cabeceras.
    protected final int insertar(String sql, Object... valores) {
        try (PreparedStatement ps = conexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            parametros(ps, valores);
            if (ps.executeUpdate() != 1) throw new SQLException("No se inserto exactamente una fila");
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("No se recibio el id generado");
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo insertar el registro", e);
        }
    }

    // Las sumas sin filas se interpretan como cero mediante COALESCE en el SQL.
    protected final java.math.BigDecimal sumar(String sql, Object... valores) {
        try (PreparedStatement ps = conexion().prepareStatement(sql)) {
            parametros(ps, valores);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("La consulta no devolvio el acumulado");
                return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo calcular el acumulado", e);
        }
    }

    // El trabajo puede contener varios DAO y debe completarse o revertirse en conjunto.
    @FunctionalInterface
    public interface Trabajo<R> {
        R ejecutar();
    }

    // Si el servicio ya inicio una transaccion, usa un savepoint sin confirmar cambios ajenos.
    public final <R> R transaccion(Trabajo<R> trabajo) {
        Connection c = conexion();
        boolean propia;
        Savepoint punto;
        try {
            propia = c.getAutoCommit();
            if (propia) {
                c.setAutoCommit(false);
                punto = null;
            } else punto = c.setSavepoint();
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo iniciar la operacion", e);
        }
        RuntimeException fallo = null;
        boolean restaurarModo = true;
        try {
            R resultado = trabajo.ejecutar();
            if (propia) c.commit();
            else c.releaseSavepoint(punto);
            return resultado;
        } catch (SQLException | RuntimeException e) {
            fallo = e instanceof RuntimeException ? (RuntimeException) e
                    : new AccesoDatosException("No se pudo confirmar la operacion", e);
            try {
                if (propia) c.rollback();
                else c.rollback(punto);
            } catch (SQLException rollback) {
                // Activar autocommit tras un rollback fallido podria confirmar cambios pendientes.
                restaurarModo = false;
                fallo.addSuppressed(rollback);
                try {
                    c.close();
                } catch (SQLException cierre) {
                    fallo.addSuppressed(cierre);
                }
            }
            throw fallo;
        } finally {
            if (propia && restaurarModo) {
                try {
                    c.setAutoCommit(true);
                } catch (SQLException restauracion) {
                    if (fallo != null) fallo.addSuppressed(restauracion);
                    else throw new AccesoDatosException("No se pudo restaurar la conexion", restauracion);
                }
            }
        }
    }

    // Reglas basicas previas a tocar la base; el modelo valida los atributos de negocio.
    protected static void idValido(int id) {
        if (id <= 0) throw new IllegalArgumentException("Se requiere un id persistido");
    }

    // Evita insertar dos veces la misma entidad como si fuera nueva.
    protected static void nuevo(int id) {
        if (id != 0) throw new IllegalArgumentException("El registro ya tiene id");
    }

    // Toda actualizacion por id debe afectar al registro solicitado.
    protected static void exigirCambio(int filas) {
        if (filas != 1) throw new AccesoDatosException("El registro solicitado no existe o no pudo actualizarse");
    }

    // Texto obligatorio usado por filtros y configuracion.
    protected static String texto(String valor) {
        if (valor == null || valor.trim().isEmpty()) throw new IllegalArgumentException("Texto obligatorio");
        return valor.trim();
    }
}
