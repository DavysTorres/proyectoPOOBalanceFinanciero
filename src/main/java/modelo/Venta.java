package modelo;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
 
public class Venta {
 
    private int id;
    private Usuario cajero;
    private LocalDateTime fechaHora;
    private BigDecimal total;
    private final List<DetalleVenta> detalles;
 
    public Venta(Usuario cajero) {
        setCajero(cajero);
        this.fechaHora = LocalDateTime.now();
        this.total = BigDecimal.ZERO;
        this.detalles = new ArrayList<>();
    }
 
    public int getId() {
        return id;
    }
 
    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException(
                    "El id no puede ser negativo.");
        }
        this.id = id;
    }
 
    public Usuario getCajero() {
        return cajero;
    }
 
    public void setCajero(Usuario cajero) {
        this.cajero = Objects.requireNonNull(
                cajero, "El cajero es obligatorio.");
    }
 
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
 
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = Objects.requireNonNull(
                fechaHora, "La fecha es obligatoria.");
    }
 
    public BigDecimal getTotal() {
        return calcularTotal();
    }
 
    public List<DetalleVenta> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }
 
    public void agregarDetalle(Producto p, int cantidad) {
        Objects.requireNonNull(p, "El producto es obligatorio.");
 
        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero.");
        }
 
        DetalleVenta detalle = buscarDetalle(p);
 
        if (detalle == null) {
            // El constructor copia el precio y costo del producto.
            detalles.add(new DetalleVenta(p, cantidad));
        } else {
            // Conserva el precio y costo guardados originalmente.
            int nuevaCantidad = Math.addExact(
                    detalle.getCantidad(), cantidad);
 
            detalle.setCantidad(nuevaCantidad);
        }
 
        calcularTotal();
    }
 
    public void quitarDetalle(Producto p) {
        Objects.requireNonNull(p, "El producto es obligatorio.");
 
        DetalleVenta detalle = buscarDetalle(p);
 
        if (detalle != null) {
            detalles.remove(detalle);
            calcularTotal();
        }
    }
 
    public void reducirCantidad(Producto p) {
        Objects.requireNonNull(p, "El producto es obligatorio.");
 
        DetalleVenta detalle = buscarDetalle(p);
 
        if (detalle == null) {
            return;
        }
 
        if (detalle.getCantidad() == 1) {
            detalles.remove(detalle);
        } else {
            detalle.setCantidad(detalle.getCantidad() - 1);
        }
 
        calcularTotal();
    }
 
    public BigDecimal calcularTotal() {
        BigDecimal suma = BigDecimal.ZERO;
 
        for (DetalleVenta detalle : detalles) {
            suma = suma.add(detalle.calcularSubtotal());
        }
 
        this.total = suma;
        return total;
    }
 
    public BigDecimal calcularCostoTotal() {
        BigDecimal suma = BigDecimal.ZERO;
 
        for (DetalleVenta detalle : detalles) {
            suma = suma.add(detalle.calcularCostoSubtotal());
        }
 
        return suma;
    }
 
    public boolean estaVacia() {
        return detalles.isEmpty();
    }
 
    private DetalleVenta buscarDetalle(Producto producto) {
        for (DetalleVenta detalle : detalles) {
            Producto actual = detalle.getProducto();
 
            // Para productos persistidos, compara por id.
            // Si aún no tienen id, compara la misma instancia.
            if (actual == producto
                    || (producto.getId() > 0
&& actual.getId() == producto.getId())) {
                return detalle;
            }
        }
 
        return null;
    }
}
