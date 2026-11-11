package servicio;

import dao.*;
import modelo.*;
import excepciones.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.Objects;

/**
 * Controla el carrito, el cobro y los ajustes autorizados sin SQL en el servicio.
 */
public class ServicioVentas extends ServicioBase {
    private final VentaDAO ventas;
    private final ProductoDAO productos;
    private final HistorialCambiosDAO historial;
    private final ServicioCierre cierre;
    private Venta ventaEnCurso;

    // Todas las dependencias normales terminan en el unico administrador de conexion.
    public ServicioVentas() {
        this(new VentaDAO(), new ProductoDAO(), new HistorialCambiosDAO(), new ServicioCierre(), Clock.systemDefaultZone());
    }

    public ServicioVentas(VentaDAO ventas, ProductoDAO productos, HistorialCambiosDAO historial, ServicioCierre cierre, Clock reloj) {
        super(reloj);
        this.ventas = Objects.requireNonNull(ventas);
        this.productos = Objects.requireNonNull(productos);
        this.historial = Objects.requireNonNull(historial);
        this.cierre = Objects.requireNonNull(cierre);
    }

    // Un carrito existente se cancela explicitamente para no perder trabajo por accidente.
    public Venta iniciarVenta() {
        sesion.exigirPermiso(Permiso.REGISTRAR_VENTA);
        cierre.exigirDiaAbierto(hoy());
        if (ventaEnCurso != null) throw new DatosInvalidosException("Cobre o cancele la venta actual");
        ventaEnCurso = new Venta(usuario());
        ventaEnCurso.setFechaHora(LocalDateTime.now(reloj));
        return ventaEnCurso;
    }

    // Siempre consulta el producto real; no confia en precios enviados por una ventana.
    public void agregarProducto(Producto producto, int cantidad) throws StockInsuficienteException {
        Venta v = exigirCarrito();
        if (cantidad <= 0) throw new DatosInvalidosException("Cantidad mayor que cero");
        Producto actual = productos.buscarPorId(Objects.requireNonNull(producto).getId());
        if (actual == null || !actual.isActivo())
            throw new DatosInvalidosException("Producto inexistente o desactivado");
        int solicitada = cantidad;
        for (DetalleVenta d : v.getDetalles())
            if (d.getProducto().getId() == actual.getId())
                solicitada = Math.addExact(solicitada, d.getCantidad());
        if (!actual.hayStock(solicitada))
            throw new StockInsuficienteException("Stock insuficiente para " + actual.getNombre());
        v.agregarDetalle(actual, cantidad);
    }

    // Quitar y reducir solo modifican el carrito, nunca el inventario persistido.
    public void quitarProducto(Producto p) {
        exigirCarrito().quitarDetalle(p);
    }

    public void reducirCantidad(Producto p) {
        exigirCarrito().reducirCantidad(p);
    }

    public BigDecimal calcularTotalEnCurso() {
        return exigirCarrito().getTotal();
    }

    // Verifica propietario, fecha y permiso incluso si la sesion cambio desde el inicio.
    private Venta exigirCarrito() {
        sesion.exigirPermiso(Permiso.REGISTRAR_VENTA);
        if (ventaEnCurso == null) throw new DatosInvalidosException("No hay venta en curso");
        if (ventaEnCurso.getCajero().getId() != usuario().getId())
            throw new PermisoDenegadoException("La venta pertenece a otro usuario");
        if (!ventaEnCurso.getFechaHora().toLocalDate().equals(hoy()))
            throw new DatosInvalidosException("La venta pertenece a otra jornada; debe cancelarla");
        cierre.exigirDiaAbierto(hoy());
        return ventaEnCurso;
    }

    // Recarga precios y existencias antes de cobrar; el DAO vuelve a proteger el stock con SQL atomico.
    public Venta cobrar() throws StockInsuficienteException {
        Venta v = exigirCarrito();
        if (v.estaVacia()) throw new DatosInvalidosException("No se puede cobrar una venta vacia");
        for (DetalleVenta d : v.getDetalles()) {
            Producto actual = productos.buscarPorId(d.getProducto().getId());
            if (actual == null || !actual.isActivo()) throw new DatosInvalidosException("Producto no disponible");
            if (!actual.hayStock(d.getCantidad()))
                throw new StockInsuficienteException("Stock insuficiente para " + actual.getNombre());
            if (actual.getPrecioVenta().compareTo(d.getPrecioUnitario()) != 0
                    || actual.getCostoCompra().compareTo(d.getCostoUnitario()) != 0)
                throw new DatosInvalidosException("El precio o costo cambio; reinicie la venta con el catalogo actualizado");
        }
        v.setFechaHora(LocalDateTime.now(reloj));
        try {
            ventas.transaccion(() -> {
                cierre.exigirDiaAbierto(hoy());
                ventas.guardarConDetalles(v);
                return null;
            });
        } catch (RuntimeException e) {
            v.setId(0);
            throw e;
        }
        ventaEnCurso = null;
        return v;
    }

    // Cancelar se permite incluso al cambiar de fecha; no necesita deshacer movimientos SQL.
    public void cancelarVenta() {
        usuario();
        if (ventaEnCurso != null && ventaEnCurso.getCajero().getId() != usuario().getId())
            throw new PermisoDenegadoException("La venta pertenece a otro usuario");
        ventaEnCurso = null;
    }

    // El carrito no esta persistido: al terminar la sesion se descarta, incluso si el usuario fue desactivado.
    public void descartarCarritoAlCerrarSesion() {
        ventaEnCurso = null;
    }

    // La pantalla de ajustes consulta sin acceder al DAO directamente desde un controlador.
    public Venta buscarVenta(int ventaId) {
        sesion.exigirPermiso(Permiso.MODIFICAR_VENTA);
        Venta venta = ventas.buscarPorId(ventaId);
        if (venta == null) throw new DatosInvalidosException("Venta inexistente");
        return venta;
    }

    // El ajuste afecta el total, no cantidades ni inventario, y siempre genera historial.
    public Venta modificarVenta(int ventaId, BigDecimal nuevoTotal, String motivo) {
        sesion.exigirPermiso(Permiso.MODIFICAR_VENTA);
        String razon = texto(motivo, "El motivo");
        if (nuevoTotal == null || nuevoTotal.signum() < 0)
            throw new DatosInvalidosException("Total no negativo obligatorio");
        return ventas.transaccion(() -> {
            Venta v = ventas.buscarPorId(ventaId);
            if (v == null) throw new DatosInvalidosException("Venta inexistente");
            cierre.exigirDiaAbierto(v.getFechaHora().toLocalDate());
            BigDecimal anterior = v.getTotal();
            ventas.actualizarTotal(ventaId, nuevoTotal);
            historial.guardar(new RegistroCambio(0, v, usuario(), LocalDateTime.now(reloj), anterior, nuevoTotal, razon));
            v.restaurarTotalRegistrado(nuevoTotal);
            return v;
        });
    }
}
