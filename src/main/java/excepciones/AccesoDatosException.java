package excepciones;

/**
 * Envuelve fallos JDBC para que las capas superiores no dependan de SQLException.
 */
public class AccesoDatosException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    // Conserva la causa tecnica para diagnostico sin exponer SQL en las ventanas.
    public AccesoDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    // Describe inconsistencias de persistencia sin inventar resultados exitosos.
    public AccesoDatosException(String mensaje) {
        super(mensaje);
    }
}
