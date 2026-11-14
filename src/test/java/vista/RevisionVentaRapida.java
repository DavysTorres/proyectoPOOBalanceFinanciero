package vista;

import controlador.*;
import modelo.*;
import servicio.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

/** Recorrido real vista/controlador con datos en memoria, sin MySQL. */
public final class RevisionVentaRapida {
    private static void exigir(boolean v) { if (!v) throw new AssertionError("Venta rapida incorrecta"); }
    private static <T extends Component> T encontrar(Container p, Class<T> tipo, String texto) {
        for (Component c : p.getComponents()) {
            String contenido = c instanceof JLabel l ? l.getText() : c instanceof JButton b ? b.getText() : "";
            if (tipo.isInstance(c) && contenido.equals(texto)) return tipo.cast(c);
            if (c instanceof Container contenedor) {
                T valor = encontrar(contenedor, tipo, texto); if (valor != null) return valor;
            }
        }
        return null;
    }
    private static void pulsarFila(JTable t, int fila, int columna) {
        exigir(t.editCellAt(fila, columna));
        ((JButton)t.getEditorComponent()).doClick();
    }
    private static void imagen(FrmPrincipal principal, String nombre) throws Exception {
        principal.validate(); Container c = principal.getContentPane();
        BufferedImage imagen = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imagen.createGraphics(); c.printAll(g); g.dispose();
        Path salida = Path.of("target/revision-venta-rapida"); Files.createDirectories(salida);
        ImageIO.write(imagen, "png", salida.resolve(nombre + ".png").toFile());
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Usuario cajero = new Cajero(1, "Ana", "ana", "hash", true, LocalDateTime.now());
            Producto cafe = new Producto(1, "Caf\u00e9", new BigDecimal("10000"), new BigDecimal("6000"), 100, 2);
            Producto pan = new Producto(2, "Pan", new BigDecimal("5000"), new BigDecimal("3000"), 100, 2);
            class VentasMemoria extends ServicioVentas {
                Venta actual, cobrada;
                public Venta iniciarVenta() { exigir(actual == null); return actual = new Venta(cajero); }
                public void agregarProducto(Producto p, int n) { actual.agregarDetalle(p, n); }
                public void reducirCantidad(Producto p) { actual.reducirCantidad(p); }
                public void quitarProducto(Producto p) { actual.quitarDetalle(p); }
                public void cancelarVenta() { actual = null; }
                public Venta cobrar() { cobrada = actual; cobrada.setId(12); actual = null; return cobrada; }
            }
            VentasMemoria servicio = new VentasMemoria();
            ServicioProductos productos = new ServicioProductos() {
                public List<Producto> buscarProductos(String consulta) { return List.of(cafe, pan); }
            };
            EjecutorTareas inmediato = new EjecutorTareas() {
                public <T> void ejecutar(Operacion<T> op, Consumer<T> ok, Consumer<Exception> error) {
                    try { ok.accept(op.ejecutar()); } catch (Exception e) { error.accept(e); }
                }
            };
            FrmPrincipal principal = new FrmPrincipal(cajero);
            FrmVenta venta = new FrmVenta();
            try {
                principal.setSize(1440, 950); principal.addNotify();
                VentaControlador controlador = new VentaControlador(venta, servicio, productos, inmediato);
                venta.conectar(controlador); principal.mostrarModulo(venta); controlador.buscarProductos();
                JTextField filtro = (JTextField)encontrar(principal, JLabel.class, "Buscar por nombre o c\u00f3digo:").getLabelFor();
                JTextField cantidad = (JTextField)encontrar(principal, JLabel.class, "Cantidad a agregar:").getLabelFor();
                JButton agregar = encontrar(principal, JButton.class, "Agregar a la venta");
                JButton cobrar = encontrar(principal, JButton.class, "Cobrar");
                JTable tabla = encontrar(principal, JTable.class, "");
                exigir(tabla.getRowCount() == 0 && !cobrar.isEnabled());
                filtro.setText("1"); cantidad.setText("2"); agregar.doClick();
                exigir(servicio.actual.getTotal().compareTo(new BigDecimal("20000")) == 0);
                exigir(filtro.getText().isEmpty() && venta.getCantidad().equals("1"));
                filtro.setText("Pan"); agregar.doClick();
                exigir(tabla.getRowCount() == 2);
                // Aunque el buscador tenga Pan, el boton de la fila debe modificar Cafe.
                filtro.setText("Pan"); pulsarFila(tabla, 0, 5);
                exigir(servicio.actual.getDetalles().get(0).getCantidad() == 3);
                pulsarFila(tabla, 0, 4); exigir(servicio.actual.getDetalles().get(0).getCantidad() == 2);
                pulsarFila(tabla, 1, 6); exigir(tabla.getRowCount() == 1);
                JTextField recibido = (JTextField)encontrar(principal, JLabel.class, "Efectivo recibido ($, opcional):").getLabelFor();
                recibido.setText("1"); exigir(!cobrar.isEnabled());
                recibido.setText("30000"); exigir(cobrar.isEnabled());
                exigir(encontrar(principal, JLabel.class, "Cambio: " + util.FormatoMoneda.mostrar(new BigDecimal("10000"))) != null);
                imagen(principal, "venta-lista-para-cobrar");
                cobrar.doClick(); exigir(servicio.cobrada.getTotal().compareTo(new BigDecimal("20000")) == 0);
                exigir(tabla.getRowCount() == 0 && !cobrar.isEnabled());
                exigir(venta.estado.getText().contains("10.000"));
                filtro.setText("cafe"); agregar.doClick(); exigir(tabla.getRowCount() == 1);
                pulsarFila(tabla, 0, 4); exigir(tabla.getRowCount() == 0);
                filtro.setText("Pan"); agregar.doClick(); exigir(tabla.getRowCount() == 1);
                encontrar(principal, JButton.class, "Vaciar venta").doClick();
                exigir(servicio.actual == null && tabla.getRowCount() == 0);
                exigir(cafe.getStock() == 100 && pan.getStock() == 100);
            } catch (Exception e) { throw new RuntimeException(e); }
            finally { venta.cerrar(); principal.cerrar(); }
        });
        System.out.println("OK: busqueda, inicio automatico, acciones por fila, total, cambio, cobro y siguiente venta");
    }
}
