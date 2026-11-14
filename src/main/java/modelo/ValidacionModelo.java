package modelo;

import java.math.BigDecimal;

/**
 * Reglas compartidas, visibles solo dentro del paquete y basadas en Java estandar.
 */
final class ValidacionModelo {
    // No hay estado ni instancias de este auxiliar.
    private ValidacionModelo() {
    }

    // Cero representa entidades nuevas y los valores negativos se rechazan.
    static int id(int valor) {
        if (valor < 0) throw new IllegalArgumentException("El id no puede ser negativo");
        return valor;
    }

    // Rechaza texto ausente y elimina espacios exteriores.
    static String texto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty())
            throw new IllegalArgumentException(campo + " es obligatorio");
        return valor.trim();
    }

    // Evita referencias nulas en los atributos requeridos.
    static <T> T obligatorio(T valor, String campo) {
        if (valor == null) throw new IllegalArgumentException(campo + " es obligatorio");
        return valor;
    }

    // Precios y gastos deben ser estrictamente positivos.
    static BigDecimal positivo(BigDecimal valor, String campo) {
        obligatorio(valor, campo);
        if (valor.signum() <= 0) throw new IllegalArgumentException(campo + " debe ser mayor que cero");
        return valor;
    }

    // Acumulados como ingresos pueden ser cero, pero no negativos.
    static BigDecimal noNegativo(BigDecimal valor, String campo) {
        obligatorio(valor, campo);
        if (valor.signum() < 0) throw new IllegalArgumentException(campo + " no puede ser negativo");
        return valor;
    }

    // La guia exige de uno a treinta y un dias de operacion.
    static int dias(int dias) {
        if (dias < 1 || dias > 31) throw new IllegalArgumentException("Los dias deben estar entre 1 y 31");
        return dias;
    }

    // Los umbrales se expresan de cero a cien, no como fracciones.
    static BigDecimal porcentaje(BigDecimal valor, String campo) {
        noNegativo(valor, campo);
        if (valor.compareTo(new BigDecimal("100")) > 0)
            throw new IllegalArgumentException(campo + " no puede superar 100");
        return valor;
    }
}
