package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    // Atributos definidos en el diagrama
    private static ConexionBD instancia;
    private String url = "jdbc:mysql://localhost:3306/zeroloss_db"; // Cambia el nombre de tu BD
    private String usuario = "root"; // Tu usuario de MySQL
    private String clave = "tu_contraseña"; // Tu contraseña de MySQL
    
    private Connection conexion;

    // Constructor privado (Regla principal del patrón Singleton)
    private ConexionBD() {
        try {
            this.conexion = DriverManager.getConnection(url, usuario, clave);
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
        }
    }

    // Métodos solicitados
    public static ConexionBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    public Connection getConexion() {
        return conexion;
    }

    public void iniciarTransaccion() throws SQLException {
        if (conexion != null) {
            conexion.setAutoCommit(false);
        }
    }

    public void confirmar() throws SQLException {
        if (conexion != null) {
            conexion.commit();
            conexion.setAutoCommit(true);
        }
    }

    public void revertir() {
        try {
            if (conexion != null) {
                conexion.rollback();
                conexion.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Error al revertir la transacción: " + e.getMessage());
        }
    }

    public void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
    }    
}
