package controlador;

import vista.contratos.*;
import servicio.*;
import modelo.Producto;

import java.util.Objects;

/**
 * Presenta el ranking calculado por el analizador, sin ordenar importes en la ventana.
 */
public final class MargenControlador extends ControladorBase<VistaMargen> {
    private final AnalizadorMargen margen;
    private final ServicioProductos productos;

    // La consulta de catalogo y el analisis se ejecutan en el mismo trabajo.
    public MargenControlador(VistaMargen vista, AnalizadorMargen margen, ServicioProductos productos, EjecutorTareas tareas) {
        super(vista, tareas);
        this.margen = Objects.requireNonNull(margen);
        this.productos = Objects.requireNonNull(productos);
        vista.alAccionar(AccionVista.CARGAR, this::cargarRanking);
        vista.alAccionar(AccionVista.CONSEJO, this::mostrarConsejos);
    }

    public void cargarRanking() {
        ejecutar(() -> margen.generarRanking(productos.listarProductosActivos()), vista::mostrarRanking);
    }

    public void mostrarConsejos() {
        preparar(() -> {
            Producto p = seleccionado(vista.getProductoSeleccionado());
            ejecutar(() -> margen.generarConsejo(p), vista::mostrarConsejo);
        });
    }
}
