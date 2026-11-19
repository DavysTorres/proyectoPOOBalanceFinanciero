package servicio;

import dao.*;
import modelo.*;
import excepciones.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/**
 * Comprueba dias cerrados y guarda una fotografia financiera y el conteo de caja.
 */
public class ServicioCierre extends ServicioBase {
    private final CierreDiarioDAO cierres;
    private final VentaDAO ventas;
    private final CompraDAO compras;
    private final GastoDAO gastos;
    private final ServicioBalance balance;

    // El DAO del cierre y los movimientos deben compartir la misma conexion.
    public ServicioCierre() {
        this(new CierreDiarioDAO(), new VentaDAO(), new CompraDAO(), new GastoDAO(), new ServicioBalance(), Clock.systemDefaultZone());
    }

    public ServicioCierre(CierreDiarioDAO cierres, VentaDAO ventas, CompraDAO compras, GastoDAO gastos, ServicioBalance balance, Clock reloj) {
        super(reloj);
        this.cierres = Objects.requireNonNull(cierres);
        this.ventas = Objects.requireNonNull(ventas);
        this.compras = Objects.requireNonNull(compras);
        this.gastos = Objects.requireNonNull(gastos);
        this.balance = Objects.requireNonNull(balance);
    }

    // Los demas servicios usan esta regla antes de cada movimiento.
    public boolean estaCerrado(LocalDate fecha) {
        return cierres.existeCierre(Objects.requireNonNull(fecha));
    }

    public void exigirDiaAbierto(LocalDate fecha) {
        if (estaCerrado(fecha)) throw new DiaCerradoException("La jornada ya fue cerrada");
    }

    // Convencion inicial: ventas, compras y gastos registrados se pagan en efectivo, sin fondo inicial.
    // Los costos mensuales configurados son devengos, no pagos de efectivo adicionales.
    private BigDecimal efectivoDelDia(LocalDate fecha) {
        BigDecimal efectivo = ventas.sumarIngresosPorFecha(fecha);
        for (Compra c : compras.listarPorFecha(fecha)) efectivo = efectivo.subtract(c.getTotal());
        for (Gasto g : gastos.listarPorFecha(fecha)) efectivo = efectivo.subtract(g.getMonto());
        return efectivo;
    }

    // Un saldo esperado negativo hace visible la necesidad de financiar egresos; no se oculta con max(0).
    public BigDecimal calcularEfectivoSistema(LocalDate fecha) {
        sesion.exigirPermiso(Permiso.CERRAR_CAJA);
        CierreDiario cierre = cierres.buscarPorFecha(Objects.requireNonNull(fecha));
        return cierre == null ? efectivoDelDia(fecha) : cierre.getEfectivoSistema();
    }

    // Se puede cerrar hoy o una fecha anterior, pero nunca una jornada futura.
    public CierreDiario cerrarDia(LocalDate fecha, BigDecimal efectivoContado) {
        sesion.exigirPermiso(Permiso.CERRAR_CAJA);
        Objects.requireNonNull(fecha);
        if (fecha.isAfter(hoy())) throw new DatosInvalidosException("No puede cerrar una fecha futura");
        if (efectivoContado == null || efectivoContado.signum() < 0)
            throw new DatosInvalidosException("Conteo no negativo obligatorio");
        return cierres.transaccion(() -> {
            exigirDiaAbierto(fecha);
            BalanceDia b = balance.calcularBalance(fecha);
            CierreDiario cierre = new CierreDiario(0, fecha, usuario(), b.getIngresos(), b.getCostosVariables(),
                    b.getCostosFijosDia(), b.getResultado(), b.getPuntoEquilibrio(), efectivoDelDia(fecha), efectivoContado);
            cierres.guardar(cierre);
            return cierre;
        });
    }
}
