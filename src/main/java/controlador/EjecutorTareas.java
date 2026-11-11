package controlador;

import java.util.function.Consumer;

/**
 * Abstraccion del trabajo para usar una cola Swing o un ejecutor inmediato en pruebas.
 */
public interface EjecutorTareas {
    @FunctionalInterface
    interface Operacion<T> {
        T ejecutar() throws Exception;
    }

    // Exito y error deben entregarse en el hilo de la interfaz cuando se utiliza Swing.
    <T> void ejecutar(Operacion<T> operacion, Consumer<T> exito, Consumer<Exception> error);
}
