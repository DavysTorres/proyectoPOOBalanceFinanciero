package excepciones;

/**
 * No se admiten mas movimientos ni ajustes en una fecha cerrada.
 */
public class DiaCerradoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    // El controlador puede presentar este mensaje sin conocer SQL ni detalles tecnicos.
    public DiaCerradoException(String mensaje) {
        super(mensaje);
    }
}
