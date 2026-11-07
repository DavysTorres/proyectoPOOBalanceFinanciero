package servicio;

import dao.*;
import modelo.*;
import excepciones.DatosInvalidosException;

import java.time.*;
import java.util.*;

/**
 * Autoriza compras y gastos y delega al DAO la unica actualizacion del inventario.
 */
public class ServicioCompras extends ServicioBase {
    private final CompraDAO compras;
    private final GastoDAO gastos;
    private final ProductoDAO productos;
    private final CostoFijoDAO costos;
    private final ServicioCierre cierre;

    // Recibe dependencias para simular un dia con reloj y base de pruebas.
    public ServicioCompras() {
        this(new CompraDAO(), new GastoDAO(), new ProductoDAO(), new CostoFijoDAO(), new ServicioCierre(), Clock.systemDefaultZone());
    }

    public ServicioCompras(CompraDAO compras, GastoDAO gastos, ProductoDAO productos, CostoFijoDAO costos, ServicioCierre cierre, Clock reloj) {
        super(reloj);
        this.compras = Objects.requireNonNull(compras);
        this.gastos = Objects.requireNonNull(gastos);
        this.productos = Objects.requireNonNull(productos);
        this.costos = Objects.requireNonNull(costos);
        this.cierre = Objects.requireNonNull(cierre);
    }

    // El usuario y la fecha proceden de la sesion, no de valores libres de la ventana.
    public Compra registrarCompra(Compra compra) {
        sesion.exigirPermiso(Permiso.REGISTRAR_COMPRA);
        Objects.requireNonNull(compra);
        if (compra.getId() != 0 || compra.getDetalles().isEmpty())
            throw new DatosInvalidosException("Compra nueva con detalles obligatoria");
        for (DetalleCompra d : compra.getDetalles()) {
            Producto p = productos.buscarPorId(d.getProducto().getId());
            if (p == null || !p.isActivo()) throw new DatosInvalidosException("Producto no disponible");
        }
        compra.setUsuario(usuario());
        compra.setFechaHora(LocalDateTime.now(reloj));
        try {
            compras.transaccion(() -> {
                cierre.exigirDiaAbierto(hoy());
                compras.guardarConDetalles(compra);
                return null;
            });
        } catch (RuntimeException e) {
            compra.setId(0);
            throw e;
        }
        return compra;
    }

    // Los gastos fijos adicionales no deben duplicar un concepto del catalogo activo.
    public Gasto registrarGasto(Gasto gasto) {
        sesion.exigirPermiso(Permiso.REGISTRAR_GASTO);
        Objects.requireNonNull(gasto);
        if (gasto.getId() != 0) throw new DatosInvalidosException("El gasto ya fue guardado");
        if (gasto instanceof GastoFijo)
            for (CostoFijo c : costos.listarActivos())
                if (c.getConcepto().equalsIgnoreCase(gasto.getConcepto()))
                    throw new DatosInvalidosException("El costo fijo ya esta configurado; no lo registre de nuevo");
        gasto.setUsuario(usuario());
        gasto.setFecha(hoy());
        try {
            gastos.transaccion(() -> {
                cierre.exigirDiaAbierto(hoy());
                gastos.guardar(gasto);
                return null;
            });
        } catch (RuntimeException e) {
            gasto.setId(0);
            throw e;
        }
        return gasto;
    }

    // Las consultas se restringen al perfil que puede operar esa seccion.
    public List<Compra> listarComprasDelDia() {
        sesion.exigirPermiso(Permiso.REGISTRAR_COMPRA);
        return compras.listarPorFecha(hoy());
    }

    public List<Gasto> listarGastosDelDia() {
        sesion.exigirPermiso(Permiso.REGISTRAR_GASTO);
        return gastos.listarPorFecha(hoy());
    }
}
