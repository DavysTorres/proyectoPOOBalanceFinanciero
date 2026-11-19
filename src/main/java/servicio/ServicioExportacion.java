package servicio;

import modelo.*;
import excepciones.DatosInvalidosException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Locale;

/**
 * Exportacion CSV compatible con hojas de calculo, utilizando solo archivos del JDK.
 */
public class ServicioExportacion implements Exportable {
    // No disfraza un CSV como XLSX ni agrega librerias PDF al proyecto.
    @Override
    public void exportar(BalanceDia balance, String ruta) {
        exportarCSV(balance, ruta);
    }

    // Escribe importes decimales y conserva el significado de una meta no calculable.
    public void exportarCSV(BalanceDia b, String ruta) {
        Sesion.getInstancia().exigirPermiso(Permiso.VER_REPORTES);
        if (ruta == null || !ruta.toLowerCase(Locale.ROOT).endsWith(".csv"))
            throw new DatosInvalidosException("Seleccione un archivo con extension .csv");
        Path archivo = Path.of(ruta);
        // CREATE_NEW evita sobrescribir silenciosamente un informe existente.
        try (BufferedWriter w = Files.newBufferedWriter(archivo, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
            w.write('\uFEFF');
            w.write("fecha;ingresos;costos_variables;margen;costos_fijos;resultado;punto_equilibrio;faltante;avance_porcentaje;meta_estimada\n");
            w.write(b.getFecha() + ";" + numero(b.getIngresos()) + ";" + numero(b.getCostosVariables()) + ";"
                    + numero(b.getMargenContribucion()) + ";" + numero(b.getCostosFijosDia()) + ";" + numero(b.getResultado()) + ";");
            if (b.isPuntoEquilibrioCalculable())
                w.write(numero(b.getPuntoEquilibrio()) + ";" + numero(b.getFaltante()) + ";" + numero(b.getPorcentajeAvance()));
            else w.write("NO CALCULABLE;;");
            w.write(";" + b.isPuntoEquilibrioEstimado() + "\n");
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo exportar el informe", e);
        }
    }

    // Usa coma decimal y punto y coma de separacion para una hoja configurada en espanol.
    private String numero(java.math.BigDecimal valor) {
        return valor.toPlainString().replace('.', ',');
    }
}
