package modelo;
 
import java.math.BigDecimal;
import java.util.Objects;
 
public class DetalleCompra {
 
    private int id;
    private final Producto producto;
    private int cantidad;
    private final BigDecimal costoUnitario;
 
    public DetalleCompra(
            Producto producto, int cantidad, BigDecimal costoUnitario) {
 
        this.producto = Objects.requireNonNull(
                producto, "El producto es obligatorio.");
 
        setCantidad(cantidad);
 
        Objects.requireNonNull(
                costoUnitario, "El costo unitario es obligatorio.");
 
        if (costoUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El costo unitario debe ser mayor que cero.");
        }
 
        this.costoUnitario = costoUnitario;
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
 
    public Producto getProducto() {
        return producto;
    }
 
    public int getCantidad() {
        return cantidad;
    }
 
    public void setCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero.");
        }
 
        this.cantidad = cantidad;
    }
 
    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }
 
    public BigDecimal calcularSubtotal() {
        return costoUnitario.multiply(
                BigDecimal.valueOf(cantidad));
    }
}