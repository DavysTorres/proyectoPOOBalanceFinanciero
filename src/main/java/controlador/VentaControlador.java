package controlador;

import vista.contratos.*;
import servicio.*;
import modelo.*;

import java.util.Objects;

/**
 * Traduce acciones del carrito a servicios sin calcular precios ni modificar stock.
 */
public final class VentaControlador extends ControladorBase<VistaVenta> {
    private final ServicioVentas ventas;
    private final ServicioProductos productos;
    private Venta ventaVisible;

    // El modelo visible es el mismo carrito devuelto por el servicio.
    public VentaControlador(VistaVenta vista, ServicioVentas ventas, ServicioProductos productos, EjecutorTareas tareas) {
        super(vista, tareas);
        this.ventas = Objects.requireNonNull(ventas);
        this.productos = Objects.requireNonNull(productos);
        vista.alAccionar(AccionVista.NUEVA_VENTA, this::iniciarVenta);
        vista.alAccionar(AccionVista.BUSCAR, this::buscarProductos);
        vista.alAccionar(AccionVista.AGREGAR, this::agregarAlCarrito);
        vista.alAccionar(AccionVista.AUMENTAR, this::aumentarCantidad);
        vista.alAccionar(AccionVista.QUITAR, this::quitarDelCarrito);
        vista.alAccionar(AccionVista.REDUCIR, this::reducirCantidad);
        vista.alAccionar(AccionVista.COBRAR, this::cobrar);
        vista.alAccionar(AccionVista.CANCELAR, this::cancelar);
    }

    // Iniciar es explicito: no se descarta un carrito anterior al abrir otra pantalla.
    public void iniciarVenta() {
        ejecutar(ventas::iniciarVenta, v -> {
            ventaVisible = v;
            vista.mostrarVenta(v);
        });
    }

    public void buscarProductos() {
        preparar(() -> {
            String filtro = vista.getFiltro();
            ejecutar(() -> productos.buscarProductos(filtro), encontrados -> {
                if (Objects.equals(filtro, vista.getFiltro())) vista.mostrarProductos(encontrados);
            });
        });
    }

    public void agregarAlCarrito() {
        preparar(() -> {
            Producto p = seleccionado(vista.getProductoSeleccionado());
            int cantidad = entero(vista.getCantidad());
            if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
            ejecutar(() -> {
                boolean nueva = ventaVisible == null;
                if (nueva) ventaVisible = ventas.iniciarVenta();
                try { ventas.agregarProducto(p, cantidad); }
                catch (Exception error) {
                    if (nueva) { ventas.cancelarVenta(); ventaVisible = null; }
                    throw error;
                }
                return ventaVisible;
            }, v -> { vista.mostrarVenta(v); vista.productoAgregado(); });
        });
    }

    public void aumentarCantidad() {
        preparar(() -> {
            Producto p = seleccionado(vista.getProductoEnCarrito());
            ejecutar(() -> { ventas.agregarProducto(p, 1); return ventaVisible; }, vista::mostrarVenta);
        });
    }

    // Las selecciones se leen antes del trabajo en segundo plano.
    public void quitarDelCarrito() {
        preparar(() -> {
            Producto p = seleccionado(vista.getProductoEnCarrito());
            ejecutar(() -> {
                ventas.quitarProducto(p);
                return ventaVisible;
            }, vista::mostrarVenta);
        });
    }

    public void reducirCantidad() {
        preparar(() -> {
            Producto p = seleccionado(vista.getProductoEnCarrito());
            ejecutar(() -> {
                ventas.reducirCantidad(p);
                return ventaVisible;
            }, vista::mostrarVenta);
        });
    }

    // Solo presenta exito despues de que el servicio confirmo la escritura completa.
    public void cobrar() {
        preparar(() -> {
            String recibido = vista.getEfectivoRecibido();
            if (recibido != null && !recibido.isBlank()) {
                java.math.BigDecimal efectivo = decimal(recibido);
                if (efectivo.signum() < 0 || (ventaVisible != null && efectivo.compareTo(ventaVisible.getTotal()) < 0))
                    throw new IllegalArgumentException("El efectivo recibido no alcanza para pagar la venta.");
            }
            ejecutar(ventas::cobrar, v -> {
                ventaVisible = null;
                vista.mostrarVenta(null);
                vista.mostrarVentaCobrada(v);
                informarCambio();
            });
        });
    }

    public void cancelar() {
        ejecutar(() -> {
            ventas.cancelarVenta();
            return null;
        }, ignorado -> {
            ventaVisible = null;
            vista.mostrarVenta(null);
        });
    }
}
