package Excepciones;

public class PermisoDenegadoException extends Exception {
    // Constructor por defecto 
    public PermisoDenegadoException() {
        super("El rol del usuario no tiene el permiso necesario para esta acción.");
    }

    // Constructor para mensaje personalizado
    public PermisoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
