package util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Lee el archivo .env con Java estandar; el entorno del sistema tiene prioridad.
 */
public final class ConfiguracionEntorno {
    private final Map<String, String> valores = new HashMap<>();

    // El archivo es opcional cuando las variables ya estan configuradas en el sistema.
    public ConfiguracionEntorno() {
        this(Path.of(".env"));
    }

    public ConfiguracionEntorno(Path archivo) {
        if (!Files.exists(archivo)) return;
        try {
            for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
                String texto = linea.replace("\uFEFF", "").trim();
                if (texto.isEmpty() || texto.startsWith("#")) continue;
                if (texto.startsWith("export ")) texto = texto.substring(7).trim();
                int separador = texto.indexOf('=');
                if (separador < 1) throw new IllegalArgumentException("Revise el formato de las variables en .env");
                String clave = texto.substring(0, separador).trim();
                String valor = texto.substring(separador + 1).trim();
                if (valor.length() >= 2 && ((valor.startsWith("\"") && valor.endsWith("\""))
                        || (valor.startsWith("'") && valor.endsWith("'"))))
                    valor = valor.substring(1, valor.length() - 1);
                valores.put(clave, valor);
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer la configuracion local", e);
        }
    }

    // No imprime datos privados ni necesita una libreria para cargar tres variables.
    public String get(String clave) {
        String sistema = System.getenv(clave);
        return sistema != null ? sistema : valores.get(clave);
    }
}
