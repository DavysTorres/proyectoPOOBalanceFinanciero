package modelo;

import excepciones.StockInsuficienteException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/** Pruebas de consola sin dependencias externas. No requieren MySQL ni ventanas. */
public class RevisionModelo {
    private static int correctas;
    private static int fallidas;

    private interface Caso { void ejecutar() throws Exception; }

    private static void probar(String nombre, Caso caso) {
        try {
            caso.ejecutar();
            correctas++;
            System.out.println("OK: " + nombre);
        } catch (Throwable error) {
            fallidas++;
            System.out.println("FALLO: " + nombre + " -> " + error);
        }
    }

    private static void verificar(boolean condicion) {
        if (!condicion) throw new AssertionError("No se cumple el resultado esperado");
    }

    private static void monto(String esperado, BigDecimal actual) {
        verificar(new BigDecimal(esperado).compareTo(actual) == 0);
    }

    private static void rechaza(Class<? extends Throwable> tipo, Caso caso) throws Exception {
        try { caso.ejecutar(); }
        catch (Throwable error) {
            if (tipo.isInstance(error)) return;
            throw new AssertionError("Se esperaba " + tipo.getSimpleName() + " y se obtuvo " + error);
        }
        throw new AssertionError("Se aceptaron datos invalidos");
    }

    private static Producto producto() {
        return new Producto(1, "Cafe", new BigDecimal("10000"), new BigDecimal("6000"), 10, 2);
    }

    private static Cajero cajero() {
        return new Cajero(1, "Ana", "ana", "hashPrueba", true, LocalDateTime.now());
    }

    public static void main(String[] args) {
        probar("Jefe tiene exactamente los ocho permisos de la guia", () -> {
            Usuario jefe = new Jefe(2, "Luis", "luis", "hashPrueba", true, LocalDateTime.now());
            verificar(jefe.obtenerPermisos().size() == 8);
            verificar(jefe.obtenerPermisos().containsAll(Arrays.asList(
                    Permiso.REGISTRAR_VENTA, Permiso.REGISTRAR_COMPRA,
                    Permiso.REGISTRAR_GASTO, Permiso.MODIFICAR_VENTA,
                    Permiso.VER_BALANCE, Permiso.VER_REPORTES,
                    Permiso.VER_HISTORIAL, Permiso.CERRAR_CAJA)));
            verificar(!jefe.tienePermiso(Permiso.GESTIONAR_USUARIOS));
        });
        probar("Menu de Jefe coincide con la guia", () -> verificar(
                new Jefe(2, "Luis", "luis", "hashPrueba", true, LocalDateTime.now()).mostrarMenu()
                        .equals(Arrays.asList("Registrar venta", "Compras y gastos", "Balance",
                                "Margen por producto", "Historial", "Cierre de caja"))));
        probar("Administrador obtiene todos los permisos", () -> verificar(
                new Administrador(3, "Ada", "ada", "hashPrueba", true, LocalDateTime.now())
                        .obtenerPermisos().containsAll(Arrays.asList(Permiso.values()))));
        probar("Cajero solo registra ventas", () -> verificar(cajero().obtenerPermisos()
                .equals(Arrays.asList(Permiso.REGISTRAR_VENTA))));
        probar("Usuario se activa y desactiva", () -> {
            Usuario u = cajero(); u.desactivar(); verificar(!u.isActivo());
            u.activar(); verificar(u.isActivo());
        });
        probar("Usuario rechaza nombre vacio", () -> rechaza(IllegalArgumentException.class,
                () -> cajero().setNombre(" ")));
        probar("Usuario rechaza username nulo", () -> rechaza(IllegalArgumentException.class,
                () -> cajero().setUsername(null)));
        probar("Producto calcula margen y utilidad", () -> {
            verificar(producto().calcularMargen() == 40.0);
            monto("4000", producto().calcularUtilidadUnitaria());
        });
        probar("Producto rechaza precio negativo", () -> rechaza(IllegalArgumentException.class,
                () -> producto().setPrecioVenta(new BigDecimal("-1"))));
        probar("Producto rechaza costo cero", () -> rechaza(IllegalArgumentException.class,
                () -> producto().setCostoCompra(BigDecimal.ZERO)));
        probar("Producto rechaza stock negativo", () -> rechaza(IllegalArgumentException.class,
                () -> producto().setStock(-1)));
        probar("Producto rechaza stock minimo negativo", () -> rechaza(IllegalArgumentException.class,
                () -> producto().setStockMinimo(-1)));
        probar("Stock insuficiente conserva existencias", () -> {
            Producto p = producto();
            rechaza(StockInsuficienteException.class, () -> p.descontarStock(11));
            verificar(p.getStock() == 10);
        });
        probar("Descuento, aumento y alerta de stock", () -> {
            Producto p = producto(); p.descontarStock(8); verificar(p.tieneStockBajo());
            p.aumentarStock(3); verificar(p.getStock() == 5 && !p.tieneStockBajo());
        });
        probar("Stock rechaza desbordamiento", () -> {
            Producto p = producto(); p.setStock(Integer.MAX_VALUE);
            rechaza(IllegalArgumentException.class, () -> p.aumentarStock(1));
            verificar(p.getStock() == Integer.MAX_VALUE);
        });
        probar("Venta suma cantidades y calcula costos", () -> {
            Venta v = new Venta(cajero()); Producto p = producto();
            v.agregarDetalle(p, 2); v.agregarDetalle(p, 1);
            verificar(v.getDetalles().size() == 1);
            monto("30000", v.getTotal()); monto("18000", v.calcularCostoTotal());
        });
        probar("Venta conserva precio y costo historicos", () -> {
            Venta v = new Venta(cajero()); Producto p = producto(); v.agregarDetalle(p, 2);
            p.setPrecioVenta(new BigDecimal("20000")); p.setCostoCompra(new BigDecimal("15000"));
            monto("20000", v.getTotal()); monto("12000", v.calcularCostoTotal());
        });
        probar("Venta identifica productos persistidos por id", () -> {
            Venta v = new Venta(cajero()); v.agregarDetalle(producto(), 1); v.agregarDetalle(producto(), 2);
            verificar(v.getDetalles().size() == 1); monto("30000", v.getTotal());
        });
        probar("Venta distingue productos nuevos sin id", () -> {
            Producto a = producto(); Producto b = producto(); a.setId(0); b.setId(0);
            Venta v = new Venta(cajero()); v.agregarDetalle(a, 1); v.agregarDetalle(b, 1);
            verificar(v.getDetalles().size() == 2);
        });
        probar("Venta reduce y quita detalles", () -> {
            Venta v = new Venta(cajero()); Producto p = producto(); v.agregarDetalle(p, 2);
            v.reducirCantidad(p); monto("10000", v.getTotal());
            v.reducirCantidad(p); verificar(v.estaVacia());
            v.agregarDetalle(p, 1); v.quitarDetalle(p); monto("0", v.getTotal());
        });
        probar("Venta rechaza cantidad cero", () -> rechaza(IllegalArgumentException.class,
                () -> new Venta(cajero()).agregarDetalle(producto(), 0)));
        probar("Lista de venta impide agregar desde afuera", () -> {
            Venta v = new Venta(cajero());
            rechaza(UnsupportedOperationException.class,
                    () -> v.getDetalles().add(new DetalleVenta(producto(), 1)));
        });
        probar("Compra suma detalles", () -> {
            Compra c = new Compra(cajero(), "Proveedor");
            c.agregarDetalle(producto(), 3, new BigDecimal("6000"));
            c.agregarDetalle(producto(), 2, new BigDecimal("5000")); monto("28000", c.getTotal());
        });
        probar("Compra rechaza proveedor vacio", () -> rechaza(IllegalArgumentException.class,
                () -> new Compra(cajero(), " ")));
        probar("DetalleCompra rechaza cantidad negativa", () -> rechaza(IllegalArgumentException.class,
                () -> new DetalleCompra(producto(), -1, BigDecimal.ONE)));
        probar("Rol y Permiso externos convierten texto", () -> {
            verificar(Rol.fromString(" jefe ") == Rol.JEFE);
            verificar(Permiso.fromString(" registrar_venta ") == Permiso.REGISTRAR_VENTA);
        });

        // Regresion de los defectos encontrados durante la primera revision.
        probar("Usuario rechaza id negativo", () -> rechaza(IllegalArgumentException.class,
                () -> cajero().setId(-1)));
        probar("Usuario rechaza hash nulo", () -> rechaza(IllegalArgumentException.class,
                () -> cajero().setPasswordHash(null)));
        probar("Usuario rechaza fecha nula", () -> rechaza(IllegalArgumentException.class,
                () -> cajero().setCreadoEn(null)));
        probar("Subclase Cajero no permite cambiar su rol a Jefe", () -> {
            Usuario u = cajero();
            rechaza(IllegalArgumentException.class, () -> u.setRol(Rol.JEFE));
        });
        probar("Usuario utiliza el Rol del paquete modelo", () -> verificar(
                Usuario.class.getMethod("getRol").getReturnType() == modelo.Rol.class));
        probar("Permiso del usuario es el del paquete modelo", () -> verificar(
                cajero().obtenerPermisos().get(0).getClass() == (Class<?>) modelo.Permiso.class));

        probarClasesCompletadas();
        System.out.println("RESULTADO: " + correctas + " correctas, " + fallidas + " fallidas");
        if (fallidas > 0) System.exit(1);
    }

    // Prueba las nuevas entidades sin SQL, servicios ni herramientas externas.
    private static void probarClasesCompletadas() {
        LocalDate fecha = LocalDate.of(2026, 11, 6);
        // Integra entidades en memoria y verifica a mano las formulas de la guia.
        probar("Ejemplo completo de la guia con venta, gasto fijo y balance", () -> {
            Producto p = producto(); p.setStock(100);
            Venta venta = new Venta(cajero()); venta.agregarDetalle(p, 85);
            BigDecimal ingresos = venta.calcularTotal();
            BigDecimal costos = venta.calcularCostoTotal();
            BigDecimal margen = ingresos.subtract(costos);
            BigDecimal razon = margen.divide(ingresos, 8, RoundingMode.HALF_UP);
            Gasto fijo = new GastoFijo(0, cajero(), "Costos mensuales",
                    new BigDecimal("11700000"), fecha, 30);
            BigDecimal costoFijo = fijo.calcularMontoDiario();
            BigDecimal equilibrio = costoFijo.divide(razon, 2, RoundingMode.HALF_UP);
            BigDecimal faltante = equilibrio.subtract(ingresos).max(BigDecimal.ZERO);
            BigDecimal avance = ingresos.multiply(new BigDecimal("100"))
                    .divide(equilibrio, 2, RoundingMode.HALF_UP);
            BalanceDia balance = new BalanceDia(fecha, ingresos, costos, margen,
                    costoFijo, margen.subtract(costoFijo), equilibrio, faltante, avance);
            monto("850000", balance.getIngresos());
            monto("510000", balance.getCostosVariables());
            monto("390000", balance.getCostosFijosDia());
            monto("975000", balance.getPuntoEquilibrio());
            monto("-50000", balance.getResultado());
            monto("125000", balance.getFaltante());
            monto("87.18", balance.getPorcentajeAvance());
        });
        probar("Gasto aplica polimorfismo a fijo y variable", () -> {
            Gasto fijo = new GastoFijo(0, cajero(), "Arriendo", new BigDecimal("390000"),
                    fecha, 30);
            Gasto variable = new GastoVariable(0, cajero(), "Transporte",
                    new BigDecimal("12000"), fecha);
            monto("13000", fijo.calcularMontoDiario());
            monto("12000", variable.calcularMontoDiario());
        });
        probar("Gasto fijo redondea division periodica", () -> monto("33.33",
                new GastoFijo(0, cajero(), "Servicio", new BigDecimal("100"), fecha, 3)
                        .calcularMontoDiario()));
        probar("Gasto rechaza importe cero", () -> rechaza(IllegalArgumentException.class,
                () -> new GastoVariable(0, cajero(), "Transporte", BigDecimal.ZERO, fecha)));
        probar("Gasto rechaza concepto vacio", () -> rechaza(IllegalArgumentException.class,
                () -> new GastoVariable(0, cajero(), " ", BigDecimal.ONE, fecha)));
        probar("Gasto rechaza usuario nulo", () -> rechaza(IllegalArgumentException.class,
                () -> new GastoVariable(0, null, "Transporte", BigDecimal.ONE, fecha)));
        probar("Gasto rechaza fecha nula", () -> rechaza(IllegalArgumentException.class,
                () -> new GastoVariable(0, cajero(), "Transporte", BigDecimal.ONE, null)));
        probar("Gasto fijo rechaza dias cero y mayores a 31", () -> {
            rechaza(IllegalArgumentException.class, () -> new GastoFijo(0, cajero(),
                    "Arriendo", BigDecimal.ONE, fecha, 0));
            rechaza(IllegalArgumentException.class, () -> new GastoFijo(0, cajero(),
                    "Arriendo", BigDecimal.ONE, fecha, 32));
        });
        probar("Costo fijo calcula monto diario", () -> {
            CostoFijo c = new CostoFijo(0, "Arriendo", new BigDecimal("390000"), true);
            monto("13000", c.calcularMontoDiario(30));
            c.setActivo(false); verificar(!c.isActivo());
        });
        probar("Costo fijo rechaza monto negativo y divisor cero", () -> {
            rechaza(IllegalArgumentException.class,
                    () -> new CostoFijo(0, "Arriendo", new BigDecimal("-1"), true));
            rechaza(IllegalArgumentException.class,
                    () -> new CostoFijo(0, "Arriendo", BigDecimal.ONE, true).calcularMontoDiario(0));
        });
        probar("Negocio tiene umbrales predeterminados de la guia", () -> {
            Negocio n = negocio();
            monto("70", n.getUmbralAmarillo()); monto("15", n.getUmbralMargenBajo());
        });
        probar("Negocio acepta los limites de dias de operacion", () -> {
            Negocio n = negocio(); n.setDiasOperacion(1); verificar(n.getDiasOperacion() == 1);
            n.setDiasOperacion(31); verificar(n.getDiasOperacion() == 31);
        });
        probar("Negocio rechaza dias fuera del intervalo", () -> {
            rechaza(IllegalArgumentException.class, () -> negocio().setDiasOperacion(0));
            rechaza(IllegalArgumentException.class, () -> negocio().setDiasOperacion(32));
        });
        probar("Horario invalido no modifica la configuracion anterior", () -> {
            Negocio n = negocio();
            rechaza(IllegalArgumentException.class,
                    () -> n.setHorario(LocalTime.of(20, 0), LocalTime.of(8, 0)));
            verificar(n.getHoraApertura().equals(LocalTime.of(8, 0)));
            verificar(n.getHoraCierre().equals(LocalTime.of(18, 0)));
        });
        probar("Negocio rechaza cierre igual a apertura", () -> rechaza(IllegalArgumentException.class,
                () -> negocio().setHoraCierre(LocalTime.of(8, 0))));
        probar("Negocio cambia ambas horas de forma atomica", () -> {
            Negocio n = negocio(); n.setHorario(LocalTime.of(19, 0), LocalTime.of(23, 0));
            verificar(n.getHoraApertura().equals(LocalTime.of(19, 0)));
        });
        probar("Umbrales rechazan negativos y mayores de cien", () -> {
            rechaza(IllegalArgumentException.class,
                    () -> negocio().setUmbralAmarillo(new BigDecimal("101")));
            rechaza(IllegalArgumentException.class,
                    () -> negocio().setUmbralMargenBajo(new BigDecimal("-1")));
        });
        probar("Cierre calcula sobrante, faltante e igualdad por valor", () -> {
            CierreDiario c = cierre("100000", "100500");
            monto("500", c.getDiferencia()); verificar(c.hayDescuadre());
            c.setEfectivoContado(new BigDecimal("99500"));
            monto("-500", c.calcularDiferenciaCaja()); verificar(c.hayDescuadre());
            c.setEfectivoContado(new BigDecimal("100000.00"));
            verificar(!c.hayDescuadre());
        });
        probar("Cierre actualiza diferencia al cambiar efectivo del sistema", () -> {
            CierreDiario c = cierre("100", "100");
            c.setEfectivoSistema(new BigDecimal("120")); monto("-20", c.getDiferencia());
        });
        probar("Cierre rechaza conteo negativo sin alterar el anterior", () -> {
            CierreDiario c = cierre("100", "100");
            rechaza(IllegalArgumentException.class,
                    () -> c.setEfectivoContado(new BigDecimal("-1")));
            monto("100", c.getEfectivoContado()); verificar(!c.hayDescuadre());
        });
        probar("Cierre permite perdida contable", () -> {
            CierreDiario c = cierre("0", "0");
            c.setResultado(new BigDecimal("-100")); monto("-100", c.getResultado());
        });
        probar("Registro conserva valores y motivo de la modificacion", () -> {
            RegistroCambio r = new RegistroCambio(0, new Venta(cajero()), cajero(),
                    fecha.atStartOfDay(), new BigDecimal("100"), new BigDecimal("90"), " Ajuste ");
            monto("100", r.getValorAnterior()); monto("90", r.getValorNuevo());
            verificar(r.getMotivo().equals("Ajuste"));
        });
        probar("Registro rechaza motivo vacio", () -> rechaza(IllegalArgumentException.class,
                () -> new RegistroCambio(0, new Venta(cajero()), cajero(), fecha.atStartOfDay(),
                        BigDecimal.ONE, BigDecimal.ONE, " ")));
        probar("Registro rechaza valor negativo y venta nula", () -> {
            rechaza(IllegalArgumentException.class,
                    () -> new RegistroCambio(0, new Venta(cajero()), cajero(), fecha.atStartOfDay(),
                            new BigDecimal("-1"), BigDecimal.ONE, "Ajuste"));
            rechaza(IllegalArgumentException.class,
                    () -> new RegistroCambio(0, null, cajero(), fecha.atStartOfDay(),
                            BigDecimal.ONE, BigDecimal.ONE, "Ajuste"));
        });
        probar("Balance conserva resultados del ejemplo de la guia", () -> {
            BalanceDia b = new BalanceDia(fecha, new BigDecimal("850000"), new BigDecimal("510000"),
                    new BigDecimal("340000"), new BigDecimal("390000"), new BigDecimal("-50000"),
                    new BigDecimal("975000"), new BigDecimal("125000"), new BigDecimal("87.18"));
            monto("975000", b.getPuntoEquilibrio()); monto("-50000", b.getResultado());
            monto("340000", b.getMargenContribucion());
        });
        probar("Balance acepta dia sin ventas y perdida", () -> {
            BalanceDia b = new BalanceDia(fecha, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    new BigDecimal("100"), new BigDecimal("-100"), BigDecimal.ZERO,
                    BigDecimal.ZERO, BigDecimal.ZERO);
            monto("0", b.getIngresos()); monto("-100", b.getResultado());
        });
        probar("Balance permite avance superior a cien", () -> {
            BalanceDia b = new BalanceDia(fecha, new BigDecimal("200"), BigDecimal.ZERO,
                    new BigDecimal("200"), BigDecimal.ONE, new BigDecimal("199"),
                    new BigDecimal("100"), BigDecimal.ZERO, new BigDecimal("200"));
            monto("200", b.getPorcentajeAvance());
        });
        probar("Balance rechaza ingresos negativos", () -> rechaza(IllegalArgumentException.class,
                () -> new BalanceDia(fecha, new BigDecimal("-1"), BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)));
        probar("Registro y Balance no permiten cambiar sus propios campos", () -> {
            for (Class<?> tipo : Arrays.asList(RegistroCambio.class, BalanceDia.class)) {
                verificar(Modifier.isFinal(tipo.getModifiers()));
                for (java.lang.reflect.Field campo : tipo.getDeclaredFields()) {
                    verificar(Modifier.isPrivate(campo.getModifiers()));
                    verificar(Modifier.isFinal(campo.getModifiers()));
                }
                for (java.lang.reflect.Method metodo : tipo.getDeclaredMethods())
                    verificar(!metodo.getName().startsWith("set"));
            }
        });
        probar("Nuevo usuario valida tambien por constructor", () -> {
            rechaza(IllegalArgumentException.class,
                    () -> new Cajero(-1, "Ana", "ana", "hash", true, LocalDateTime.now()));
            rechaza(IllegalArgumentException.class,
                    () -> new Cajero(0, "Ana", "ana", null, true, LocalDateTime.now()));
        });
        probar("Venta y compra en curso no alteran inventario antes de confirmar", () -> {
            Producto p = producto();
            Venta v = new Venta(cajero()); v.agregarDetalle(p, 2);
            Compra c = new Compra(cajero(), "Proveedor");
            c.agregarDetalle(p, 3, new BigDecimal("6000"));
            verificar(p.getStock() == 10);
        });
    }

    // Fixture de configuracion valida para probar cambios de horario y umbrales.
    private static Negocio negocio() {
        return new Negocio("Tienda", 30, LocalTime.of(8, 0), LocalTime.of(18, 0));
    }

    // Fixture de cierre con resultados en cero y efectivo configurable.
    private static CierreDiario cierre(String sistema, String contado) {
        return new CierreDiario(0, LocalDate.of(2026, 11, 6), cajero(), BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal(sistema), new BigDecimal(contado));
    }
}
