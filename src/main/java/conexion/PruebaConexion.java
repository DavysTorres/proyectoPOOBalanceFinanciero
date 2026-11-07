package conexion;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Comprobacion JDBC sencilla: el SQL de negocio permanece exclusivamente en dao.
 */
public class PruebaConexion {
    public static void main(String[] args) {
        conexionDB administrador = conexionDB.getInstance();
        try {
            Connection conexion = administrador.getConnection();
            if (conexion.isValid(5)) System.out.println("La base de datos responde correctamente.");
            else throw new SQLException("La conexion no responde");
        } catch (SQLException e) {
            System.err.println("No se pudo verificar la conexion a MySQL.");
        } finally {
            administrador.desconectar();
        }
    }
}
