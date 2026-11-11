package util;

import java.time.*;

/**
 * Fechas y fraccion del horario; no guarda estado ni consulta la base de datos.
 */
public final class FechaUtil {
    private FechaUtil() {
    }

    public static LocalDate hoy() {
        return LocalDate.now();
    }

    public static double fraccionDelDia(LocalTime ahora, LocalTime apertura, LocalTime cierre) {
        if (!cierre.isAfter(apertura)) throw new IllegalArgumentException("Horario invalido");
        long total = Duration.between(apertura, cierre).toNanos();
        long transcurrido = Duration.between(apertura, ahora).toNanos();
        return Math.max(0, Math.min(1, (double) transcurrido / total));
    }
}
