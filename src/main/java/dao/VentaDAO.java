package dao;

import modelo.*;
import excepciones.AccesoDatosException;

import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Guarda una venta junto con detalles y descuento atomico del inventario.
 */
public class VentaDAO extends DAOBase<Venta> {
    // La misma conexion debe utilizarse para todas las operaciones de una transaccion.
    public VentaDAO() {
        super();
    }

    public VentaDAO(Connection c) {
        super(c);
    }

    // Reconstruye el precio historico de cada linea en un producto separado del catalogo.
    @Override
    protected Venta mapear(ResultSet r) throws SQLException {
        Usuario usuario = new UsuarioDAO(conexion()).buscarPorId(r.getInt("cajero_id"));
        Venta v = new Venta(usuario);
        v.setId(r.getInt("id"));
        v.setFechaHora(r.getTimestamp("fecha_hora").toLocalDateTime());
        try (PreparedStatement ps = conexion().prepareStatement("SELECT * FROM detalle_venta WHERE venta_id=? ORDER BY id")) {
            ps.setInt(1, v.getId());
            try (ResultSet d = ps.executeQuery()) {
                while (d.next()) {
                    Producto p = new ProductoDAO(conexion()).buscarPorId(d.getInt("producto_id"));
                    if (p == null) throw new SQLException("Producto de la venta inexistente");
                    p.setPrecioVenta(d.getBigDecimal("precio_unitario"));
                    p.setCostoCompra(d.getBigDecimal("costo_unitario"));
                    v.agregarDetalle(p, d.getInt("cantidad"));
                    v.getDetalles().get(v.getDetalles().size() - 1).setId(d.getInt("id"));
                }
            }
        }
        // Un ajuste autorizado de total debe sobrevivir a la reconstruccion desde detalles.
        v.restaurarTotalRegistrado(r.getBigDecimal("total"));
        return v;
    }

    // No se descuentan existencias en memoria: la fuente de verdad es la base de datos.
    public void guardarConDetalles(Venta v) {
        Objects.requireNonNull(v);
        nuevo(v.getId());
        if (v.estaVacia()) throw new IllegalArgumentException("La venta no puede estar vacia");
        int id = transaccion(() -> {
            int generado = insertar("INSERT INTO ventas(cajero_id,fecha_hora,total) VALUES (?,?,?)",
                    v.getCajero().getId(), v.getFechaHora(), v.calcularTotal());
            for (DetalleVenta d : v.getDetalles()) {
                idValido(d.getProducto().getId());
                // La condicion se evalua dentro del UPDATE y evita vender stock negativo aun con concurrencia.
                int filas = ejecutar("UPDATE productos SET stock=stock-? WHERE id=? AND activo=TRUE AND stock>=?",
                        d.getCantidad(), d.getProducto().getId(), d.getCantidad());
                if (filas != 1) throw new AccesoDatosException("Stock insuficiente o producto inactivo");
                insertar("INSERT INTO detalle_venta(venta_id,producto_id,cantidad,precio_unitario,costo_unitario) VALUES (?,?,?,?,?)",
                        generado, d.getProducto().getId(), d.getCantidad(), d.getPrecioUnitario(), d.getCostoUnitario());
            }
            return generado;
        });
        // Se asigna el id solo despues de completar la operacion.
        v.setId(id);
    }

    // El intervalo incluye todo el dia sin perder ventas con fracciones de segundo.
    public List<Venta> listarPorFecha(LocalDate fecha) {
        Objects.requireNonNull(fecha);
        return consultar("SELECT * FROM ventas WHERE fecha_hora>=? AND fecha_hora<? ORDER BY fecha_hora,id",
                fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());
    }

    public Venta buscarPorId(int id) {
        idValido(id);
        return uno("SELECT * FROM ventas WHERE id=?", id);
    }

    // El servicio exige permiso, motivo e historial y agrupa la modificacion en una transaccion.
    public void actualizarTotal(int id, BigDecimal total) {
        idValido(id);
        if (total == null || total.signum() < 0) throw new IllegalArgumentException("Total no negativo obligatorio");
        exigirCambio(ejecutar("UPDATE ventas SET total=? WHERE id=?", total, id));
    }

    // Usa el total registrado para incluir ajustes de venta autorizados.
    public BigDecimal sumarIngresosPorFecha(LocalDate fecha) {
        Objects.requireNonNull(fecha);
        return sumar("SELECT COALESCE(SUM(total),0) FROM ventas WHERE fecha_hora>=? AND fecha_hora<?",
                fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());
    }
}
