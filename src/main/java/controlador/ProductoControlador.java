package controlador;

import vista.contratos.*;
import servicio.ServicioProductos;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Convierte campos y solicita operaciones de catalogo; no ajusta inventario por su cuenta.
 */
public final class ProductoControlador extends ControladorBase<VistaProductos> {
    private final ServicioProductos productos;

    // Los cambios de stock pertenecen a compras y ventas; editar solo cambia el catalogo.
    public ProductoControlador(VistaProductos vista, ServicioProductos productos, EjecutorTareas tareas) {
        super(vista, tareas);
        this.productos = Objects.requireNonNull(productos);
        vista.alAccionar(AccionVista.CREAR, this::crear);
        vista.alAccionar(AccionVista.EDITAR, this::editar);
        vista.alAccionar(AccionVista.DESACTIVAR, this::desactivar);
        vista.alAccionar(AccionVista.LISTAR, this::listar);
    }

    public void crear() {
        preparar(() -> {
            String nombre = vista.getNombre();
            BigDecimal precio = decimal(vista.getPrecio()), costo = decimal(vista.getCosto());
            int stock = entero(vista.getStock()), minimo = entero(vista.getStockMinimo());
            ejecutar(() -> productos.crearProducto(nombre, precio, costo, stock, minimo), producto -> {
                vista.mostrarProducto(producto);
                informarCambio();
            });
        });
    }

    public void editar() {
        preparar(() -> {
            int id = id(vista.getIdSeleccionado());
            String nombre = vista.getNombre();
            BigDecimal precio = decimal(vista.getPrecio()), costo = decimal(vista.getCosto());
            int minimo = entero(vista.getStockMinimo());
            ejecutar(() -> productos.editarProducto(id, nombre, precio, costo, minimo), producto -> {
                vista.mostrarProducto(producto);
                informarCambio();
            });
        });
    }

    public void desactivar() {
        preparar(() -> {
            int id = id(vista.getIdSeleccionado());
            ejecutar(() -> {
                productos.desactivarProducto(id);
                return null;
            }, ignorado -> {
                vista.mostrarInformacion("Producto desactivado.");
                informarCambio();
            });
        });
    }

    public void listar() {
        ejecutar(productos::listarProductos, vista::mostrarProductos);
    }
}
