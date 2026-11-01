package modelo;

public final class Rol {
    public static final Rol ADMINISTRADOR = new Rol(1, "ADMINISTRADOR");
    public static final Rol JEFE = new Rol(2, "JEFE");
    public static final Rol CAJERO = new Rol(3, "CAJERO");

    private final int id;
    private final String nombre;

    private Rol(int id, String nombre) {
        if (id <= 0) {
            throw new IllegalArgumentException("El id del rol debe ser mayor que cero");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del rol es obligatorio");
        }
        this.id = id;
        this.nombre = nombre.trim();
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    // Método para buscar desde MySQL
    public static Rol fromString(String rol){
        if (rol == null || rol.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del rol es obligatorio");
        }
        String nombre = rol.trim();
        if(nombre.equalsIgnoreCase("ADMINISTRADOR")){
            return ADMINISTRADOR;
        }
        else if(nombre.equalsIgnoreCase("JEFE")){
            return JEFE;
        }
        else if(nombre.equalsIgnoreCase("CAJERO")){
            return CAJERO;
        }
        throw new IllegalArgumentException("Rol no valido");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Rol rol = (Rol) obj;
        return id == rol.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
