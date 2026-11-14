package excepciones;

/**
 * El usuario, la clave o el estado de la cuenta no permiten iniciar sesion.
 */
public class CredencialesInvalidasException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    // El controlador puede presentar este mensaje sin conocer SQL ni detalles tecnicos.
    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
