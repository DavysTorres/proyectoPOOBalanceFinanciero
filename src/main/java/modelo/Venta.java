package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Entidad del modelo: encapsula sus datos sin depender de vistas, servicios ni DAO.
 */
public class Venta {

    // Estado privado; los setters final conservan las validaciones al construir subclases.
    private int id;
    private Usuario cajero;
    private LocalDateTime fechaHora;
    private BigDecimal total;
    // Un ajuste autorizado puede diferir de los subtotales; null indica un carrito sin ajuste.
    private BigDecimal totalRegistrado;
    private final List<DetalleVenta> detalles;

    // Construye el objeto e inicializa sus atributos con las validaciones disponibles.
    public Venta(Usuario cajero) {
        setCajero(cajero);
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

    // Consulta al usuario responsable de la venta.
    public Usuario getCajero() {
        return cajero;
    }

    // Exige un usuario asociado; validar sus permisos corresponde al servicio.
    public final void setCajero(Usuario cajero) {
        this.cajero = Objects.requireNonNull(
                cajero, "El cajero es obligatorio.");
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

    // Consulta el total persistido si existe un ajuste; en un carrito suma los detalles.
    public BigDecimal getTotal() {
        return totalRegistrado == null ? calcularTotal() : totalRegistrado;
    }

    // El DAO restaura el importe persistido, incluido un ajuste documentado en historial.
    public void restaurarTotalRegistrado(BigDecimal totalRegistrado) {
        this.totalRegistrado = ValidacionModelo.noNegativo(totalRegistrado, "El total registrado");
    }

    // Expone una lista que impide agregar o quitar lineas desde el exterior.
    public List<DetalleVenta> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    // Agrega una linea validada; confirmar y mover inventario corresponde al servicio.
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

        totalRegistrado = null;
        calcularTotal();
    }

    // Retira una linea del carrito y actualiza su total sin mover inventario.
    public void quitarDetalle(Producto p) {
        Objects.requireNonNull(p, "El producto es obligatorio.");

        DetalleVenta detalle = buscarDetalle(p);

        if (detalle != null) {
            detalles.remove(detalle);
            totalRegistrado = null;
            calcularTotal();
        }
    }

    // Reduce una unidad o elimina la linea cuando solo queda una.
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

        totalRegistrado = null;
        calcularTotal();
    }

    // Recorre los detalles y suma sus subtotales con BigDecimal.
    public BigDecimal calcularTotal() {
        BigDecimal suma = BigDecimal.ZERO;

        for (DetalleVenta detalle : detalles) {
            suma = suma.add(detalle.calcularSubtotal());
        }

        this.total = suma;
        return total;
    }

    // Suma los costos historicos de lo vendido, no el valor de las compras.
    public BigDecimal calcularCostoTotal() {
        BigDecimal suma = BigDecimal.ZERO;

        for (DetalleVenta detalle : detalles) {
            suma = suma.add(detalle.calcularCostoSubtotal());
        }

        return suma;
    }

    // Informa si el carrito carece de detalles.
    public boolean estaVacia() {
        return detalles.isEmpty();
    }

    // Busca por id persistido o por identidad si el producto todavia es nuevo.
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
