package excepciones;

/**
 * La sesion no tiene la capacidad requerida para ejecutar una accion.
 */
public class PermisoDenegadoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    // El controlador puede presentar este mensaje sin conocer SQL ni detalles tecnicos.
    public PermisoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
