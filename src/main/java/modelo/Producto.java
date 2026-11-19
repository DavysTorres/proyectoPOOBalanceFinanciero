package modelo;

import excepciones.StockInsuficienteException;

import java.math.BigDecimal;
import java.math.RoundingMode;


/**
 * Entidad del modelo: encapsula sus datos sin depender de vistas, servicios ni DAO.
 */
public class Producto {

    // Estado privado; los setters final conservan las validaciones al construir subclases.
    private int id;
    private String nombre;
    private BigDecimal precioVenta;
    private BigDecimal costoCompra;
    private int stock;
    private int stockMinimo;
    private boolean activo;

    // Constructor vacío
    // Construye el objeto e inicializa sus atributos con las validaciones disponibles.
    public Producto() {
        this.activo = true;
    }

    // Constructor completo con validaciones
    // Construye el objeto e inicializa sus atributos con las validaciones disponibles.
    public Producto(int id, String nombre, BigDecimal precioVenta, BigDecimal costoCompra, int stock, int stockMinimo) {
        setId(id);
        setNombre(nombre);
        setPrecioVenta(precioVenta);
        setCostoCompra(costoCompra);
        setStock(stock);
        setStockMinimo(stockMinimo);
        this.activo = true;
    }

    // Consulta el identificador; cero representa una entidad aun no guardada.
    public int getId() {
        return id;
    }

    // Valida y asigna el identificador sin permitir valores negativos.
    public final void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("El id del producto no puede ser negativo");
        }
        this.id = id;
    }

    // Consulta el nombre del producto.
    public String getNombre() {
        return nombre;
    }

    // Rechaza nombres ausentes y elimina espacios exteriores.
    public final void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        this.nombre = nombre.trim();
    }

    // Consulta el precio de venta actual del producto.
    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    // Valida que el precio de venta sea obligatorio y positivo.
    public final void setPrecioVenta(BigDecimal precioVenta) {
        if (precioVenta == null || precioVenta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio de venta debe ser mayor que cero");
        }
        this.precioVenta = precioVenta;
    }

    // Consulta el costo actual de una unidad del producto.
    public BigDecimal getCostoCompra() {
        return costoCompra;
    }

    // Rechaza costos nulos, negativos o iguales a cero.
    public final void setCostoCompra(BigDecimal costoCompra) {
        if (costoCompra == null || costoCompra.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El costo debe ser mayor que cero");
        }
        this.costoCompra = costoCompra;
    }

    // Consulta las unidades disponibles del inventario.
    public int getStock() {
        return stock;
    }

    // Evita establecer existencias negativas.
    public final void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        this.stock = stock;
    }

    // Consulta el limite utilizado para las alertas de inventario.
    public int getStockMinimo() {
        return stockMinimo;
    }

    // El limite de stock puede ser cero, pero nunca negativo.
    public final void setStockMinimo(int stockMinimo) {
        if (stockMinimo < 0) {
            throw new IllegalArgumentException("El stock minimo no puede ser negativo");
        }
        this.stockMinimo = stockMinimo;
    }

    // Consulta si el producto esta disponible para nuevas operaciones.
    public boolean isActivo() {
        return activo;
    }

    // Permite la baja logica del producto sin borrar su historial.
    public final void setActivo(boolean activo) {
        this.activo = activo;
    }

    /**
     * Calcula el margen de ganancia en porcentaje.
     * Fórmula: ((precioVenta - costoCompra) / precioVenta) * 100
     */
    public double calcularMargen() {
        if (precioVenta == null || costoCompra == null) {
            throw new IllegalStateException(
                    "El precio de venta y el costo de compra son obligatorios para calcular el margen"
            );
        }
        return precioVenta.subtract(costoCompra)
                .multiply(BigDecimal.valueOf(100))
                .divide(precioVenta, 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * Calcula la ganancia neta por cada unidad vendida.
     */
    public BigDecimal calcularUtilidadUnitaria() {
        if (precioVenta == null || costoCompra == null) {
            throw new IllegalStateException(
                    "El precio de venta y el costo de compra son obligatorios para calcular la utilidad"
            );
        }
        return precioVenta.subtract(costoCompra);
    }

    /**
     * Verifica si el stock está en el límite o por debajo del mínimo.
     */
    public boolean tieneStockBajo() {
        return this.stock <= this.stockMinimo;
    }

    /**
     * Verifica si hay suficiente stock para cubrir una cantidad solicitada.
     */
    public boolean hayStock(int cantidad) {
        if (cantidad < 0) return false;
        return this.stock >= cantidad;
    }

    /**
     * Descuenta stock de la cantidad disponible.
     * Lanza una excepción si no hay suficiente stock.
     */
    public void descontarStock(int cantidad) throws StockInsuficienteException {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        if (!hayStock(cantidad)) {
            throw new StockInsuficienteException(
                    "Stock insuficiente para el producto '" + this.nombre + "'. " +
                            "Solicitado: " + cantidad + ", Disponible: " + this.stock
            );
        }
        this.stock -= cantidad;
    }

    /**
     * Aumenta el stock (ej: al registrar una compra).
     */
    public void aumentarStock(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a aumentar debe ser mayor a cero.");
        }
        if (cantidad > Integer.MAX_VALUE - this.stock) {
            throw new IllegalArgumentException("La cantidad supera el stock maximo permitido");
        }
        this.stock += cantidad;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", precioVenta=" + precioVenta +
                ", costoCompra=" + costoCompra +
                ", stock=" + stock +
                ", stockMinimo=" + stockMinimo +
                ", activo=" + activo +
                ", margen=" + (precioVenta == null || costoCompra == null
                ? "sin calcular"
                : String.format("%.2f", calcularMargen()) + "%") +
                '}';
    }


}
