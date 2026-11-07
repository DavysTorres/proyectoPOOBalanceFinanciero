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
            ejecutar(() -> productos.buscarProductos(filtro), vista::mostrarProductos);
        });
    }

    public void agregarAlCarrito() {
        preparar(() -> {
            Producto p = seleccionado(vista.getProductoSeleccionado());
            int cantidad = entero(vista.getCantidad());
            ejecutar(() -> {
                ventas.agregarProducto(p, cantidad);
                return ventaVisible;
            }, vista::mostrarVenta);
        });
    }

    // Las selecciones se leen antes del trabajo en segundo plano.
    public void quitarDelCarrito() {
        preparar(() -> {
            Producto p = seleccionado(vista.getProductoSeleccionado());
            ejecutar(() -> {
                ventas.quitarProducto(p);
                return ventaVisible;
            }, vista::mostrarVenta);
        });
    }

    public void reducirCantidad() {
        preparar(() -> {
            Producto p = seleccionado(vista.getProductoSeleccionado());
            ejecutar(() -> {
                ventas.reducirCantidad(p);
                return ventaVisible;
            }, vista::mostrarVenta);
        });
    }

    // Solo presenta exito despues de que el servicio confirmo la escritura completa.
    public void cobrar() {
        ejecutar(ventas::cobrar, v -> {
            ventaVisible = null;
            vista.mostrarVenta(null);
            vista.mostrarVentaCobrada(v);
            informarCambio();
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
