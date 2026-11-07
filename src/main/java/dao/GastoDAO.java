package dao;

import modelo.*;

import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Una tabla conserva ambos tipos; la columna tipo permite recuperar el polimorfismo.
 */
public class GastoDAO extends DAOBase<Gasto> {
    // Constructor normal y variante para pruebas o transacciones compartidas.
    public GastoDAO() {
        super();
    }

    public GastoDAO(Connection c) {
        super(c);
    }

    // Factory de gastos: el modelo aplica la formula diaria apropiada.
    @Override
    protected Gasto mapear(ResultSet r) throws SQLException {
        Usuario u = new UsuarioDAO(conexion()).buscarPorId(r.getInt("usuario_id"));
        if ("FIJO".equals(r.getString("tipo")))
            return new GastoFijo(r.getInt("id"), u, r.getString("concepto"), r.getBigDecimal("monto"),
                    r.getDate("fecha").toLocalDate(), r.getInt("dias_operacion"));
        if ("VARIABLE".equals(r.getString("tipo")))
            return new GastoVariable(r.getInt("id"), u, r.getString("concepto"), r.getBigDecimal("monto"), r.getDate("fecha").toLocalDate());
        throw new SQLException("Tipo de gasto desconocido");
    }

    // Guarda el tipo concreto, sin introducir calculos en las ventanas.
    public void guardar(Gasto g) {
        nuevo(g.getId());
        if (!(g instanceof GastoFijo) && !(g instanceof GastoVariable))
            throw new IllegalArgumentException("Tipo de gasto no soportado");
        boolean fijo = g instanceof GastoFijo;
        int id = insertar("INSERT INTO gastos(usuario_id,concepto,monto,fecha,tipo,dias_operacion) VALUES (?,?,?,?,?,?)",
                g.getUsuario().getId(), g.getConcepto(), g.getMonto(), g.getFecha(),
                fijo ? "FIJO" : "VARIABLE", fijo ? ((GastoFijo) g).getDiasOperacion() : null);
        g.setId(id);
    }

    // Consultas para balance y verificacion de persistencia.
    public List<Gasto> listarPorFecha(LocalDate fecha) {
        return consultar("SELECT * FROM gastos WHERE fecha=? ORDER BY id", Objects.requireNonNull(fecha));
    }

    public Gasto buscarPorId(int id) {
        idValido(id);
        return uno("SELECT * FROM gastos WHERE id=?", id);
    }

    // Suma montos registrados, no prorrateos; el balance solicita solo VARIABLE para costos variables.
    public BigDecimal sumarPorTipoYFecha(String tipo, LocalDate fecha) {
        String t = texto(tipo).toUpperCase(java.util.Locale.ROOT);
        if (!t.equals("FIJO") && !t.equals("VARIABLE")) throw new IllegalArgumentException("Tipo de gasto no valido");
        return sumar("SELECT COALESCE(SUM(monto),0) FROM gastos WHERE tipo=? AND fecha=?", t, Objects.requireNonNull(fecha));
    }
}
