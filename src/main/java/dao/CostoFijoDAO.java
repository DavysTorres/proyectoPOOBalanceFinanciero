package dao;

import modelo.CostoFijo;

import java.sql.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Catalogo de costos mensuales; el servicio decide como repartirlos entre dias.
 */
public class CostoFijoDAO extends DAOBase<CostoFijo> implements CrudDAO<CostoFijo> {
    // Admite la conexion normal o una conexion aislada para pruebas.
    public CostoFijoDAO() {
        super();
    }

    public CostoFijoDAO(Connection c) {
        super(c);
    }

    // Reconstruye el costo conservando su estado de baja logica.
    @Override
    protected CostoFijo mapear(ResultSet r) throws SQLException {
        return new CostoFijo(r.getInt("id"), r.getString("concepto"), r.getBigDecimal("monto_mensual"), r.getBoolean("activo"));
    }

    // Insercion y actualizacion parametrizadas del catalogo.
    @Override
    public void guardar(CostoFijo c) {
        nuevo(c.getId());
        c.setId(insertar("INSERT INTO costos_fijos(concepto,monto_mensual,activo) VALUES (?,?,?)",
                c.getConcepto(), c.getMontoMensual(), c.isActivo()));
    }

    @Override
    public void actualizar(CostoFijo c) {
        idValido(c.getId());
        exigirCambio(ejecutar("UPDATE costos_fijos SET concepto=?,monto_mensual=?,activo=? WHERE id=?",
                c.getConcepto(), c.getMontoMensual(), c.isActivo(), c.getId()));
    }

    // Los listados activos excluyen costos desactivados del balance futuro.
    @Override
    public CostoFijo buscarPorId(int id) {
        idValido(id);
        return uno("SELECT * FROM costos_fijos WHERE id=?", id);
    }

    @Override
    public List<CostoFijo> listarTodos() {
        return consultar("SELECT * FROM costos_fijos ORDER BY id");
    }

    public List<CostoFijo> listarActivos() {
        return consultar("SELECT * FROM costos_fijos WHERE activo=TRUE ORDER BY id");
    }

    public BigDecimal sumarMontoMensualActivos() {
        return sumar("SELECT COALESCE(SUM(monto_mensual),0) FROM costos_fijos WHERE activo=TRUE");
    }
}
