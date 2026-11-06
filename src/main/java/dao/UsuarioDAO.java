package dao;

import modelo.Administrador;
import modelo.Cajero;
import modelo.Jefe;
import modelo.Usuario;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

public class UsuarioDAO implements CrudDAO<Usuario>{
    private Connection conexion;

    public UsuarioDAO() {
        // Obtenemos la conexión única usando el Singleton
        this.conexion = ConexionBD.getInstancia().getConexion();
    }

    //Heredamos los metodos del CrudDAO
    @Override
    public void guardar(Usuario entidad) {
        // Lógica SQL INSERT...
    }

    @Override
    public void actualizar(Usuario entidad) {
        // Lógica SQL UPDATE...
    }

    @Override
    public Usuario buscarPorId(int id) {
        // Lógica SQL SELECT WHERE id = ?
        return null;
    }

    @Override
    public List<Usuario> listarTodos() {
        // Lógica SQL SELECT *
        return null;
    }

    //Metodos unicos de UsuarioDAO
    public Usuario buscarPorUsername(String username) {
        // Lógica SQL SELECT WHERE username = ?
        return null;
    }

    public List<Usuario> listarActivos() {
        // Lógica SQL SELECT WHERE activo = true
        return null;
    }

    public void cambiarEstado(int id, boolean activo) {
        // Lógica SQL UPDATE usuario SET activo = ? WHERE id = ?
    }

    public void actualizarPassword(int id, String hash) {
        // Lógica SQL UPDATE usuario SET passwordHash = ? WHERE id = ?
    }

    // Este método lo usarás internamente cuando leas un registro de la BD
    private Usuario construirUsuarioFactory(int id, String nombre, String username, String hash, String rolStr, boolean activo, LocalDateTime fecha) {
        switch (rolStr.toUpperCase()) {
            case "ADMINISTRADOR":
                return new Administrador(id, nombre, username, hash, activo, fecha);
            case "CAJERO":
                return new Cajero(id, nombre, username, hash, activo, fecha);
            case "JEFE":
                return new Jefe(id, nombre, username, hash, activo, fecha);
            default:
                throw new IllegalArgumentException("Rol desconocido en la base de datos: " + rolStr);
        }
    }    
}
