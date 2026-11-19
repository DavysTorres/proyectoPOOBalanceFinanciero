package modelo;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad del modelo: encapsula sus datos sin depender de vistas, servicios ni DAO.
 */
public class DetalleVenta {

    // Estado privado; los setters final conservan las validaciones al construir subclases.
    private int id;
    private final Producto producto;
    private int cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal costoUnitario;

    // Construye el objeto e inicializa sus atributos con las validaciones disponibles.
    public DetalleVenta(Producto producto, int cantidad) {
        this.producto = Objects.requireNonNull(
                producto, "El producto es obligatorio.");

        setCantidad(cantidad);

        this.precioUnitario = validarMonto(
                producto.getPrecioVenta(), "El precio");

        this.costoUnitario = validarMonto(
                producto.getCostoCompra(), "El costo");
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

    // Consulta el producto asociado al detalle.
    public Producto getProducto() {
        return producto;
    }

    // Consulta las unidades representadas por la linea.
    public int getCantidad() {
        return cantidad;
    }

    // Mantiene cantidades estrictamente positivas.
    public final void setCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero.");
        }

        this.cantidad = cantidad;
    }

    // Consulta el precio copiado al crear el detalle, independiente del precio actual.
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    // Consulta el costo historico capturado para esta linea.
    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    // Multiplica el valor unitario por la cantidad usando aritmetica decimal.
    public BigDecimal calcularSubtotal() {
        return precioUnitario.multiply(
                BigDecimal.valueOf(cantidad));
    }

    // Multiplica el costo historico unitario por las unidades vendidas.
    public BigDecimal calcularCostoSubtotal() {
        return costoUnitario.multiply(
                BigDecimal.valueOf(cantidad));
    }

    // Rechaza valores unitarios nulos o no positivos antes de copiarlos.
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
