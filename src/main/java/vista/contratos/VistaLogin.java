package vista.contratos;

/**
 * Datos y resultados que necesita VistaLogin; los importes y fechas de entrada se entregan como texto.
 */
public interface VistaLogin extends VistaBase {
    String getUsername();

    char[] getContrasena();

    void limpiarContrasena();
}
