package excepciones;

/**
 * Los datos no cumplen una regla de negocio de la capa de servicios.
 */
public class DatosInvalidosException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    // El controlador puede presentar este mensaje sin conocer SQL ni detalles tecnicos.
    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
