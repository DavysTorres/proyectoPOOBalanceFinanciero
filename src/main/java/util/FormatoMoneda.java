package util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * El formato es presentacion: los calculos siguen usando BigDecimal.
 */
public final class FormatoMoneda {
    private FormatoMoneda() {
    }

    public static String mostrar(BigDecimal valor) {
        if (valor == null) return "";
        NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));
        formato.setMaximumFractionDigits(2);
        return formato.format(valor);
    }
}
