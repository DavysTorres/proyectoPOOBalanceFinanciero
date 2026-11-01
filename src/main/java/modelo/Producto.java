package modelo;

import excepciones.StockInsuficienteException;

import java.math.BigDecimal;
import java.math.RoundingMode;



public class Producto {

    private int id;
    private String nombre;
    private BigDecimal precioVenta;
    private BigDecimal costoCompra;
    private int stock;
    private int stockMinimo;
    private boolean activo;

    // Constructor vacío
    public Producto() {
        this.activo = true;
    }

    // Constructor completo con validaciones
    public Producto(int id, String nombre, BigDecimal precioVenta, BigDecimal costoCompra, int stock, int stockMinimo) {
        setId(id);
        setNombre(nombre);
        setPrecioVenta(precioVenta);
        setCostoCompra(costoCompra);
        setStock(stock);
        setStockMinimo(stockMinimo);
        this.activo = true;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("El id del producto no puede ser negativo");
        }
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        if (nombre == null ||  nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        this.nombre = nombre.trim();
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }
    public void setPrecioVenta(BigDecimal precioVenta) {
        if (precioVenta == null || precioVenta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio de venta debe ser mayor que cero");
        }
        this.precioVenta = precioVenta;
    }

    public BigDecimal getCostoCompra() {
        return costoCompra;
    }
    public void setCostoCompra(BigDecimal costoCompra) {
        if (costoCompra == null || costoCompra.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El costo debe ser mayor que cero");
        }
        this.costoCompra = costoCompra;
    }

    public int getStock() {
        return stock;
    }
    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        this.stock = stock;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }
    public void setStockMinimo(int stockMinimo) {
        if (stockMinimo < 0) {
            throw new IllegalArgumentException("El stock minimo no puede ser negativo");
        }
        this.stockMinimo = stockMinimo;
    }

    public boolean isActivo() {
        return activo;
    }
    public void setActivo(boolean activo) {
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
        if(cantidad < 0) return false;
        return this.stock >= cantidad;
    }

    /**
     * Descuenta stock de la cantidad disponible.
     * Lanza una excepción si no hay suficiente stock.
     */
    public void descontarStock(int cantidad) throws StockInsuficienteException{
        if(cantidad <= 0){
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        if(!hayStock(cantidad)){
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
