package servicio;

import dao.ProductoDAO;
import modelo.*;
import excepciones.DatosInvalidosException;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.*;

/**
 * Separa las operaciones administrativas del catalogo que puede consultar un cajero.
 */
public class ServicioProductos extends ServicioBase {
    private final ProductoDAO productos;

    // La conexion se obtiene por el DAO existente, no desde el servicio.
    public ServicioProductos() {
        this(new ProductoDAO(), Clock.systemDefaultZone());
    }

    public ServicioProductos(ProductoDAO productos, Clock reloj) {
        super(reloj);
        this.productos = Objects.requireNonNull(productos);
    }

    // El modelo valida precios y cantidades antes de escribir.
    public Producto crearProducto(String nombre, BigDecimal precio, BigDecimal costo, int stock, int minimo) {
        sesion.exigirPermiso(Permiso.GESTIONAR_PRODUCTOS);
        Producto p = new Producto(0, nombre, precio, costo, stock, minimo);
        productos.guardar(p);
        return p;
    }

    // Editar el catalogo no modifica stock: las compras y ventas ya gestionan ese movimiento.
    public Producto editarProducto(int id, String nombre, BigDecimal precio, BigDecimal costo, int minimo) {
        sesion.exigirPermiso(Permiso.GESTIONAR_PRODUCTOS);
        Producto p = exigirProducto(id);
        Producto editado = new Producto(id, nombre, precio, costo, p.getStock(), minimo);
        editado.setActivo(p.isActivo());
        productos.actualizar(editado);
        return editado;
    }

    // La baja logica conserva los detalles historicos que hacen referencia al producto.
    public void desactivarProducto(int id) {
        sesion.exigirPermiso(Permiso.GESTIONAR_PRODUCTOS);
        Producto p = exigirProducto(id);
        p.setActivo(false);
        productos.actualizar(p);
    }

    // El buscador de ventas solo ofrece productos activos.
    public List<Producto> buscarProductos(String filtro) {
        usuario();
        if (filtro == null || filtro.isBlank()) return productos.listarActivos();
        List<Producto> activos = new ArrayList<>();
        for (Producto p : productos.buscarPorNombre(filtro)) if (p.isActivo()) activos.add(p);
        return activos;
    }

    public List<Producto> listarProductosActivos() {
        usuario();
        return productos.listarActivos();
    }

    // La administracion debe poder ver tambien los productos desactivados.
    public List<Producto> listarProductos() {
        sesion.exigirPermiso(Permiso.GESTIONAR_PRODUCTOS);
        return productos.listarTodos();
    }

    public List<Producto> listarAlertasStock() {
        usuario();
        return productos.listarStockBajo();
    }

    // Una busqueda vacia se transforma en un mensaje de negocio.
    private Producto exigirProducto(int id) {
        Producto p = productos.buscarPorId(id);
        if (p == null) throw new DatosInvalidosException("Producto inexistente");
        return p;
    }
}
