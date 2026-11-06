package Excepciones;

public class CredencialesInvalidasException extends Exception{
    // Constructor por defecto
    public CredencialesInvalidasException() {
        super("Usuario o contraseña incorrectos.");
    }

    // Constructor para mensaje personalizado
    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
