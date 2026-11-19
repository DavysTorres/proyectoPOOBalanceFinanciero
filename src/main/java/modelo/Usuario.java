package modelo;

import java.time.LocalDateTime;
import java.util.List;

<<<<<<< HEAD
public abstract class Usuario { //Ckase principal (Padre)
     //Enum de Rol y Permiso
    public enum Rol { //Aquí creo la numeración de los ROLES
    ADMINISTRADOR, CAJERO, JEFE
    }
    
    public enum Permiso {
    REGISTRAR_VENTA, REGISTRAR_COMPRA, REGISTRAR_GASTO, MODIFICAR_VENTA, VER_BALANCE,
    VER_REPORTES, VER_HISTORIAL, CERRAR_CAJA, GESTIONAR_USUARIOS, GESTIONAR_PRODUCTOS,
    DEFINIR_COSTOS_FIJOS, CONFIGURAR
    }

    
    //Atributos 
    private int id; //Guarda la llave primaria generada por MySQL
    private String nombre; //Guarda los nombres
    private String username; //Guarda los usuarios
    private String passwordHash; //Guarda las contraseñas
    private Rol rol; //Guarda el rol
    private boolean activo; //Guarda el estado del usuario 
    private LocalDateTime creadoEn; // Guarda laecha de registro del usuario
    
    // Constructor completo: permite crear un usuario con todos sus datos.
    public Usuario(int id, String nombre, String username, String passwordHash, Rol rol, boolean activo, LocalDateTime creadoEn) {
        this.id = id;
        setNombre(nombre);
        setUsername(username);
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = activo;
        this.creadoEn = creadoEn;
    }

    // Getter y Setter 
=======
/**
 * Clase abstracta que concentra el estado comun y delega permisos a cada perfil.
 */
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
>>>>>>> c6b38029e9d5f9f3c350cb56d301388f5b5859f1
    public int getId() {
        return id;
    }

<<<<<<< HEAD
    public void setId(int id) {
        this.id = id;
    }

=======
    public final void setId(int id) {
        this.id = ValidacionModelo.id(id);
    }

    // El nombre es obligatorio y se conserva sin espacios exteriores.
>>>>>>> c6b38029e9d5f9f3c350cb56d301388f5b5859f1
    public String getNombre() {
        return nombre;
    }

    public final void setNombre(String nombre) {
<<<<<<< HEAD
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        this.nombre = nombre;
    }

=======
        this.nombre = ValidacionModelo.texto(nombre, "El nombre");
    }

    // La unicidad del username corresponde al servicio; aqui se valida su contenido.
>>>>>>> c6b38029e9d5f9f3c350cb56d301388f5b5859f1
    public String getUsername() {
        return username;
    }

    public final void setUsername(String username) {
<<<<<<< HEAD
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario no puede estar vacío");
        }
        this.username = username;
    }

=======
        this.username = ValidacionModelo.texto(username, "El usuario");
    }

    // Se recibe un hash generado en autenticacion; el modelo no cifra ni imprime claves.
>>>>>>> c6b38029e9d5f9f3c350cb56d301388f5b5859f1
    public String getPasswordHash() {
        return passwordHash;
    }

<<<<<<< HEAD
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

=======
    public final void setPasswordHash(String passwordHash) {
        this.passwordHash = ValidacionModelo.texto(passwordHash, "El hash de la clave");
    }

    // El rol permanece coherente con la subclase y sus permisos.
>>>>>>> c6b38029e9d5f9f3c350cb56d301388f5b5859f1
    public Rol getRol() {
        return rol;
    }

<<<<<<< HEAD
    public void setRol(Rol rol) {
        this.rol = rol;
    }

=======
    public final void setRol(Rol rol) {
        if (this.rol != ValidacionModelo.obligatorio(rol, "El rol"))
            throw new IllegalArgumentException("El rol debe corresponder a la subclase del usuario");
    }

    // El estado permite la baja logica sin borrar el historial.
>>>>>>> c6b38029e9d5f9f3c350cb56d301388f5b5859f1
    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

<<<<<<< HEAD
=======
    // Todo usuario debe tener una fecha de registro.
>>>>>>> c6b38029e9d5f9f3c350cb56d301388f5b5859f1
    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

<<<<<<< HEAD
    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    //METODOS CONCRETOS 
    public void activar(){
        this.activo = true;
    }
    
    public void desactivar(){
        this.activo = false;
    }
    
    //METODOS ABSTRACTOS
    public abstract List<Permiso> obtenerPermisos();
    public abstract List<String> mostrarMenu();
    
    //METODOS AUXILIAR 
    public boolean tienePermiso(Permiso p){
        List<Permiso> misPermisos = obtenerPermisos();
        return misPermisos != null && misPermisos.contains(p);
    }
    
=======
    public final void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = ValidacionModelo.obligatorio(creadoEn, "La fecha de creacion");
    }

    // Operaciones de activacion y desactivacion solicitadas por la guia.
    public void activar() {
        activo = true;
    }

    public void desactivar() {
        activo = false;
    }

    // Polimorfismo: cada perfil implementa su lista de capacidades.
    public abstract List<Permiso> obtenerPermisos();

    // Devuelve textos; abrir ventanas sigue siendo responsabilidad de la vista.
    public abstract List<String> mostrarMenu();

    // Consulta una capacidad; el servicio valida aparte la sesion y el estado activo.
    public boolean tienePermiso(Permiso permiso) {
        return permiso != null && obtenerPermisos().contains(permiso);
    }
>>>>>>> c6b38029e9d5f9f3c350cb56d301388f5b5859f1
}
