package servicio;

import dao.*;
import modelo.*;

import java.math.*;
import java.time.*;
import java.util.*;

/**
 * Consulta datos y compone resultados sin colocar formulas en DAO o ventanas.
 */
public class ServicioBalance extends ServicioBase {
    private final VentaDAO ventas;
    private final GastoDAO gastos;
    private final CostoFijoDAO costos;
    private final ConfiguracionDAO configuracion;
    private final ProductoDAO productos;
    private final CierreDiarioDAO cierres;
    private final CalculadoraFinanciera calculadora = new CalculadoraFinanciera();

    // Los DAO normales comparten la conexion existente; no se crea otro administrador.
    public ServicioBalance() {
        this(new VentaDAO(), new GastoDAO(), new CostoFijoDAO(), new ConfiguracionDAO(),
                new ProductoDAO(), new CierreDiarioDAO(), Clock.systemDefaultZone());
    }

    public ServicioBalance(VentaDAO ventas, GastoDAO gastos, CostoFijoDAO costos, ConfiguracionDAO configuracion,
                           ProductoDAO productos, CierreDiarioDAO cierres, Clock reloj) {
        super(reloj);
        this.ventas = Objects.requireNonNull(ventas);
        this.gastos = Objects.requireNonNull(gastos);
        this.costos = Objects.requireNonNull(costos);
        this.configuracion = Objects.requireNonNull(configuracion);
        this.productos = Objects.requireNonNull(productos);
        this.cierres = Objects.requireNonNull(cierres);
    }

    // Los datos financieros completos se restringen a los perfiles autorizados.
    public BalanceDia obtenerBalanceDelDia(LocalDate fecha) {
        sesion.exigirPermiso(Permiso.VER_BALANCE);
        return calcularBalance(fecha);
    }

    // Un cierre conserva su foto financiera aunque despues cambien costos o configuracion.
    BalanceDia calcularBalance(LocalDate fecha) {
        Objects.requireNonNull(fecha);
        CierreDiario cerrado = cierres.buscarPorFecha(fecha);
        if (cerrado != null) return desdeCierre(cerrado);
        Negocio negocio = configuracion.cargarNegocio();
        List<Venta> operaciones = ventas.listarPorFecha(fecha);
        List<Gasto> egresos = gastos.listarPorFecha(fecha);
        BigDecimal ingresos = calculadora.calcularIngresos(operaciones);
        BigDecimal variables = calculadora.calcularCostosVariables(operaciones, egresos);
        BigDecimal margen = calculadora.calcularMargenContribucion(ingresos, variables);
        BigDecimal fijos = calculadora.calcularCostoFijoDiario(costos.sumarMontoMensualActivos(), negocio.getDiasOperacion());
        // Un GastoFijo registrado es adicional al catalogo; no se debe registrar dos veces el mismo costo.
        for (Gasto g : egresos) if (g instanceof GastoFijo) fijos = fijos.add(g.calcularMontoDiario());
        BigDecimal razon = calculadora.calcularRazonMargen(margen, ingresos);
        boolean estimado = false;
        // Sin ventas se usa el promedio simple de margenes del catalogo activo, sin ignorar perdidas.
        if (ingresos.signum() == 0) {
            razon = promedioMargenCatalogo();
            estimado = razon.signum() > 0 && (fijos.signum() > 0 || variables.signum() > 0);
        }
        boolean sinCostos = fijos.signum() == 0 && variables.signum() == 0;
        boolean calculable = razon.signum() > 0 || sinCostos;
        BigDecimal equilibrio = BigDecimal.ZERO, faltante = BigDecimal.ZERO, avance = BigDecimal.ZERO;
        if (calculable) {
            // Sin ventas, los gastos ya pagados tambien deben cubrirse con el margen estimado.
            BigDecimal metaBase = ingresos.signum() == 0 ? fijos.add(variables) : fijos;
            if (!sinCostos) equilibrio = calculadora.calcularPuntoEquilibrio(metaBase, razon);
            faltante = calculadora.calcularFaltante(equilibrio, ingresos);
            avance = calculadora.calcularPorcentajeAvance(ingresos, equilibrio);
        }
        return new BalanceDia(fecha, ingresos, variables, margen, fijos,
                calculadora.calcularResultado(margen, fijos), equilibrio, faltante, avance, calculable, estimado);
    }

    // Mantiene la razon con precision decimal en lugar de convertir dinero a double.
    private BigDecimal promedioMargenCatalogo() {
        List<Producto> catalogo = productos.listarActivos();
        if (catalogo.isEmpty()) return BigDecimal.ZERO;
        BigDecimal suma = BigDecimal.ZERO;
        for (Producto p : catalogo)
            suma = suma.add(p.getPrecioVenta().subtract(p.getCostoCompra()).divide(p.getPrecioVenta(), 10, RoundingMode.HALF_UP));
        return suma.divide(BigDecimal.valueOf(catalogo.size()), 10, RoundingMode.HALF_UP);
    }

    // Reconstruye resultados cerrados exclusivamente desde los importes guardados.
    private BalanceDia desdeCierre(CierreDiario c) {
        BigDecimal margen = c.getIngresos().subtract(c.getCostosVariables());
        boolean calculable = c.getPuntoEquilibrio().signum() > 0 ||
                (c.getCostosFijos().signum() == 0 && c.getResultado().signum() >= 0);
        return new BalanceDia(c.getFecha(), c.getIngresos(), c.getCostosVariables(), margen, c.getCostosFijos(),
                c.getResultado(), c.getPuntoEquilibrio(),
                calculable ? calculadora.calcularFaltante(c.getPuntoEquilibrio(), c.getIngresos()) : BigDecimal.ZERO,
                calculable ? calculadora.calcularPorcentajeAvance(c.getIngresos(), c.getPuntoEquilibrio()) : BigDecimal.ZERO,
                calculable, calculable && c.getIngresos().signum() == 0 && c.getPuntoEquilibrio().signum() > 0);
    }

    // Los reportes recorren un rango inclusivo sin guardar todas las ventas en Negocio.
    public List<BalanceDia> obtenerBalanceRango(LocalDate desde, LocalDate hasta) {
        sesion.exigirPermiso(Permiso.VER_BALANCE);
        Objects.requireNonNull(desde);
        Objects.requireNonNull(hasta);
        if (hasta.isBefore(desde)) throw new IllegalArgumentException("Rango invertido");
        List<BalanceDia> resultados = new ArrayList<>();
        for (LocalDate fecha = desde; !fecha.isAfter(hasta); fecha = fecha.plusDays(1))
            resultados.add(calcularBalance(fecha));
        return resultados;
    }

    // Todos los perfiles ven el semaforo; el cajero no recibe las cifras del balance.
    public EstadoNegocio obtenerEstadoDelDia() {
        return obtenerResumenDelDia().getEstado();
    }

    // Presenta avance y mensaje sin revelar importes al cajero.
    public ResumenSemaforo obtenerResumenDelDia() {
        usuario();
        BalanceDia balance = calcularBalance(hoy());
        EstadoNegocio estado = new Diagnostico().evaluar(balance, configuracion.cargarNegocio());
        return new ResumenSemaforo(estado, estado.getMensaje(balance), balance.getPorcentajeAvance(), balance.isPuntoEquilibrioCalculable());
    }
}
