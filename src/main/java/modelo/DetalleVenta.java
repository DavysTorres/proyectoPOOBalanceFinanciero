package modelo;
 
import java.math.BigDecimal;
import java.util.Objects;
 
public class DetalleVenta {
 
    private int id;
    private final Producto producto;
    private int cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal costoUnitario;
 
    public DetalleVenta(Producto producto, int cantidad) {
        this.producto = Objects.requireNonNull(
                producto, "El producto es obligatorio.");
 
        setCantidad(cantidad);
 
        this.precioUnitario = validarMonto(
                producto.getPrecioVenta(), "El precio");
 
        this.costoUnitario = validarMonto(
                producto.getCostoCompra(), "El costo");
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
 
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
 
    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }
 
    public BigDecimal calcularSubtotal() {
        return precioUnitario.multiply(
                BigDecimal.valueOf(cantidad));
    }
 
    public BigDecimal calcularCostoSubtotal() {
        return costoUnitario.multiply(
                BigDecimal.valueOf(cantidad));
    }
 
    private static BigDecimal validarMonto(
            BigDecimal monto, String nombre) {
 
        Objects.requireNonNull(
                monto, nombre + " es obligatorio.");
 
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    nombre + " debe ser mayor que cero.");
        }
 
        return monto;
    }
}