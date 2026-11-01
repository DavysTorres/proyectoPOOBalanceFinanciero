package modelo;

import java.util.ArrayList;
import java.util.List;

public final class Permiso {

    // 1. Definimos las constantes como instancias estáticas de la propia clase
    public static final Permiso REGISTRAR_VENTA = new Permiso(1, "REGISTRAR_VENTA", "Registrar Venta");
    public static final Permiso REGISTRAR_COMPRA = new Permiso(2, "REGISTRAR_COMPRA", "Registrar Compra");
    public static final Permiso REGISTRAR_GASTO = new Permiso(3, "REGISTRAR_GASTO", "Registrar Gasto");
    public static final Permiso MODIFICAR_VENTA = new Permiso(4, "MODIFICAR_VENTA", "Modificar Venta");
    public static final Permiso VER_BALANCE = new Permiso(5, "VER_BALANCE", "Ver Balance");
    public static final Permiso VER_REPORTES = new Permiso(6, "VER_REPORTES", "Ver Reportes");
    public static final Permiso VER_HISTORIAL = new Permiso(7, "VER_HISTORIAL", "Ver Historial");
    public static final Permiso CERRAR_CAJA = new Permiso(8, "CERRAR_CAJA", "Cerrar Caja");
    public static final Permiso GESTIONAR_USUARIOS = new Permiso(9, "GESTIONAR_USUARIOS", "Gestionar Usuarios");
    public static final Permiso GESTIONAR_PRODUCTOS = new Permiso(10, "GESTIONAR_PRODUCTOS", "Gestionar Productos");
    public static final Permiso DEFINIR_COSTOS_FIJOS = new Permiso(11, "DEFINIR_COSTOS_FIJOS", "Definir Costos Fijos");
    public static final Permiso CONFIGURAR = new Permiso(12, "CONFIGURAR", "Configurar");

    // Lista estática para facilitar la búsqueda en el método fromString
    private static final List<Permiso> TODOS_LOS_PERMISOS  = new ArrayList<>();

    // Bloque que llena la lista
    static {
        TODOS_LOS_PERMISOS.add(REGISTRAR_VENTA);
        TODOS_LOS_PERMISOS.add(REGISTRAR_COMPRA);
        TODOS_LOS_PERMISOS.add(REGISTRAR_GASTO);
        TODOS_LOS_PERMISOS.add(MODIFICAR_VENTA);
        TODOS_LOS_PERMISOS.add(VER_BALANCE);
        TODOS_LOS_PERMISOS.add(VER_REPORTES);
        TODOS_LOS_PERMISOS.add(VER_HISTORIAL);
        TODOS_LOS_PERMISOS.add(CERRAR_CAJA);
        TODOS_LOS_PERMISOS.add(GESTIONAR_USUARIOS);
        TODOS_LOS_PERMISOS.add(GESTIONAR_PRODUCTOS);
        TODOS_LOS_PERMISOS.add(DEFINIR_COSTOS_FIJOS);
        TODOS_LOS_PERMISOS.add(CONFIGURAR);
    }

    // Atributos de la clase
    private final int id;
    private final String nombre;
    private final String descripcion;

    // 2. Constructor
    private Permiso(int id, String nombre, String descripcion) {
        if (id <= 0) {
            throw new IllegalArgumentException("El id del permiso debe ser mayor que cero");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del permiso es obligatorio");
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripcion del permiso es obligatoria");
        }
        this.id = id;
        this.nombre = nombre.trim();
        this.descripcion = descripcion.trim();
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    // 3. metodo para recuperar desde mysql
    public static Permiso fromString(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("El texto del permiso es obligatorio");
        }
        String valor = texto.trim();

        for (Permiso permiso : TODOS_LOS_PERMISOS) {
            // Compara ya sea que venga el nombre técnico o la descripción
            if (permiso.getNombre().equalsIgnoreCase(valor) || permiso.getDescripcion().equalsIgnoreCase(valor)) {
                return permiso;
            }
        }
        throw new IllegalArgumentException("El texto no es valido");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Permiso permiso = (Permiso) obj;
        return id == permiso.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return this.descripcion;
    }

}
