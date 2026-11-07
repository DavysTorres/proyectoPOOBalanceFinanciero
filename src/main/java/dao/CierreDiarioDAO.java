package dao;

import modelo.CierreDiario;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * El cierre es un registro historico y no ofrece actualizar ni eliminar.
 */
public class CierreDiarioDAO extends DAOBase<CierreDiario> {
    // Permite compartir conexion con las operaciones previas al cierre.
    public CierreDiarioDAO() {
        super();
    }

    public CierreDiarioDAO(Connection c) {
        super(c);
    }

    // La diferencia se recalcula desde los dos importes del conteo.
    @Override
    protected CierreDiario mapear(ResultSet r) throws SQLException {
        return new CierreDiario(r.getInt("id"), r.getDate("fecha").toLocalDate(),
                new UsuarioDAO(conexion()).buscarPorId(r.getInt("usuario_id")),
                r.getBigDecimal("ingresos"), r.getBigDecimal("costos_variables"),
                r.getBigDecimal("costos_fijos"), r.getBigDecimal("resultado"),
                r.getBigDecimal("punto_equilibrio"), r.getBigDecimal("efectivo_sistema"), r.getBigDecimal("efectivo_contado"));
    }

    // La restriccion UNIQUE(fecha) evita dos cierres aun ante intentos concurrentes.
    public void guardar(CierreDiario c) {
        nuevo(c.getId());
        c.setId(insertar("INSERT INTO cierres_diarios(fecha,usuario_id,ingresos,costos_variables,costos_fijos,resultado,punto_equilibrio,efectivo_sistema,efectivo_contado,diferencia) VALUES (?,?,?,?,?,?,?,?,?,?)",
                c.getFecha(), c.getUsuario().getId(), c.getIngresos(), c.getCostosVariables(), c.getCostosFijos(),
                c.getResultado(), c.getPuntoEquilibrio(), c.getEfectivoSistema(), c.getEfectivoContado(), c.calcularDiferenciaCaja()));
    }

    // La fecha es unica; null significa que el dia todavia no tiene cierre.
    public CierreDiario buscarPorFecha(LocalDate fecha) {
        return uno("SELECT * FROM cierres_diarios WHERE fecha=?", Objects.requireNonNull(fecha));
    }

    public boolean existeCierre(LocalDate fecha) {
        return buscarPorFecha(fecha) != null;
    }

    // El rango incluye los dos extremos y rechaza fechas invertidas.
    public List<CierreDiario> listarPorRango(LocalDate desde, LocalDate hasta) {
        Objects.requireNonNull(desde);
        Objects.requireNonNull(hasta);
        if (hasta.isBefore(desde)) throw new IllegalArgumentException("Rango de fechas invertido");
        return consultar("SELECT * FROM cierres_diarios WHERE fecha BETWEEN ? AND ? ORDER BY fecha", desde, hasta);
    }
}
