package modelo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


/**
 * Entidad del modelo: encapsula sus datos sin depender de vistas, servicios ni DAO.
 */
public class Cajero extends Usuario {

    //Constructor
    // Construye el objeto e inicializa sus atributos con las validaciones disponibles.
    public Cajero(int id, String nombre, String username, String passwordHash, boolean activo, LocalDateTime creadoEn) {
        super(id, nombre, username, passwordHash, Rol.CAJERO, activo, creadoEn);
    }

    @Override
    // Implementa por polimorfismo las capacidades del perfil indicado en la guia.
    public List<Permiso> obtenerPermisos() {
        //Aquí llamo unicamente el permiso REGISTRA_VENTA
        return Arrays.asList(Permiso.REGISTRAR_VENTA);
    }

    @Override
    // Devuelve los textos exigidos por la guia; la vista construye las ventanas.
    public List<String> mostrarMenu() {
        return Arrays.asList(
                "Registrar venta",
                "Ver semaforo del día"
        );
    }

}
