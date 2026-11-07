package servicio;

import dao.UsuarioDAO;
import modelo.*;
import excepciones.DatosInvalidosException;

import java.time.*;
import java.util.*;

/**
 * Administra usuarios con Factory y baja logica; nunca recibe hashes escritos por la vista.
 */
public class ServicioUsuarios extends ServicioBase {
    private final UsuarioDAO usuarios;
    private final ServicioAutenticacion autenticacion;

    // Comparte autenticacion para aplicar un unico formato de hash.
    public ServicioUsuarios() {
        this(new UsuarioDAO(), new ServicioAutenticacion(), Clock.systemDefaultZone());
    }

    public ServicioUsuarios(UsuarioDAO usuarios, ServicioAutenticacion autenticacion, Clock reloj) {
        super(reloj);
        this.usuarios = Objects.requireNonNull(usuarios);
        this.autenticacion = Objects.requireNonNull(autenticacion);
    }

    // Resuelve el primer inicio sin contrasenas predeterminadas ni saltarse permisos despues.
    public Usuario crearAdministradorInicial(String nombre, String username, String clave) {
        return usuarios.transaccion(() -> {
            if (!usuarios.listarTodos().isEmpty())
                throw new DatosInvalidosException("La inicializacion ya fue realizada");
            Usuario u = fabricar(0, nombre, username, autenticacion.cifrarClave(clave), Rol.ADMINISTRADOR, true, LocalDateTime.now(reloj));
            usuarios.guardar(u);
            return u;
        });
    }

    // El administrador crea la subclase apropiada y el servicio genera el hash.
    public Usuario crearUsuario(String nombre, String username, String clave, Rol rol) {
        sesion.exigirPermiso(Permiso.GESTIONAR_USUARIOS);
        exigirUsernameLibre(username, 0);
        Usuario u = fabricar(0, nombre, username, autenticacion.cifrarClave(clave), rol, true, LocalDateTime.now(reloj));
        usuarios.guardar(u);
        return u;
    }

    // Cambiar un rol crea otra subclase coherente, en vez de alterar el rol del objeto anterior.
    public Usuario editarUsuario(int id, String nombre, String username, Rol rol, boolean activo) {
        sesion.exigirPermiso(Permiso.GESTIONAR_USUARIOS);
        Usuario anterior = exigirUsuario(id);
        exigirUsernameLibre(username, id);
        protegerAdministrador(anterior, rol, activo);
        Usuario nuevo = fabricar(id, nombre, username, anterior.getPasswordHash(), rol, activo, anterior.getCreadoEn());
        usuarios.actualizar(nuevo);
        if (usuario().getId() == id) {
            if (activo) sesion.iniciar(nuevo);
            else sesion.cerrar();
        }
        return nuevo;
    }

    // Conserva al menos un administrador activo para no bloquear la administracion del proyecto.
    public void desactivarUsuario(int id) {
        sesion.exigirPermiso(Permiso.GESTIONAR_USUARIOS);
        Usuario u = exigirUsuario(id);
        protegerAdministrador(u, u.getRol(), false);
        usuarios.cambiarEstado(id, false);
        if (usuario().getId() == id) sesion.cerrar();
    }

    // Consultar usuarios tambien requiere capacidad administrativa.
    public List<Usuario> listarUsuarios() {
        sesion.exigirPermiso(Permiso.GESTIONAR_USUARIOS);
        return usuarios.listarTodos();
    }

    // La restriccion UNIQUE del esquema sigue siendo la proteccion definitiva ante escrituras simultaneas.
    private void exigirUsernameLibre(String username, int propioId) {
        Usuario existente = usuarios.buscarPorUsername(texto(username, "El usuario"));
        if (existente != null && existente.getId() != propioId)
            throw new DatosInvalidosException("El username ya existe");
    }

    private Usuario exigirUsuario(int id) {
        Usuario u = usuarios.buscarPorId(id);
        if (u == null) throw new DatosInvalidosException("Usuario inexistente");
        return u;
    }

    // Se comprueba el catalogo persistido, no solamente el usuario en memoria de la sesion.
    private void protegerAdministrador(Usuario anterior, Rol nuevoRol, boolean activo) {
        if (anterior.getRol() != Rol.ADMINISTRADOR || (nuevoRol == Rol.ADMINISTRADOR && activo)) return;
        for (Usuario u : usuarios.listarActivos())
            if (u.getId() != anterior.getId() && u.getRol() == Rol.ADMINISTRADOR) return;
        throw new DatosInvalidosException("Debe conservar al menos un administrador activo");
    }

    // Factory explicita para demostrar la relacion de herencia del modelo.
    private Usuario fabricar(int id, String nombre, String username, String hash, Rol rol, boolean activo, LocalDateTime fecha) {
        Objects.requireNonNull(rol);
        switch (rol) {
            case ADMINISTRADOR:
                return new Administrador(id, nombre, username, hash, activo, fecha);
            case JEFE:
                return new Jefe(id, nombre, username, hash, activo, fecha);
            case CAJERO:
                return new Cajero(id, nombre, username, hash, activo, fecha);
            default:
                throw new DatosInvalidosException("Rol desconocido");
        }
    }
}
