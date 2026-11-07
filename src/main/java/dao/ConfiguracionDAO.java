package dao;

import modelo.Negocio;
import excepciones.DatosInvalidosException;

import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Configuracion clave-valor; la validacion de horarios y umbrales reside en Negocio.
 */
public class ConfiguracionDAO extends DAOBase<String> {
    // Los dos constructores siguen la misma convencion que los demas DAO.
    public ConfiguracionDAO() {
        super();
    }

    public ConfiguracionDAO(Connection c) {
        super(c);
    }

    // Cada fila aporta el texto correspondiente a una clave.
    @Override
    protected String mapear(ResultSet r) throws SQLException {
        return r.getString("valor");
    }

    // Devuelve null si la clave aun no fue configurada.
    public String obtener(String clave) {
        return uno("SELECT valor FROM configuracion WHERE clave=?", texto(clave));
    }

    // INSERT ... ON DUPLICATE KEY permite crear o reemplazar una clave en una sola sentencia.
    public void guardar(String clave, String valor) {
        ejecutar("INSERT INTO configuracion(clave,valor) VALUES (?,?) ON DUPLICATE KEY UPDATE valor=?",
                texto(clave), texto(valor), texto(valor));
    }

    // No inventa horarios: la configuracion obligatoria debe existir antes de operar.
    private String requerido(String clave) {
        String valor = obtener(clave);
        if (valor == null || valor.isBlank())
            throw new DatosInvalidosException("Falta configurar el negocio (" + clave
                    + "). Un administrador debe abrir Configuracion, completar los datos y pulsar Guardar.");
        return valor;
    }

    // La construccion del modelo vuelve a validar los datos recuperados.
    public Negocio cargarNegocio() {
        String amarillo = obtener("umbral_amarillo"), margen = obtener("umbral_margen_bajo");
        return new Negocio(requerido("nombre"), Integer.parseInt(requerido("dias_operacion")),
                LocalTime.parse(requerido("hora_apertura")), LocalTime.parse(requerido("hora_cierre")),
                new BigDecimal(amarillo == null ? "70" : amarillo), new BigDecimal(margen == null ? "15" : margen));
    }

    // Todas las claves se guardan juntas; un error no deja medio horario actualizado.
    public void guardarNegocio(Negocio n) {
        transaccion(() -> {
            guardar("nombre", n.getNombre());
            guardar("dias_operacion", Integer.toString(n.getDiasOperacion()));
            guardar("hora_apertura", n.getHoraApertura().toString());
            guardar("hora_cierre", n.getHoraCierre().toString());
            guardar("umbral_amarillo", n.getUmbralAmarillo().toPlainString());
            guardar("umbral_margen_bajo", n.getUmbralMargenBajo().toPlainString());
            return null;
        });
    }
}
