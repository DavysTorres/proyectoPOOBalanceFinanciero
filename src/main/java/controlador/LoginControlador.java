package controlador;

import vista.contratos.*;
import servicio.ServicioAutenticacion;
import modelo.Usuario;

import java.util.*;

/**
 * Conecta el boton ingresar con autenticacion y delega la apertura del menu a la navegacion.
 */
public final class LoginControlador extends ControladorBase<VistaLogin> {
    private final ServicioAutenticacion autenticacion;
    private final Navegacion navegacion;

    // El mismo servicio debe conservarse entre intentos para no reiniciar el bloqueo.
    public LoginControlador(VistaLogin vista, ServicioAutenticacion autenticacion, Navegacion navegacion, EjecutorTareas tareas) {
        super(vista, tareas);
        this.autenticacion = Objects.requireNonNull(autenticacion);
        this.navegacion = Objects.requireNonNull(navegacion);
        vista.alAccionar(AccionVista.INGRESAR, this::iniciarSesion);
    }

    // Solo lee campos y limpia la copia de caracteres; verificar credenciales corresponde al servicio.
    public void iniciarSesion() {
        preparar(() -> {
            String username = vista.getUsername();
            char[] caracteres = vista.getContrasena();
            String clave;
            try {
                clave = caracteres == null ? "" : new String(caracteres);
            } finally {
                if (caracteres != null) Arrays.fill(caracteres, '\0');
                vista.limpiarContrasena();
            }
            if (username == null || username.isBlank() || clave.isEmpty())
                throw new IllegalArgumentException("Ingrese usuario y contrasena.");
            ejecutar(() -> autenticacion.iniciarSesion(username, clave), this::abrirMenuSegunRol);
        });
    }

    // La navegacion utiliza el rol y las opciones del usuario que devolvio autenticacion.
    private void abrirMenuSegunRol(Usuario usuario) {
        navegacion.abrirMenu(usuario);
    }
}
