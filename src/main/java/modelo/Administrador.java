package modelo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class Administrador extends Usuario {
    //Constructor 
    public Administrador(int id, String nombre, String username, String passwordHash, boolean activo, LocalDateTime creadoEn) {
        super(id, nombre, username, passwordHash, Rol.ADMINISTRADOR, activo, creadoEn);
    }
    
    @Override
    public List<Permiso> obtenerPermisos() {
        // Devuelve todos los valores del Enum Permiso
        return Arrays.asList(Permiso.values());
    }

    @Override
    public List<String> mostrarMenu() {
        // Imprime las opciones del perfil administrador
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
