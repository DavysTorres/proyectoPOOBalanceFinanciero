package dao;

import conexion.conexionDB;
import java.sql.*;
import java.util.*;

/** Inspeccion de solo lectura del esquema configurado. No registra ventas ni modifica stock. */
public final class RevisionEsquemaVenta {
    public static void main(String[] args) throws Exception {
        try {
            Connection c = conexionDB.getInstance().getConnection();
            String columnaCajero = new VentaDAO(c).columnaCajero();
            Map<String, List<String>> esperadas = Map.of(
                    "ventas", List.of(columnaCajero, "fecha_hora", "total"),
                    "detalle_venta", List.of("venta_id", "producto_id", "cantidad", "precio_unitario", "costo_unitario"));
            List<String> problemas = new ArrayList<>();
            for (var tabla : esperadas.entrySet()) {
                Set<String> presentes = new HashSet<>();
                try (ResultSet r = c.getMetaData().getColumns(c.getCatalog(), null, tabla.getKey(), null)) {
                    while (r.next()) {
                        String nombre = r.getString("COLUMN_NAME"); presentes.add(nombre);
                        boolean requerida = r.getInt("NULLABLE") == DatabaseMetaData.columnNoNulls
                                && r.getString("COLUMN_DEF") == null && !"YES".equals(r.getString("IS_AUTOINCREMENT"))
                                && !"YES".equals(r.getString("IS_GENERATEDCOLUMN"));
                        if (requerida && !tabla.getValue().contains(nombre)) problemas.add(tabla.getKey() + "." + nombre + " obligatoria sin valor ni default");
                    }
                }
                for (String columna : tabla.getValue()) if (!presentes.contains(columna)) problemas.add(tabla.getKey() + "." + columna + " inexistente");
            }
            System.out.println("Compatibilidad del esquema de ventas: " + (problemas.isEmpty() ? "OK" : problemas));
            for (String sql : List.of("EXPLAIN INSERT INTO ventas(" + columnaCajero + ",fecha_hora,total) VALUES(0,NOW(),1)",
                    "EXPLAIN INSERT INTO detalle_venta(venta_id,producto_id,cantidad,precio_unitario,costo_unitario) VALUES(0,0,1,1,1)")) {
                try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
                    System.out.println("Sentencia de venta validada sin ejecutarla.");
                } catch (SQLException error) {
                    System.out.println("Validacion SQL: " + error.getSQLState() + " / " + error.getErrorCode() + " / " + error.getMessage());
                }
            }
        } finally { conexionDB.getInstance().desconectar(); }
    }
}
