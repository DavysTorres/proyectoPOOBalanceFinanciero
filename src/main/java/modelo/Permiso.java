package modelo;

/** Catalogo unico de acciones compartido por usuarios y servicios. */
public enum Permiso {
    // Los permisos representan capacidades; no ejecutan operaciones.
    REGISTRAR_VENTA(1, "Registrar Venta"),
    REGISTRAR_COMPRA(2, "Registrar Compra"),
    REGISTRAR_GASTO(3, "Registrar Gasto"),
    MODIFICAR_VENTA(4, "Modificar Venta"),
    VER_BALANCE(5, "Ver Balance"),
    VER_REPORTES(6, "Ver Reportes"),
    VER_HISTORIAL(7, "Ver Historial"),
    CERRAR_CAJA(8, "Cerrar Caja"),
    GESTIONAR_USUARIOS(9, "Gestionar Usuarios"),
    GESTIONAR_PRODUCTOS(10, "Gestionar Productos"),
    DEFINIR_COSTOS_FIJOS(11, "Definir Costos Fijos"),
    CONFIGURAR(12, "Configurar");

    // Datos inmutables de cada constante.
    private final int id;
    private final String descripcion;

    // Inicializa una constante del enum.
    Permiso(int id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    // Consultas para persistencia y presentacion.
    public int getId() { return id; }
    public String getNombre() { return name(); }
    public String getDescripcion() { return descripcion; }

    // Reconoce un nombre tecnico o su descripcion sin importar mayusculas.
    public static Permiso fromString(String texto) {
        String nombre = ValidacionModelo.texto(texto, "El permiso");
        for (Permiso permiso : values()) {
            if (permiso.name().equalsIgnoreCase(nombre)
                    || permiso.descripcion.equalsIgnoreCase(nombre)) return permiso;
        }
        throw new IllegalArgumentException("Permiso no valido: " + nombre);
    }

    // Representacion legible para las vistas.
    @Override
    public String toString() { return descripcion; }
}
