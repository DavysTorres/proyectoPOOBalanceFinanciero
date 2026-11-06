package modelo;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
 
public class Compra {
 
    private int id;
    private Usuario usuario;
    private String proveedor;
    private LocalDateTime fechaHora;
    private BigDecimal total;
    private final List<DetalleCompra> detalles;
 
    public Compra(Usuario usuario, String proveedor) {
        setUsuario(usuario);
        setProveedor(proveedor);
 
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
 
    public Usuario getUsuario() {
        return usuario;
    }
 
    public void setUsuario(Usuario usuario) {
        this.usuario = Objects.requireNonNull(
                usuario, "El usuario es obligatorio.");
    }
 
    public String getProveedor() {
        return proveedor;
    }
 
    public void setProveedor(String proveedor) {
        if (proveedor == null || proveedor.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El proveedor es obligatorio.");
        }
 
        this.proveedor = proveedor.trim();
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
 
    public List<DetalleCompra> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }
 
    public void agregarDetalle(
            Producto p, int cantidad, BigDecimal costoUnitario) {
 
        Objects.requireNonNull(
                p, "El producto es obligatorio.");
 
        Objects.requireNonNull(
                costoUnitario, "El costo unitario es obligatorio.");
 
        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero.");
        }
 
        if (costoUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El costo unitario debe ser mayor que cero.");
        }
 
        DetalleCompra detalle = new DetalleCompra(
                p, cantidad, costoUnitario);
 
        detalles.add(detalle);
        calcularTotal();
    }
 
    public BigDecimal calcularTotal() {
        BigDecimal suma = BigDecimal.ZERO;
 
        for (DetalleCompra detalle : detalles) {
            suma = suma.add(detalle.calcularSubtotal());
        }
 
        this.total = suma;
        return total;
    }
}