package conexion;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class conexionDB {

    // 1. Variable para almacenar la única instancia de la clase
    private static conexionDB instance;

    // 2. Variable para almacenar la conexión JDBC de Java
    private Connection connection;

    // Lee .env desde el directorio de trabajo; el entorno del sistema tiene prioridad.
    private static final Dotenv ENV = Dotenv.configure().ignoreIfMissing().load();

    // 3. Constructor privado para evitar que se instancie con 'new'
    private conexionDB() {
        conectar();
    }

    // 4. Método público estático para obtener la única instancia
    public static synchronized conexionDB getInstance() {
        if (instance == null) {
            instance = new conexionDB();
        }
        return instance;
    }

    private void conectar(){
        String url = configuracionObligatoria("DB_URL");
        String usuario = configuracionObligatoria("DB_USER");
        String password = configuracionObligatoria("DB_PASSWORD");
        try {  // Registrar driver si es necesario
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, usuario, password);
            System.out.println("Conexion exitosa con Mysql");
        }catch (ClassNotFoundException e){
            System.err.println("No se encontro el driver de Mysql: "+e.getMessage());
        }catch (SQLException e){
            System.err.println("Error al conectar con MySQL: " + e.getMessage());
        }
    }

    private static String configuracionObligatoria(String clave) {
        String valor = ENV.get(clave);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Configura " + clave + " en .env o en las variables de entorno");
        }
        return valor;
    }

    public Connection getConnection() throws SQLException{
        if(connection == null || connection.isClosed()){
            conectar();
        }
        if (connection == null){
            throw new SQLException("No se establecio connexion con Mysql");
        }
        return connection;
    }

    public void desconectar(){
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Conexión cerrada correctamente.");
            }
        }catch (SQLException e){
            System.err.println("Error al desconectar Mysql: "+e.getMessage());
        }
    }
}
