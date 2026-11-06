package modelo;
 
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
 
/** Entidad del modelo: encapsula sus datos sin depender de vistas, servicios ni DAO. */
public class Compra {
 
    // Estado privado; los setters final conservan las validaciones al construir subclases.
    private int id;
    private Usuario usuario;
    private String proveedor;
    private LocalDateTime fechaHora;
    private BigDecimal total;
    private final List<DetalleCompra> detalles;
 
    // Construye el objeto e inicializa sus atributos con las validaciones disponibles.
    public Compra(Usuario usuario, String proveedor) {
        setUsuario(usuario);
        setProveedor(proveedor);
 
        this.fechaHora = LocalDateTime.now();
        this.total = BigDecimal.ZERO;
        this.detalles = new ArrayList<>();
    }
 
    // Consulta el identificador; cero representa una entidad aun no guardada.
    public int getId() {
        return id;
    }
 
    // Valida y asigna el identificador sin permitir valores negativos.
    public final void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException(
                    "El id no puede ser negativo.");
        }
 
        this.id = id;
    }
 
    // Consulta al usuario responsable de registrar la compra.
    public Usuario getUsuario() {
        return usuario;
    }
 
    // Exige un usuario asociado; autorizarlo corresponde al servicio.
    public final void setUsuario(Usuario usuario) {
        this.usuario = Objects.requireNonNull(
                usuario, "El usuario es obligatorio.");
    }
 
    // Consulta el proveedor de la compra.
    public String getProveedor() {
        return proveedor;
    }
 
    // Valida el proveedor y elimina espacios exteriores.
    public final void setProveedor(String proveedor) {
        if (proveedor == null || proveedor.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El proveedor es obligatorio.");
        }
 
        this.proveedor = proveedor.trim();
    }
 
    // Consulta la fecha y hora de registro de la operacion.
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
 
    // Evita operaciones sin fecha y hora de registro.
    public final void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = Objects.requireNonNull(
                fechaHora, "La fecha es obligatoria.");
    }
 
    // Recalcula el total para reflejar las cantidades actuales de los detalles.
    public BigDecimal getTotal() {
        return calcularTotal();
    }
 
    // Expone una lista que impide agregar o quitar lineas desde el exterior.
    public List<DetalleCompra> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }
 
    // Agrega una linea validada; confirmar y mover inventario corresponde al servicio.
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
 
    // Recorre los detalles y suma sus subtotales con BigDecimal.
    public BigDecimal calcularTotal() {
        BigDecimal suma = BigDecimal.ZERO;
 
        for (DetalleCompra detalle : detalles) {
            suma = suma.add(detalle.calcularSubtotal());
        }
 
        this.total = suma;
        return total;
    }
}
