package modelo;

import java.time.LocalDateTime;
import java.util.List;

/** Clase abstracta que concentra el estado comun y delega permisos a cada perfil. */
public abstract class Usuario {
    // Encapsulacion: los atributos privados se modifican por operaciones validadas.
    private int id;
    private String nombre;
    private String username;
    private String passwordHash;
    private final Rol rol;
    private boolean activo;
    private LocalDateTime creadoEn;

    // La subclase fija el rol; los datos obligatorios se validan al crear el usuario.
    protected Usuario(int id, String nombre, String username, String passwordHash,
                      Rol rol, boolean activo, LocalDateTime creadoEn) {
        setId(id);
        setNombre(nombre);
        setUsername(username);
        setPasswordHash(passwordHash);
        this.rol = ValidacionModelo.obligatorio(rol, "El rol");
        this.activo = activo;
        setCreadoEn(creadoEn);
    }

    // Consulta y asignacion del identificador; cero significa que aun no se ha guardado.
    public int getId() { return id; }
    public final void setId(int id) { this.id = ValidacionModelo.id(id); }

    // El nombre es obligatorio y se conserva sin espacios exteriores.
    public String getNombre() { return nombre; }
    public final void setNombre(String nombre) {
        this.nombre = ValidacionModelo.texto(nombre, "El nombre");
    }

    // La unicidad del username corresponde al servicio; aqui se valida su contenido.
    public String getUsername() { return username; }
    public final void setUsername(String username) {
        this.username = ValidacionModelo.texto(username, "El usuario");
    }

    // Se recibe un hash generado en autenticacion; el modelo no cifra ni imprime claves.
    public String getPasswordHash() { return passwordHash; }
    public final void setPasswordHash(String passwordHash) {
        this.passwordHash = ValidacionModelo.texto(passwordHash, "El hash de la clave");
    }

    // El rol permanece coherente con la subclase y sus permisos.
    public Rol getRol() { return rol; }
    public final void setRol(Rol rol) {
        if (this.rol != ValidacionModelo.obligatorio(rol, "El rol"))
            throw new IllegalArgumentException("El rol debe corresponder a la subclase del usuario");
    }

    // El estado permite la baja logica sin borrar el historial.
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    // Todo usuario debe tener una fecha de registro.
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public final void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = ValidacionModelo.obligatorio(creadoEn, "La fecha de creacion");
    }

    // Operaciones de activacion y desactivacion solicitadas por la guia.
    public void activar() { activo = true; }
    public void desactivar() { activo = false; }

    // Polimorfismo: cada perfil implementa su lista de capacidades.
    public abstract List<Permiso> obtenerPermisos();

    // Devuelve textos; abrir ventanas sigue siendo responsabilidad de la vista.
    public abstract List<String> mostrarMenu();

    // Consulta una capacidad; el servicio valida aparte la sesion y el estado activo.
    public boolean tienePermiso(Permiso permiso) {
        return permiso != null && obtenerPermisos().contains(permiso);
    }
}
