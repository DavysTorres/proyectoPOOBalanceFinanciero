package dao;

import modelo.*;

import java.sql.*;
import java.util.List;

/**
 * Persistencia de usuarios con baja logica y reconstruccion de la subclase adecuada.
 */
public class UsuarioDAO extends DAOBase<Usuario> implements CrudDAO<Usuario> {
    // Ambos constructores permiten usar la conexion de la aplicacion o una conexion de pruebas.
    public UsuarioDAO() {
        super();
    }

    public UsuarioDAO(Connection conexion) {
        super(conexion);
    }

    // Factory: la columna rol determina el tipo real del objeto reconstruido.
    @Override
    protected Usuario mapear(ResultSet r) throws SQLException {
        int id = r.getInt("id");
        String nombre = r.getString("nombre"), usuario = r.getString("username");
        String hash = r.getString("password_hash");
        boolean activo = r.getBoolean("activo");
        java.time.LocalDateTime fecha = r.getTimestamp("creado_en").toLocalDateTime();
        switch (Rol.fromString(r.getString("rol"))) {
            case ADMINISTRADOR:
                return new Administrador(id, nombre, usuario, hash, activo, fecha);
            case JEFE:
                return new Jefe(id, nombre, usuario, hash, activo, fecha);
            case CAJERO:
                return new Cajero(id, nombre, usuario, hash, activo, fecha);
            default:
                throw new SQLException("Rol desconocido");
        }
    }

    // Guarda un hash ya calculado por autenticacion; nunca almacena aqui una clave en texto plano.
    @Override
    public void guardar(Usuario u) {
        nuevo(u.getId());
        int id = insertar("INSERT INTO usuarios(nombre,username,password_hash,rol,activo,creado_en) VALUES (?,?,?,?,?,?)",
                u.getNombre(), u.getUsername(), u.getPasswordHash(), u.getRol().name(), u.isActivo(), u.getCreadoEn());
        u.setId(id);
    }

    // Actualiza datos sin eliminar relaciones con ventas o historial.
    @Override
    public void actualizar(Usuario u) {
        idValido(u.getId());
        exigirCambio(ejecutar("UPDATE usuarios SET nombre=?,username=?,password_hash=?,rol=?,activo=? WHERE id=?",
                u.getNombre(), u.getUsername(), u.getPasswordHash(), u.getRol().name(), u.isActivo(), u.getId()));
    }

    // Busquedas individuales, por nombre de acceso, y listados de administracion.
    @Override
    public Usuario buscarPorId(int id) {
        idValido(id);
        return uno("SELECT * FROM usuarios WHERE id=?", id);
    }

    public Usuario buscarPorUsername(String username) {
        return uno("SELECT * FROM usuarios WHERE username=?", texto(username));
    }

    @Override
    public List<Usuario> listarTodos() {
        return consultar("SELECT * FROM usuarios ORDER BY id");
    }

    public List<Usuario> listarActivos() {
        return consultar("SELECT * FROM usuarios WHERE activo=TRUE ORDER BY nombre,id");
    }

    // Baja logica: la entidad continua disponible para sus registros historicos.
    public void cambiarEstado(int id, boolean activo) {
        idValido(id);
        exigirCambio(ejecutar("UPDATE usuarios SET activo=? WHERE id=?", activo, id));
    }

    // El servicio genera el hash y autoriza quien puede restablecer la clave.
    public void actualizarPassword(int id, String hash) {
        idValido(id);
        exigirCambio(ejecutar("UPDATE usuarios SET password_hash=? WHERE id=?", texto(hash), id));
    }
}
