package Excepciones;

public class UsuarioBloqueadoException extends Exception {
    // Constructor por defecto
    public UsuarioBloqueadoException() {
        super("El usuario ha sido bloqueado por muchos intentos fallidos.");
    }

    // Constructor para mensaje personalizado
    public UsuarioBloqueadoException(String mensaje) {
        super(mensaje);
    }
}
