package controlador;

import vista.contratos.*;
import servicio.*;
import modelo.*;
import excepciones.PermisoDenegadoException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Prepara lineas de una compra y delega validacion, fecha definitiva y persistencia al servicio.
 */
public final class CompraGastoControlador extends ControladorBase<VistaCompraGasto> {
    private final ServicioCompras compras;
    private final ServicioProductos productos;
    private Compra compraEnCurso;

    // Una ventana conserva su propia compra hasta guardarla o cancelarla.
    public CompraGastoControlador(VistaCompraGasto vista, ServicioCompras compras, EjecutorTareas tareas) {
        this(vista, compras, new ServicioProductos(), tareas);
    }

    public CompraGastoControlador(VistaCompraGasto vista, ServicioCompras compras, ServicioProductos productos, EjecutorTareas tareas) {
        super(vista, tareas);
        this.compras = Objects.requireNonNull(compras);
        this.productos = Objects.requireNonNull(productos);
        vista.alAccionar(AccionVista.BUSCAR, this::cargarProductos);
        vista.alAccionar(AccionVista.AGREGAR, this::agregarLineaCompra);
        vista.alAccionar(AccionVista.GUARDAR_COMPRA, this::guardarCompra);
        vista.alAccionar(AccionVista.GUARDAR_GASTO, this::guardarGasto);
        vista.alAccionar(AccionVista.LISTAR, this::listar);
        vista.alAccionar(AccionVista.CANCELAR, this::cancelarCompra);
    }

    public void cargarProductos() {
        preparar(() -> {
            String filtro = vista.getFiltro();
            ejecutar(() -> productos.buscarProductos(filtro), vista::mostrarProductos);
        });
    }

    // El subtotal lo calcula DetalleCompra; el controlador no repite su formula.
    public void agregarLineaCompra() {
        preparar(() -> {
            Producto p = seleccionado(vista.getProductoSeleccionado());
            int cantidad = entero(vista.getCantidad());
            BigDecimal costo = decimal(vista.getCostoUnitario());
            String proveedor = vista.getProveedor();
            ejecutar(() -> {
                Sesion.getInstancia().exigirPermiso(Permiso.REGISTRAR_COMPRA);
                Usuario u = Sesion.getInstancia().getUsuarioActual();
                if (compraEnCurso == null) compraEnCurso = new Compra(u, proveedor);
                if (compraEnCurso.getUsuario().getId() != u.getId())
                    throw new PermisoDenegadoException("La compra pertenece a otro usuario");
                compraEnCurso.setProveedor(proveedor);
                compraEnCurso.agregarDetalle(p, cantidad, costo);
                return compraEnCurso;
            }, vista::mostrarCompra);
        });
    }

    // El servicio realiza el movimiento de stock una sola vez.
    public void guardarCompra() {
        preparar(() -> {
            Compra compra = seleccionado(compraEnCurso);
            String proveedor = vista.getProveedor();
            ejecutar(() -> {
                Sesion.getInstancia().exigirPermiso(Permiso.REGISTRAR_COMPRA);
                if (compra.getUsuario().getId() != Sesion.getInstancia().getUsuarioActual().getId())
                    throw new PermisoDenegadoException("La compra pertenece a otro usuario");
                compra.setProveedor(proveedor);
                return compras.registrarCompra(compra);
            }, guardada -> {
                compraEnCurso = null;
                vista.mostrarCompra(null);
                vista.mostrarInformacion("Compra guardada con numero " + guardada.getId());
                informarCambio();
            });
        });
    }

    // El modelo expresa polimorfismo entre gasto fijo y variable; el servicio fija la fecha real.
    public void guardarGasto() {
        preparar(() -> {
            String concepto = vista.getConceptoGasto();
            BigDecimal monto = decimal(vista.getMontoGasto());
            boolean fijo = vista.esGastoFijo();
            int dias = fijo ? entero(vista.getDiasOperacion()) : 1;
            ejecutar(() -> {
                Sesion.getInstancia().exigirPermiso(Permiso.REGISTRAR_GASTO);
                Usuario u = Sesion.getInstancia().getUsuarioActual();
                Gasto g = fijo ? new GastoFijo(0, u, concepto, monto, LocalDate.now(), dias)
                        : new GastoVariable(0, u, concepto, monto, LocalDate.now());
                return compras.registrarGasto(g);
            }, g -> {
                vista.mostrarInformacion("Gasto guardado con numero " + g.getId());
                informarCambio();
            });
        });
    }

    // Ambos listados se obtienen en una tarea; no se mezclan callbacks con trabajo JDBC.
    public void listar() {
        ejecutar(() -> new Listados(compras.listarComprasDelDia(), compras.listarGastosDelDia()),
                datos -> {
                    vista.mostrarCompras(datos.compras);
                    vista.mostrarGastos(datos.gastos);
                });
    }

    public void cancelarCompra() {
        ejecutar(() -> {
            Sesion.getInstancia().exigirPermiso(Permiso.REGISTRAR_COMPRA);
            compraEnCurso = null;
            return null;
        }, ignorado -> vista.mostrarCompra(null));
    }

    // Objeto privado de transporte para no entregar datos con tipos Object o cast innecesarios.
    private static final class Listados {
        private final java.util.List<Compra> compras;
        private final java.util.List<Gasto> gastos;

        Listados(java.util.List<Compra> compras, java.util.List<Gasto> gastos) {
            this.compras = compras;
            this.gastos = gastos;
        }
    }
}
