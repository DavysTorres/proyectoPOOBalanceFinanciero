package dao;

import modelo.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Persiste la compra y actualiza existencias y costo en la misma transaccion.
 */
public class CompraDAO extends DAOBase<Compra> {
    // La conexion inyectada permite incluir esta operacion en una transaccion del servicio.
    public CompraDAO() {
        super();
    }

    public CompraDAO(Connection c) {
        super(c);
    }

    // Reconstruye cabecera y detalles con el costo realmente pagado, no el costo actual.
    @Override
    protected Compra mapear(ResultSet r) throws SQLException {
        Compra c = new Compra(new UsuarioDAO(conexion()).buscarPorId(r.getInt("usuario_id")), r.getString("proveedor"));
        c.setId(r.getInt("id"));
        c.setFechaHora(r.getTimestamp("fecha_hora").toLocalDateTime());
        try (PreparedStatement ps = conexion().prepareStatement("SELECT * FROM detalle_compra WHERE compra_id=? ORDER BY id")) {
            ps.setInt(1, c.getId());
            try (ResultSet d = ps.executeQuery()) {
                while (d.next()) {
                    Producto p = new ProductoDAO(conexion()).buscarPorId(d.getInt("producto_id"));
                    if (p == null) throw new SQLException("Producto de compra inexistente");
                    c.agregarDetalle(p, d.getInt("cantidad"), d.getBigDecimal("costo_unitario"));
                    c.getDetalles().get(c.getDetalles().size() - 1).setId(d.getInt("id"));
                }
            }
        }
        return c;
    }

    // Cada linea aumenta stock una sola vez; las instancias en memoria se recargan despues.
    public void guardarConDetalles(Compra c) {
        Objects.requireNonNull(c);
        nuevo(c.getId());
        if (c.getDetalles().isEmpty()) throw new IllegalArgumentException("La compra no puede estar vacia");
        int id = transaccion(() -> {
            int generado = insertar("INSERT INTO compras(usuario_id,proveedor,fecha_hora,total) VALUES (?,?,?,?)",
                    c.getUsuario().getId(), c.getProveedor(), c.getFechaHora(), c.calcularTotal());
            for (DetalleCompra d : c.getDetalles()) {
                idValido(d.getProducto().getId());
                // Se comprueba el limite int para no desbordar las existencias.
                exigirCambio(ejecutar("UPDATE productos SET stock=stock+?,costo_compra=? WHERE id=? AND stock<=?",
                        d.getCantidad(), d.getCostoUnitario(), d.getProducto().getId(), Integer.MAX_VALUE - d.getCantidad()));
                insertar("INSERT INTO detalle_compra(compra_id,producto_id,cantidad,costo_unitario) VALUES (?,?,?,?)",
                        generado, d.getProducto().getId(), d.getCantidad(), d.getCostoUnitario());
            }
            return generado;
        });
        c.setId(id);
    }

    // Consulta de un dia completo; buscarPorId permite verificar la escritura desde consola.
    public List<Compra> listarPorFecha(LocalDate fecha) {
        Objects.requireNonNull(fecha);
        return consultar("SELECT * FROM compras WHERE fecha_hora>=? AND fecha_hora<? ORDER BY fecha_hora,id",
                fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());
    }

    public Compra buscarPorId(int id) {
        idValido(id);
        return uno("SELECT * FROM compras WHERE id=?", id);
    }
}
