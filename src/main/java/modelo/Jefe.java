package modelo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class Jefe extends Usuario{
    //Constructor
    public Jefe(int id, String nombre, String username, String passwordHash, boolean activo, LocalDateTime creadoEn) {
        super(id, nombre, username, passwordHash, Rol.JEFE, activo, creadoEn);
    }

    @Override
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
    public List<String> mostrarMenu() {
        return Arrays.asList(
                "Registrar venta",
                "Compras y gastos",
                "Balance",
                "Margen por producto",
                "Historial",
                "Cierre de caja"
        );
    }    
}
