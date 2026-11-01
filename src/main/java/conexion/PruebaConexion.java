package conexion;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PruebaConexion {

    public static void main(String[] args) {
        conexionDB db = conexionDB.getInstance();

        try {
            Connection conexion = db.getConnection();

            try (Statement consulta = conexion.createStatement();
                 ResultSet resultado = consulta.executeQuery("SELECT 1")) {

                if (resultado.next() && resultado.getInt(1) == 1) {
                    System.out.println("Prueba correcta: la base de datos responde.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Falló la prueba: " + e.getMessage());
        } finally {
            db.desconectar();
        }
    }
}