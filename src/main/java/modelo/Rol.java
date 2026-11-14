package modelo;

/**
 * Enum compartido: limita el rol a los tres perfiles autorizados por la guia.
 */
public enum Rol {
    ADMINISTRADOR(1), JEFE(2), CAJERO(3);

    // Cada constante conserva un identificador inmutable para persistencia.
    private final int id;

    Rol(int id) {
        this.id = id;
    }

    // Consultas del identificador y del nombre tecnico.
    public int getId() {
        return id;
    }

    public String getNombre() {
        return name();
    }

    // Convierte texto sin depender de MySQL ni de otra capa.
    public static Rol fromString(String texto) {
        String nombre = ValidacionModelo.texto(texto, "El rol");
        for (Rol rol : values()) {
            if (rol.name().equalsIgnoreCase(nombre)) return rol;
        }
        throw new IllegalArgumentException("Rol no valido: " + nombre);
    }
}
