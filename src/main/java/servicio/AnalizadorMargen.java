package servicio;

import modelo.*;
import dao.ProductoDAO;

import java.util.*;
import java.time.Clock;

/**
 * Ordena productos por margen y ofrece explicaciones sencillas para el negocio.
 */
public class AnalizadorMargen extends ServicioBase {
    private final ProductoDAO productos;

    // El DAO se recibe para utilizar el mismo catalogo que los demas servicios.
    public AnalizadorMargen() {
        this(new ProductoDAO(), Clock.systemDefaultZone());
    }

    public AnalizadorMargen(ProductoDAO productos, Clock reloj) {
        super(reloj);
        this.productos = Objects.requireNonNull(productos);
    }

    // Copia antes de ordenar para no cambiar la lista recibida del controlador.
    public List<Producto> generarRanking(List<Producto> catalogo) {
        sesion.exigirPermiso(Permiso.VER_BALANCE);
        List<Producto> ranking = new ArrayList<>(catalogo);
        ranking.sort(Comparator.comparingDouble(Producto::calcularMargen).reversed().thenComparing(Producto::getNombre));
        return ranking;
    }

    // Nunca permite solicitar una cantidad negativa de resultados.
    public List<Producto> obtenerMasRentables(int n) {
        sesion.exigirPermiso(Permiso.VER_BALANCE);
        if (n < 0) throw new IllegalArgumentException("Cantidad no negativa");
        List<Producto> ranking = generarRanking(productos.listarActivos());
        return new ArrayList<>(ranking.subList(0, Math.min(n, ranking.size())));
    }

    // El umbral esta expresado en porcentaje, como Producto.calcularMargen.
    public List<Producto> detectarMargenBajo(double umbral) {
        sesion.exigirPermiso(Permiso.VER_BALANCE);
        if (!Double.isFinite(umbral) || umbral < 0 || umbral > 100)
            throw new IllegalArgumentException("Umbral entre 0 y 100");
        List<Producto> bajos = new ArrayList<>();
        for (Producto p : productos.listarActivos()) if (p.calcularMargen() < umbral) bajos.add(p);
        return bajos;
    }

    // El consejo describe el calculo; no modifica automaticamente precios ni costos.
    public String generarConsejo(Producto p) {
        sesion.exigirPermiso(Permiso.VER_BALANCE);
        if (p.calcularUtilidadUnitaria().signum() <= 0)
            return p.getNombre() + ": revise el precio y el costo; cada unidad no aporta ganancia.";
        return p.getNombre() + ": aporta " + p.calcularUtilidadUnitaria().toPlainString()
                + " por unidad antes de cubrir costos fijos.";
    }
}
