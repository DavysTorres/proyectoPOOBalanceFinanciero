package excepciones;

/**
 * Se alcanzo el limite temporal de intentos de autenticacion.
 */
public class UsuarioBloqueadoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    // El controlador puede presentar este mensaje sin conocer SQL ni detalles tecnicos.
    public UsuarioBloqueadoException(String mensaje) {
        super(mensaje);
    }
}
