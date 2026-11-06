package modelo;
import java.time.LocalDateTime;
import java.util.List;
public abstract class Usuario { //Clase Principal (Padre)
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
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public final void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        this.nombre = nombre;
    }

    public String getUsername() {
        return username;
    }

    public final void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario no puede estar vacío");
        }
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

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
    
    
}
