package servicio;

import dao.CostoFijoDAO;
import modelo.*;
import excepciones.DatosInvalidosException;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.*;

/**
 * Permite al administrador configurar los costos que se distribuyen diariamente.
 */
public class ServicioCostosFijos extends ServicioBase {
    private final CostoFijoDAO costos;

    // Un solo catalogo se comparte con el servicio de balance.
    public ServicioCostosFijos() {
        this(new CostoFijoDAO(), Clock.systemDefaultZone());
    }

    public ServicioCostosFijos(CostoFijoDAO costos, Clock reloj) {
        super(reloj);
        this.costos = Objects.requireNonNull(costos);
    }

    // El modelo valida concepto y monto; los permisos se comprueban aqui.
    public CostoFijo crear(String concepto, BigDecimal montoMensual) {
        sesion.exigirPermiso(Permiso.DEFINIR_COSTOS_FIJOS);
        CostoFijo c = new CostoFijo(0, concepto, montoMensual, true);
        costos.guardar(c);
        return c;
    }

    public CostoFijo editar(int id, String concepto, BigDecimal montoMensual, boolean activo) {
        sesion.exigirPermiso(Permiso.DEFINIR_COSTOS_FIJOS);
        if (costos.buscarPorId(id) == null) throw new DatosInvalidosException("Costo inexistente");
        CostoFijo c = new CostoFijo(id, concepto, montoMensual, activo);
        costos.actualizar(c);
        return c;
    }

    // Desactivar no elimina el costo ni cambia los cierres ya guardados.
    public void desactivar(int id) {
        sesion.exigirPermiso(Permiso.DEFINIR_COSTOS_FIJOS);
        CostoFijo c = costos.buscarPorId(id);
        if (c == null) throw new DatosInvalidosException("Costo inexistente");
        c.setActivo(false);
        costos.actualizar(c);
    }

    public List<CostoFijo> listar() {
        sesion.exigirPermiso(Permiso.DEFINIR_COSTOS_FIJOS);
        return costos.listarTodos();
    }
}
