package util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Comprobaciones basicas reutilizables, sin sustituir las reglas del modelo.
 */
public final class Validador {
    private Validador() {
    }

    public static boolean esTextoVacio(String texto) {
        return texto == null || texto.isBlank();
    }

    public static boolean esNumeroPositivo(BigDecimal numero) {
        return numero != null && numero.signum() > 0;
    }

    public static boolean esFechaValida(String texto) {
        if (esTextoVacio(texto)) return false;
        try {
            LocalDate.parse(texto.trim());
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
