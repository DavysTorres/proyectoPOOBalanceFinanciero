package controlador;

import excepciones.*;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Comparte ejecucion, conversion de campos y mensajes sin duplicar reglas de negocio.
 */
public abstract class ControladorBase<V extends VistaBase> {
    protected final V vista;
    private final EjecutorTareas tareas;
    private boolean ocupada;
    private boolean vinculada = true;
    private Runnable alCambiarDatos = () -> {
    };

    // La aplicacion puede refrescar el semaforo cuando una escritura termino correctamente.
    public final void alCambiarDatos(Runnable accion) {
        alCambiarDatos = Objects.requireNonNull(accion);
    }

    protected final void informarCambio() {
        alCambiarDatos.run();
    }

    // Una vista y un ejecutor se inyectan; no se buscan DAO ni conexiones aqui.
    protected ControladorBase(V vista, EjecutorTareas tareas) {
        this.vista = Objects.requireNonNull(vista);
        this.tareas = Objects.requireNonNull(tareas);
    }

    // Lee y convierte campos en el hilo de la vista, nunca dentro del trabajo JDBC.
    protected final void preparar(Runnable lectura) {
        if (!vinculada || ocupada) return;
        try {
            lectura.run();
        } catch (RuntimeException e) {
            mostrarError(e);
        }
    }

    // Serializa el trabajo usando el ejecutor compartido y protege contra un segundo clic.
    protected final <T> void ejecutar(EjecutorTareas.Operacion<T> operacion, Consumer<T> exito) {
        if (!vinculada || ocupada) return;
        ocupada = true;
        vista.setOcupada(true);
        try {
            tareas.ejecutar(operacion, resultado -> {
                try {
                    if (vinculada) exito.accept(resultado);
                } catch (RuntimeException e) {
                    if (vinculada) mostrarError(e);
                } finally {
                    finalizar();
                }
            }, error -> {
                try {
                    if (vinculada) mostrarError(error);
                } finally {
                    finalizar();
                }
            });
        } catch (RuntimeException e) {
            try {
                mostrarError(e);
            } finally {
                finalizar();
            }
        }
    }

    // El cierre de una ventana no cancela una escritura SQL a medio ejecutar.
    public final void desvincular() {
        vinculada = false;
    }

    // Las respuestas tardias no intentan modificar una ventana ya destruida.
    private void finalizar() {
        ocupada = false;
        if (vinculada) vista.setOcupada(false);
    }

    // Los errores JDBC y tecnicos no muestran sentencias, rutas ni credenciales al usuario.
    protected final void mostrarError(Exception e) {
        if (e instanceof AccesoDatosException)
            vista.mostrarError("No fue posible acceder a los datos. Revise la conexion.");
        else if (e instanceof PermisoDenegadoException || e instanceof CredencialesInvalidasException
                || e instanceof UsuarioBloqueadoException || e instanceof DiaCerradoException
                || e instanceof DatosInvalidosException || e instanceof StockInsuficienteException
                || e instanceof IllegalArgumentException)
            vista.mostrarError(e.getMessage() == null ? "Revise los datos ingresados." : e.getMessage());
        else vista.mostrarError("No se pudo completar la operacion.");
    }

    // La seleccion es una validacion de presentacion; los servicios vuelven a validar la entidad.
    protected static <T> T seleccionado(T valor) {
        if (valor == null) throw new IllegalArgumentException("Seleccione un registro.");
        return valor;
    }

    protected static int id(Integer valor) {
        if (valor == null || valor <= 0) throw new IllegalArgumentException("Seleccione un registro valido.");
        return valor;
    }

    // Se acepta coma o punto decimal, sin separadores de miles ambiguos.
    protected static BigDecimal decimal(String texto) {
        if (texto == null || !texto.trim().matches("[+-]?[0-9]+([.,][0-9]+)?"))
            throw new IllegalArgumentException("Ingrese un importe valido sin separadores de miles.");
        return new BigDecimal(texto.trim().replace(',', '.'));
    }

    protected static int entero(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NullPointerException | NumberFormatException e) {
            throw new IllegalArgumentException("Ingrese un numero entero valido.");
        }
    }

    protected static LocalDate fecha(String texto) {
        try {
            return LocalDate.parse(texto);
        } catch (NullPointerException | DateTimeParseException e) {
            throw new IllegalArgumentException("Ingrese la fecha con formato AAAA-MM-DD.");
        }
    }

    protected static LocalTime hora(String texto) {
        try {
            return LocalTime.parse(texto);
        } catch (NullPointerException | DateTimeParseException e) {
            throw new IllegalArgumentException("Ingrese una hora valida, por ejemplo 08:30.");
        }
    }
}
