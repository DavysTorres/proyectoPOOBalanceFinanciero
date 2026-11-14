package servicio;

import excepciones.PermisoDenegadoException;
import modelo.Permiso;
import modelo.Usuario;

import java.util.Objects;

/**
 * Singleton de la sesion de escritorio; no abre conexiones ni contiene ventanas.
 */
public final class Sesion {
    private static final Sesion INSTANCIA = new Sesion();
    private Usuario usuarioActual;

    // Se impide crear sesiones globales distintas por accidente.
    private Sesion() {
    }

    public static Sesion getInstancia() {
        return INSTANCIA;
    }

    // Autenticacion entrega un usuario previamente verificado y activo.
    public void iniciar(Usuario usuario) {
        Objects.requireNonNull(usuario);
        if (!usuario.isActivo()) throw new PermisoDenegadoException("El usuario esta desactivado");
        usuarioActual = usuario;
    }

    // Cerrar no modifica la base de datos ni el estado permanente del usuario.
    public void cerrar() {
        usuarioActual = null;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public boolean hayUsuario() {
        return usuarioActual != null && usuarioActual.isActivo();
    }

    // Punto unico de comprobacion de permisos para los servicios.
    public void exigirPermiso(Permiso permiso) {
        if (!hayUsuario() || !usuarioActual.tienePermiso(permiso))
            throw new PermisoDenegadoException("No tiene permiso para esta accion");
    }
}
