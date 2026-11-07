package dao;

import modelo.RegistroCambio;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Historial de solo insercion y consulta: no expone modificacion ni borrado.
 */
public class HistorialCambiosDAO extends DAOBase<RegistroCambio> {
    // Reutiliza la transaccion de la modificacion de venta cuando se inyecta la conexion.
    public HistorialCambiosDAO() {
        super();
    }

    public HistorialCambiosDAO(Connection c) {
        super(c);
    }

    // Los valores anterior y nuevo se toman del historial, nunca del total actual de la venta.
    @Override
    protected RegistroCambio mapear(ResultSet r) throws SQLException {
        return new RegistroCambio(r.getInt("id"),
                new VentaDAO(conexion()).buscarPorId(r.getInt("venta_id")),
                new UsuarioDAO(conexion()).buscarPorId(r.getInt("usuario_id")),
                r.getTimestamp("fecha_hora").toLocalDateTime(),
                r.getBigDecimal("valor_anterior"), r.getBigDecimal("valor_nuevo"), r.getString("motivo"));
    }

    // Devuelve el id generado sin introducir un setter en el registro inmutable.
    public int guardar(RegistroCambio r) {
        nuevo(r.getId());
        return insertar("INSERT INTO historial_cambios(venta_id,usuario_id,fecha_hora,valor_anterior,valor_nuevo,motivo) VALUES (?,?,?,?,?,?)",
                r.getVenta().getId(), r.getUsuario().getId(), r.getFechaHora(), r.getValorAnterior(), r.getValorNuevo(), r.getMotivo());
    }

    // Los listados permiten filtrar sin alterar el historial.
    public List<RegistroCambio> listarPorVenta(int ventaId) {
        idValido(ventaId);
        return consultar("SELECT * FROM historial_cambios WHERE venta_id=? ORDER BY fecha_hora,id", ventaId);
    }

    public List<RegistroCambio> listarTodos() {
        return consultar("SELECT * FROM historial_cambios ORDER BY fecha_hora,id");
    }

    public List<RegistroCambio> listarPorFecha(LocalDate fecha) {
        Objects.requireNonNull(fecha);
        return consultar("SELECT * FROM historial_cambios WHERE fecha_hora>=? AND fecha_hora<? ORDER BY fecha_hora,id",
                fecha.atStartOfDay(), fecha.plusDays(1).atStartOfDay());
    }
}
