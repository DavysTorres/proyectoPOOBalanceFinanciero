package controlador;

import java.util.concurrent.*;
import java.util.function.Consumer;
import javax.swing.SwingUtilities;

/**
 * Una cola de trabajo compartida: no bloquea Swing ni usa la conexion JDBC simultaneamente.
 */
public final class EjecutorSwing implements EjecutorTareas, AutoCloseable {
    private final ExecutorService cola = Executors.newSingleThreadExecutor();

    // El trabajo va fuera del EDT; las respuestas vuelven al EDT para actualizar componentes.
    @Override
    public <T> void ejecutar(Operacion<T> operacion, Consumer<T> exito, Consumer<Exception> error) {
        cola.execute(() -> {
            try {
                T resultado = operacion.ejecutar();
                SwingUtilities.invokeLater(() -> exito.accept(resultado));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> error.accept(e));
            }
        });
    }

    // Deja terminar operaciones ya aceptadas y rechaza nuevas tareas al cerrar la aplicacion.
    @Override
    public void close() {
        cola.shutdown();
    }
}
