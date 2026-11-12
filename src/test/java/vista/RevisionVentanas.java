package vista;

import controlador.*;
import modelo.*;
import servicio.*;
import util.*;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;
import java.util.Arrays;
import java.util.function.Consumer;
import javax.imageio.ImageIO;

/**
 * Comprueba formularios reales sin mostrarlos y guarda imagenes para revisar su distribucion.
 */
public final class RevisionVentanas {
    private static int correctas;
    private static final Path SALIDA = Path.of("target/revision-ventanas");

    private interface Caso {
        void ejecutar() throws Exception;
    }

    private static void caso(String nombre, Caso prueba) throws Exception {
        prueba.ejecutar();
        correctas++;
        System.out.println("OK: " + nombre);
    }

    private static void verificar(boolean condicion) {
        if (!condicion) throw new AssertionError("Resultado inesperado");
    }

    private static BigDecimal d(String valor) {
        return new BigDecimal(valor);
    }

    private static <T extends Component> T componente(Container padre, Class<T> tipo, String texto) {
        for (Component c : padre.getComponents()) {
            if (tipo.isInstance(c) && (c instanceof JButton && texto.equals(((JButton)c).getText())
                    || c instanceof JLabel && texto.equals(((JLabel)c).getText()))) return tipo.cast(c);
            if (c instanceof Container) {
                T resultado = componente((Container)c, tipo, texto);
                if (resultado != null) return resultado;
            }
        }
        return null;
    }

    private static JTextField campo(FrmModulo ventana, String etiqueta) {
        return (JTextField) componente(ventana.campos, JLabel.class, etiqueta).getLabelFor();
    }

    private static void imagen(FrmBase ventana, String nombre) throws Exception {
        try {
            ventana.addNotify();
            ventana.validate();
            Container contenido = ventana.getContentPane();
            BufferedImage imagen = new BufferedImage(contenido.getWidth(), contenido.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D grafico = imagen.createGraphics();
            contenido.printAll(grafico);
            grafico.dispose();
            ImageIO.write(imagen, "png", SALIDA.resolve(nombre + ".png").toFile());
            verificar(contenido.getWidth() > 0 && contenido.getHeight() > 0);
        } finally {
            ventana.cerrar();
        }
    }

    public static void main(String[] args) throws Exception {
        Files.createDirectories(SALIDA);
        caso("Utilidades validan texto numero y fecha", () -> {
            verificar(Validador.esTextoVacio(" "));
            verificar(Validador.esNumeroPositivo(d("1")) && !Validador.esNumeroPositivo(d("-1")));
            verificar(Validador.esFechaValida("2026-11-06") && !Validador.esFechaValida("2026-02-30"));
            verificar(FormatoMoneda.mostrar(d("1200000")).contains("1.200.000"));
        });
        caso("Configuracion local conserva comillas y caracteres de la clave", () -> {
            Path archivo = Files.createTempFile("configuracion_poo_", ".env");
            try {
                Files.writeString(archivo, "# Ejemplo\nPOO_VALOR_PRUEBA=\"A#b=c\"\nexport POO_OTRO_PRUEBA=123\n");
                ConfiguracionEntorno config = new ConfiguracionEntorno(archivo);
                verificar(config.get("POO_VALOR_PRUEBA").equals("A#b=c"));
                verificar(config.get("POO_OTRO_PRUEBA").equals("123"));
            } finally {
                Files.delete(archivo);
            }
        });
        final Exception[] fallo = new Exception[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                Usuario admin = new Administrador(1, "Ada", "ada", "hash_prueba", true, LocalDateTime.now());
                Producto producto = new Producto(1, "Cafe", d("10000"), d("6000"), 100, 2);
                caso("Login e inicializacion tienen formularios reales", () -> {
                    imagen(new FrmLogin(), "login");
                    imagen(new FrmInicializacion(), "inicializacion");
                });
                caso("Menu y semaforo presentan estado y avance", () -> {
                    FrmPrincipal principal = new FrmPrincipal(admin);
                    principal.mostrarResumenSemaforo(new ResumenSemaforo(new EstadoAmarillo(), "Las ventas se acercan a la meta.", d("87.18"), true));
                    imagen(principal, "principal");
                });
                caso("Venta presenta catalogo y detalles con total del modelo", () -> {
                    FrmVenta v = new FrmVenta();
                    v.mostrarProductos(Arrays.asList(producto));
                    Venta venta = new Venta(admin);
                    venta.agregarDetalle(producto, 2);
                    v.mostrarVenta(venta);
                    imagen(v, "venta");
                });
                caso("Compras presenta selector y detalles", () -> {
                    FrmCompraGasto v = new FrmCompraGasto();
                    v.mostrarProductos(Arrays.asList(producto));
                    Compra compra = new Compra(admin, "Proveedor");
                    compra.agregarDetalle(producto, 3, d("6000"));
                    v.mostrarCompra(compra);
                    imagen(v, "compra");
                });
                caso("Balance muestra las cifras de la guia sin recalcularlas", () -> {
                    FrmBalance v = new FrmBalance();
                    v.mostrarBalance(new BalanceDia(LocalDate.now(), d("850000"), d("510000"), d("340000"), d("390000"),
                            d("-50000"), d("975000"), d("125000"), d("87.18")));
                    imagen(v, "balance");
                });
                caso("Administracion muestra usuarios sin hashes y catalogos", () -> {
                    FrmUsuarios usuarios = new FrmUsuarios();
                    usuarios.mostrarUsuarios(Arrays.asList(admin));
                    imagen(usuarios, "usuarios");
                    FrmProductos productos = new FrmProductos();
                    productos.mostrarProductos(Arrays.asList(producto));
                    imagen(productos, "productos");
                    FrmCostosFijos costos = new FrmCostosFijos();
                    costos.mostrarCostos(Arrays.asList(new CostoFijo(1, "Arriendo", d("390000"), true)));
                    imagen(costos, "costos");
                });
                caso("Todas las demas pantallas pueden construirse", () -> {
                    imagen(new FrmConfiguracion(), "configuracion");
                    imagen(new FrmReportes(), "reportes");
                    imagen(new FrmHistorial(), "historial");
                    imagen(new FrmMargen(), "margen");
                    imagen(new FrmModificarVenta(), "modificar");
                    imagen(new FrmCierreCaja(), "cierre");
                });
                caso("Modificar exige consultar y descarta la venta anterior al cambiar numero", () -> {
                    FrmModificarVenta v = new FrmModificarVenta();
                    try {
                        JButton guardar = componente(v, JButton.class, "Guardar ajuste");
                        verificar(!guardar.isEnabled());
                        campo(v, "Número de venta:").setText("7");
                        ServicioVentas servicio = new ServicioVentas() {
                            @Override public Venta buscarVenta(int id) {
                                verificar(id == 7);
                                Venta venta = new Venta(admin);
                                venta.setId(id);
                                venta.agregarDetalle(producto, 2);
                                return venta;
                            }
                        };
                        EjecutorTareas inmediato = new EjecutorTareas() {
                            public <T> void ejecutar(Operacion<T> tarea, Consumer<T> exito, Consumer<Exception> error) {
                                try { exito.accept(tarea.ejecutar()); }
                                catch (Exception e) { throw new AssertionError(e); }
                            }
                        };
                        v.conectar(new ModificarVentaControlador(v, servicio, inmediato));
                        componente(v, JButton.class, "Buscar venta").doClick();
                        verificar(guardar.isEnabled() && v.getNuevoTotal().equals("20000"));
                        v.setOcupada(true);
                        verificar(!guardar.isEnabled() && !campo(v, "Número de venta:").isEnabled());
                        v.setOcupada(false);
                        verificar(guardar.isEnabled());
                        imagen(v, "modificar-consultada");
                        campo(v, "Número de venta:").setText("8");
                        verificar(!guardar.isEnabled() && v.getNuevoTotal().isEmpty());
                    } finally { v.cerrar(); }
                });
                caso("Seleccion administrativa respeta orden de tabla y bloquea stock existente", () -> {
                    FrmProductos v = new FrmProductos();
                    try {
                        Producto otro = new Producto(2, "Arroz", d("5000"), d("4000"), 20, 3);
                        v.mostrarProductos(Arrays.asList(producto, otro));
                        JScrollPane scroll = (JScrollPane)((BorderLayout)v.cuerpo.getLayout()).getLayoutComponent(BorderLayout.CENTER);
                        JTable tabla = (JTable)scroll.getViewport().getView();
                        tabla.getRowSorter().toggleSortOrder(1);
                        tabla.setRowSelectionInterval(0, 0);
                        verificar(v.getIdSeleccionado() == 2 && v.getNombre().equals("Arroz"));
                        verificar(!campo(v, "Stock inicial / actual:").isEditable());
                        componente(v, JButton.class, "Limpiar").doClick();
                        verificar(v.getIdSeleccionado() == null && campo(v, "Stock inicial / actual:").isEditable());
                    } finally { v.cerrar(); }
                });
                caso("Cada ventana concreta acepta los eventos de su controlador", () -> {
                    EjecutorTareas inmediato = new EjecutorTareas() {
                        public <T> void ejecutar(Operacion<T> tarea, Consumer<T> exito, Consumer<Exception> error) {
                            try {
                                exito.accept(tarea.ejecutar());
                            } catch (Exception e) {
                                error.accept(e);
                            }
                        }
                    };
                    Navegacion navegacion = new Navegacion() {
                        public void abrirMenu(Usuario usuario) {
                        }

                        public void abrir(Pantalla pantalla) {
                        }

                        public void volverAlLogin() {
                        }
                    };
                    java.util.List<FrmBase> creadas = new java.util.ArrayList<>();
                    try (AplicacionControladores a = new AplicacionControladores(new ServiciosAplicacion(), inmediato)) {
                        FrmLogin login = new FrmLogin();
                        creadas.add(login);
                        a.crearLogin(login, navegacion);
                        FrmPrincipal principal = new FrmPrincipal(new Cajero(1, "Ana", "ana", "hash_prueba", true, LocalDateTime.now()));
                        creadas.add(principal);
                        a.crearPrincipal(principal, navegacion);
                        FrmVenta venta = new FrmVenta();
                        creadas.add(venta);
                        a.crearVenta(venta);
                        FrmCompraGasto compra = new FrmCompraGasto();
                        creadas.add(compra);
                        a.crearCompraGasto(compra);
                        FrmBalance balance = new FrmBalance();
                        creadas.add(balance);
                        a.crearBalance(balance);
                        FrmMargen margen = new FrmMargen();
                        creadas.add(margen);
                        a.crearMargen(margen);
                        FrmHistorial historial = new FrmHistorial();
                        creadas.add(historial);
                        a.crearHistorial(historial);
                        FrmModificarVenta modificar = new FrmModificarVenta();
                        creadas.add(modificar);
                        a.crearModificarVenta(modificar);
                        FrmCierreCaja cierre = new FrmCierreCaja();
                        creadas.add(cierre);
                        a.crearCierre(cierre);
                        FrmUsuarios usuarios = new FrmUsuarios();
                        creadas.add(usuarios);
                        a.crearUsuario(usuarios);
                        FrmProductos productos = new FrmProductos();
                        creadas.add(productos);
                        a.crearProducto(productos);
                        FrmCostosFijos costos = new FrmCostosFijos();
                        creadas.add(costos);
                        a.crearCostoFijo(costos);
                        FrmConfiguracion config = new FrmConfiguracion();
                        creadas.add(config);
                        a.crearConfiguracion(config);
                        FrmInicializacion inicio = new FrmInicializacion();
                        creadas.add(inicio);
                        a.crearInicializacion(inicio, navegacion);
                    } finally {
                        for (FrmBase frame : creadas) frame.cerrar();
                    }
                });
            } catch (Exception e) {
                fallo[0] = e;
            }
        });
        if (fallo[0] != null) throw fallo[0];
        System.out.println("RESULTADO VENTANAS Y UTILIDADES: " + correctas + " correctas");
    }
}
