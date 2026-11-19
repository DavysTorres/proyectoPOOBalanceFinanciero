package modelo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

<<<<<<< HEAD
public class Jefe extends Usuario{
    //Constructor
=======
/**
 * Entidad del modelo: encapsula sus datos sin depender de vistas, servicios ni DAO.
 */
public class Jefe extends Usuario {

    //Constructor
    // Construye el objeto e inicializa sus atributos con las validaciones disponibles.
>>>>>>> c6b38029e9d5f9f3c350cb56d301388f5b5859f1
    public Jefe(int id, String nombre, String username, String passwordHash, boolean activo, LocalDateTime creadoEn) {
        super(id, nombre, username, passwordHash, Rol.JEFE, activo, creadoEn);
    }

    @Override
<<<<<<< HEAD
=======
    // Implementa por polimorfismo las capacidades del perfil indicado en la guia.
>>>>>>> c6b38029e9d5f9f3c350cb56d301388f5b5859f1
    public List<Permiso> obtenerPermisos() {
        return Arrays.asList(
                Permiso.REGISTRAR_VENTA,
                Permiso.REGISTRAR_COMPRA,
                Permiso.REGISTRAR_GASTO,
                Permiso.MODIFICAR_VENTA,
                Permiso.VER_BALANCE,
                Permiso.VER_REPORTES,
                Permiso.VER_HISTORIAL,
                Permiso.CERRAR_CAJA);
    }

    @Override
<<<<<<< HEAD
=======
    // Devuelve los textos exigidos por la guia; la vista construye las ventanas.
>>>>>>> c6b38029e9d5f9f3c350cb56d301388f5b5859f1
    public List<String> mostrarMenu() {
        return Arrays.asList(
                "Registrar venta",
                "Compras y gastos",
                "Balance",
                "Margen por producto",
                "Historial",
                "Cierre de caja"
        );
<<<<<<< HEAD
    }    
=======
    }

>>>>>>> c6b38029e9d5f9f3c350cb56d301388f5b5859f1
}
