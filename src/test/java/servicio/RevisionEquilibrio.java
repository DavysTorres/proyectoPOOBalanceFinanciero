package servicio;

import dao.*;
import modelo.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

/** Casos de balance aislados: no abre conexiones ni modifica datos reales. */
public final class RevisionEquilibrio {
    private static final Usuario USUARIO = new Administrador(1, "Admin", "admin", "hash", true, LocalDateTime.now());
    private static BigDecimal d(String n) { return new BigDecimal(n); }
    private static void exigir(boolean valor) { if (!valor) throw new AssertionError("Balance incorrecto"); }
    private static BalanceDia calcular(List<Venta> operaciones, BigDecimal gastos) {
        LocalDate fecha = LocalDate.of(2026, 11, 13);
        return new ServicioBalance(
            new VentaDAO() { @Override public List<Venta> listarPorFecha(LocalDate f) { return operaciones; } },
            new GastoDAO() {
                @Override public List<Gasto> listarPorFecha(LocalDate f) {
                    return gastos.signum() == 0 ? List.of() : List.of(new GastoVariable(0, USUARIO, "Transporte", gastos, f));
                }
                @Override public List<Gasto> listarFijosDelMes(LocalDate f) {
                    return List.of(new GastoFijo(0, USUARIO, "Licencia mensual", d("3000"), fecha.minusDays(5), 30));
                }
            },
            new CostoFijoDAO() { @Override public BigDecimal sumarMontoMensualActivos() { return d("3000000"); } },
            new ConfiguracionDAO() { @Override public Negocio cargarNegocio() { return new Negocio("Tienda", 30, LocalTime.of(8,0), LocalTime.of(18,0)); } },
            new ProductoDAO() { @Override public List<Producto> listarActivos() { throw new AssertionError("No usar catalogo para inventar mezcla de ventas"); } },
            new CierreDiarioDAO() { @Override public CierreDiario buscarPorFecha(LocalDate f) { return null; } },
            Clock.systemUTC()).calcularBalance(fecha);
    }
    public static void main(String[] args) {
        BalanceDia vacio = calcular(List.of(), BigDecimal.ZERO);
        exigir(!vacio.isPuntoEquilibrioCalculable());
        exigir(vacio.getCostosFijosDia().compareTo(d("100100")) == 0);
        exigir(vacio.getResultado().compareTo(d("-100100")) == 0);
        Venta venta = new Venta(USUARIO);
        venta.agregarDetalle(new Producto(1, "Cafe", d("10000"), d("6000"), 100, 1), 15);
        BalanceDia balance = calcular(List.of(venta), BigDecimal.ZERO);
        exigir(balance.isPuntoEquilibrioEstimado());
        exigir(balance.getPuntoEquilibrio().compareTo(d("250250")) == 0);
        exigir(balance.getFaltante().compareTo(d("100250")) == 0);
        exigir(balance.getResultado().compareTo(d("-40100")) == 0);
        BalanceDia perdida = calcular(List.of(venta), d("70000"));
        exigir(!perdida.isPuntoEquilibrioCalculable());
        exigir(new Diagnostico().evaluar(perdida, new Negocio("T", 30, LocalTime.of(8,0), LocalTime.of(18,0))) instanceof EstadoRojo);
        System.out.println("OK: sin ventas no hay meta, margen ponderado real, gastos mensuales y margen negativo");
    }
}
