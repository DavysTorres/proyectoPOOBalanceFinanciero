package servicio;

import dao.UsuarioDAO;
import modelo.*;

import java.util.Objects;

/**
 * Primer uso sin usuarios de fabrica: guarda administrador y configuracion en una transaccion.
 */
public final class ServicioInicializacion {
    private final UsuarioDAO usuarios;
    private final ServicioUsuarios administracion;
    private final ServicioConfiguracion configuracion;

    public ServicioInicializacion(UsuarioDAO usuarios, ServicioUsuarios administracion, ServicioConfiguracion configuracion) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.administracion = Objects.requireNonNull(administracion);
        this.configuracion = Objects.requireNonNull(configuracion);
    }

    public boolean necesitaConfiguracionInicial() {
        return usuarios.listarTodos().isEmpty();
    }

    // Si falla cualquiera de los dos registros, se revierte todo y no queda una sesion abierta.
    public Usuario inicializar(String nombre, String username, String clave, Negocio negocio) {
        try {
            return usuarios.transaccion(() -> {
                Usuario administrador = administracion.crearAdministradorInicial(nombre, username, clave);
                Sesion.getInstancia().iniciar(administrador);
                configuracion.guardarNegocio(negocio);
                return administrador;
            });
        } catch (RuntimeException e) {
            Sesion.getInstancia().cerrar();
            throw e;
        }
    }
}
