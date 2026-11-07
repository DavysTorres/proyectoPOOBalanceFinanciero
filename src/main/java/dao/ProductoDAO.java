package dao;

import modelo.Producto;

import java.sql.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * CRUD de productos: los borrados se representan mediante activo=false.
 */
public class ProductoDAO extends DAOBase<Producto> implements CrudDAO<Producto> {
    // Construccion normal o con conexion inyectada para coordinar transacciones.
    public ProductoDAO() {
        super();
    }

    public ProductoDAO(Connection c) {
        super(c);
    }

    // Convierte columnas SQL en un objeto que aplica las validaciones del modelo.
    @Override
    protected Producto mapear(ResultSet r) throws SQLException {
        Producto p = new Producto(r.getInt("id"), r.getString("nombre"), r.getBigDecimal("precio_venta"),
                r.getBigDecimal("costo_compra"), r.getInt("stock"), r.getInt("stock_minimo"));
        p.setActivo(r.getBoolean("activo"));
        return p;
    }

    // Inserta una entidad nueva y devuelve su identificador mediante el mismo objeto.
    @Override
    public void guardar(Producto p) {
        nuevo(p.getId());
        p.setId(insertar("INSERT INTO productos(nombre,precio_venta,costo_compra,stock,stock_minimo,activo) VALUES (?,?,?,?,?,?)",
                texto(p.getNombre()), p.getPrecioVenta(), p.getCostoCompra(), p.getStock(), p.getStockMinimo(), p.isActivo()));
    }

    // Actualiza catalogo y estado; el stock se modifica con una operacion especifica para evitar sobrescribir ventas concurrentes.
    @Override
    public void actualizar(Producto p) {
        idValido(p.getId());
        exigirCambio(ejecutar("UPDATE productos SET nombre=?,precio_venta=?,costo_compra=?,stock_minimo=?,activo=? WHERE id=?",
                p.getNombre(), p.getPrecioVenta(), p.getCostoCompra(), p.getStockMinimo(), p.isActivo(), p.getId()));
    }

    // Consultas del catalogo, busqueda parcial y alertas de existencias bajas.
    @Override
    public Producto buscarPorId(int id) {
        idValido(id);
        return uno("SELECT * FROM productos WHERE id=?", id);
    }

    @Override
    public List<Producto> listarTodos() {
        return consultar("SELECT * FROM productos ORDER BY id");
    }

    public List<Producto> buscarPorNombre(String nombre) {
        return consultar("SELECT * FROM productos WHERE nombre LIKE ? ORDER BY nombre,id", "%" + texto(nombre) + "%");
    }

    public List<Producto> listarActivos() {
        return consultar("SELECT * FROM productos WHERE activo=TRUE ORDER BY nombre,id");
    }

    public List<Producto> listarStockBajo() {
        return consultar("SELECT * FROM productos WHERE activo=TRUE AND stock<=stock_minimo ORDER BY nombre,id");
    }

    // Ajuste absoluto para inventarios manuales, autorizado posteriormente por el servicio.
    public void actualizarStock(int id, int nuevoStock) {
        idValido(id);
        if (nuevoStock < 0) throw new IllegalArgumentException("El stock no puede ser negativo");
        exigirCambio(ejecutar("UPDATE productos SET stock=? WHERE id=?", nuevoStock, id));
    }

    // Cambia el costo actual; los detalles de ventas conservan su copia historica.
    public void actualizarCosto(int id, BigDecimal costo) {
        idValido(id);
        if (costo == null || costo.signum() <= 0) throw new IllegalArgumentException("Costo positivo obligatorio");
        exigirCambio(ejecutar("UPDATE productos SET costo_compra=? WHERE id=?", costo, id));
    }
}
