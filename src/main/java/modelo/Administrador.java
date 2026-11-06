package modelo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/** Entidad del modelo: encapsula sus datos sin depender de vistas, servicios ni DAO. */
public class Administrador extends Usuario{

    //Constructor 
    // Construye el objeto e inicializa sus atributos con las validaciones disponibles.
    public Administrador(int id, String nombre, String username, String passwordHash, boolean activo, LocalDateTime creadoEn) {
        super(id, nombre, username, passwordHash, Rol.ADMINISTRADOR, activo, creadoEn);
    }
    
    @Override
    // Implementa por polimorfismo las capacidades del perfil indicado en la guia.
    public List<Permiso> obtenerPermisos() {
        // Devuelve todos los valores del Enum Permiso
        return Arrays.asList(Permiso.values());
    }

    @Override
    // Devuelve los textos exigidos por la guia; la vista construye las ventanas.
    public List<String> mostrarMenu() {
        // Proporciona las opciones del perfil administrador sin abrir ventanas.
        return Arrays.asList(
            "Usuarios", 
            "Productos", 
            "Costos fijos", 
            "Configuración", 
            "Historial de cambios", 
            "Reportes"
        );
    }
    
    
}
