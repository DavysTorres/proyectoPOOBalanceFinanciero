package modelo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class Cajero extends Usuario {
    //Constructor
    public Cajero(int id, String nombre, String username, String passwordHash, boolean activo, LocalDateTime creadoEn) {
        super(id, nombre, username, passwordHash, Rol.CAJERO, activo, creadoEn);
    }
 
    @Override
    public List<Permiso> obtenerPermisos() {
        //Aquí llamo unicamente el permiso REGISTRA_VENTA
        return Arrays.asList(Permiso.REGISTRAR_VENTA);
    }

    @Override
    public List<String> mostrarMenu() {
        return Arrays.asList(
            "Registrar venta", 
            "Ver semaforo del día"
        );
    }    
}
