package servicio;

import dao.HistorialCambiosDAO;
import modelo.*;

import java.time.*;
import java.util.*;

/**
 * Solo consultas: el historial se genera dentro de la modificacion transaccional de una venta.
 */
public class ServicioHistorial extends ServicioBase {
    private final HistorialCambiosDAO historial;

    // No ofrece metodos de borrar ni editar evidencia.
    public ServicioHistorial() {
        this(new HistorialCambiosDAO(), Clock.systemDefaultZone());
    }

    public ServicioHistorial(HistorialCambiosDAO historial, Clock reloj) {
        super(reloj);
        this.historial = Objects.requireNonNull(historial);
    }

    // Los tres filtros aplican el mismo permiso.
    public List<RegistroCambio> listarTodos() {
        sesion.exigirPermiso(Permiso.VER_HISTORIAL);
        return historial.listarTodos();
    }

    public List<RegistroCambio> listarPorFecha(LocalDate fecha) {
        sesion.exigirPermiso(Permiso.VER_HISTORIAL);
        return historial.listarPorFecha(fecha);
    }

    public List<RegistroCambio> listarPorVenta(int id) {
        sesion.exigirPermiso(Permiso.VER_HISTORIAL);
        return historial.listarPorVenta(id);
    }
}
