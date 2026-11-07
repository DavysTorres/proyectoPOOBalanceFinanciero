package servicio;

import excepciones.DatosInvalidosException;
import excepciones.PermisoDenegadoException;
import modelo.Usuario;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Comparte sesion y reloj, sin SQL ni una nueva conexion a la base de datos.
 */
abstract class ServicioBase {
    protected final Sesion sesion;
    protected final Clock reloj;

    // El reloj se recibe para probar distintos dias sin cambiar la hora del computador.
    protected ServicioBase(Clock reloj) {
        this.sesion = Sesion.getInstancia();
        this.reloj = Objects.requireNonNull(reloj);
    }

    // Centraliza la obtencion de la fecha usada por ventas, gastos y cierres.
    protected final LocalDate hoy() {
        return LocalDate.now(reloj);
    }

    // Solo hay un usuario autenticado por sesion de escritorio.
    protected final Usuario usuario() {
        if (!sesion.hayUsuario()) throw new PermisoDenegadoException("Debe iniciar sesion");
        return sesion.getUsuarioActual();
    }

    // Los filtros y motivos no pueden estar vacios.
    protected static String texto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty())
            throw new DatosInvalidosException(campo + " es obligatorio");
        return valor.trim();
    }
}
