package controlador;

import vista.contratos.*;
import servicio.*;
import modelo.*;

import java.util.*;

/**
 * Administra perfiles sin generar hashes ni modificar permisos dentro del controlador.
 */
public final class UsuarioControlador extends ControladorBase<VistaUsuarios> {
    private final ServicioUsuarios usuarios;
    private final ServicioAutenticacion autenticacion;

    // Los listeners no contienen acceso directo a la base de datos.
    public UsuarioControlador(VistaUsuarios vista, ServicioUsuarios usuarios, ServicioAutenticacion autenticacion, EjecutorTareas tareas) {
        super(vista, tareas);
        this.usuarios = Objects.requireNonNull(usuarios);
        this.autenticacion = Objects.requireNonNull(autenticacion);
        vista.alAccionar(AccionVista.CREAR, this::crear);
        vista.alAccionar(AccionVista.EDITAR, this::editar);
        vista.alAccionar(AccionVista.DESACTIVAR, this::desactivar);
        vista.alAccionar(AccionVista.LISTAR, this::listar);
        vista.alAccionar(AccionVista.RESTABLECER_CLAVE, this::restablecerClave);
    }

    // Las claves no se guardan en atributos del controlador ni se muestran en mensajes.
    private String leerClave() {
        char[] caracteres = vista.getClave();
        try {
            return caracteres == null ? "" : new String(caracteres);
        } finally {
            if (caracteres != null) Arrays.fill(caracteres, '\0');
            vista.limpiarClave();
        }
    }

    public void crear() {
        preparar(() -> {
            String nombre = vista.getNombre(), username = vista.getUsername();
            Rol rol = seleccionado(vista.getRol());
            String clave = leerClave();
            ejecutar(() -> usuarios.crearUsuario(nombre, username, clave, rol), u -> {
                vista.mostrarInformacion("Usuario creado con numero " + u.getId());
                informarCambio();
            });
        });
    }

    public void editar() {
        preparar(() -> {
            int id = id(vista.getIdSeleccionado());
            String nombre = vista.getNombre(), username = vista.getUsername();
            Rol rol = seleccionado(vista.getRol());
            boolean activo = vista.isActivo();
            ejecutar(() -> usuarios.editarUsuario(id, nombre, username, rol, activo), u -> {
                vista.mostrarInformacion("Usuario actualizado.");
                informarCambio();
            });
        });
    }

    public void desactivar() {
        preparar(() -> {
            int id = id(vista.getIdSeleccionado());
            ejecutar(() -> {
                usuarios.desactivarUsuario(id);
                return null;
            }, ignorado -> {
                vista.mostrarInformacion("Usuario desactivado.");
                informarCambio();
            });
        });
    }

    public void listar() {
        ejecutar(usuarios::listarUsuarios, vista::mostrarUsuarios);
    }

    public void restablecerClave() {
        preparar(() -> {
            int id = id(vista.getIdSeleccionado());
            String clave = leerClave();
            ejecutar(() -> {
                autenticacion.restablecerClave(id, clave);
                return null;
            }, ignorado -> {
                vista.mostrarInformacion("Clave restablecida.");
                informarCambio();
            });
        });
    }
}
