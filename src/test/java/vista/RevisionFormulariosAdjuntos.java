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
import java.time.*;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

/** Verifica integración Swing/controladores sin conexiones ni escrituras en MySQL. */
public final class RevisionFormulariosAdjuntos {
    private static final Path SALIDA = Path.of("target/revision-formularios-adjuntos");
    private static final EjecutorTareas INMEDIATO = new EjecutorTareas() {
        public <T> void ejecutar(Operacion<T> tarea, Consumer<T> exito, Consumer<Exception> error) {
            try { exito.accept(tarea.ejecutar()); }
            catch (Exception e) { error.accept(e); }
        }
    };

    private static void verificar(boolean valor) {
        if (!valor) throw new AssertionError("Falló la revisión de formularios");
    }

    private static <T extends Component> List<T> componentes(Container padre, Class<T> tipo) {
        List<T> encontrados = new ArrayList<>();
        for (Component c : padre.getComponents()) {
            if (tipo.isInstance(c)) encontrados.add(tipo.cast(c));
            if (c instanceof Container) encontrados.addAll(componentes((Container)c, tipo));
        }
        return encontrados;
    }

    private static JButton boton(Container c, String texto) {
        return componentes(c, JButton.class).stream().filter(b -> texto.equals(b.getText())).findFirst().orElseThrow();
    }

    private static JTextField campo(Container c, String etiqueta) {
        JLabel label = componentes(c, JLabel.class).stream().filter(l -> etiqueta.equals(l.getText())).findFirst().orElseThrow();
        return (JTextField) label.getLabelFor();
    }

    private static void imagen(FrmBase ventana, String nombre) throws Exception {
        ventana.addNotify();
        ventana.validate();
        Container c = ventana.getContentPane();
        BufferedImage img = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        c.printAll(g);
        g.dispose();
        ImageIO.write(img, "png", SALIDA.resolve(nombre + ".png").toFile());
    }

    public static void main(String[] args) throws Exception {
        Files.createDirectories(SALIDA);
        SwingUtilities.invokeAndWait(() -> {
            Usuario admin = new Administrador(1, "Ada", "ada", "hash_prueba", true, LocalDateTime.now());
            Producto cafe = new Producto(1, "Café", new BigDecimal("10000"), new BigDecimal("6000"), 10, 2);
            Producto pan = new Producto(2, "Pan", new BigDecimal("5000"), new BigDecimal("4500"), 10, 2);
            ServicioProductos productos = new ServicioProductos() {
                @Override public List<Producto> buscarProductos(String filtro) { return List.of(cafe, pan); }
                @Override public List<Producto> listarProductosActivos() { return List.of(pan, cafe); }
            };
            FrmCompraGasto compra = new FrmCompraGasto();
            FrmMargen margen = new FrmMargen();
            FrmPrincipal principal = new FrmPrincipal(admin);
            try {
                Sesion.getInstancia().iniciar(admin);
                CompraGastoControlador cc = new CompraGastoControlador(compra, new ServicioCompras(), productos, INMEDIATO);
                compra.conectar(cc);
                cc.cargarProductos();
                verificar(compra.getProductoSeleccionado() == cafe);
                campo(compra, "Proveedor:").setText("Proveedor de prueba");
                campo(compra, "Cantidad:").setText("2");
                boton(compra, "+ Agregar línea").doClick();
                JTable lineas = componentes(compra, JTable.class).stream().filter(t -> t.getColumnCount() == 4
                        && t.getColumnName(0).equals("Producto")).findFirst().orElseThrow();
                verificar(lineas.getRowCount() == 1 && lineas.getValueAt(0, 1).equals(2));
                verificar(!lineas.isCellEditable(0, 0));
                verificar(lineas.getValueAt(0, 3).equals(util.FormatoMoneda.mostrar(new BigDecimal("12000"))));
                verificar(!campo(compra, "Fecha (día actual):").isEditable());
                JComboBox<?> tipo = componentes(compra, JComboBox.class).stream()
                        .filter(c -> c.getItemCount() > 0 && "Gasto Fijo".equals(c.getItemAt(0))).findFirst().orElseThrow();
                tipo.setSelectedIndex(1);
                verificar(!compra.esGastoFijo() && !campo(compra, "Días de operación:").isEnabled());
                imagen(compra, "compras");
                componentes(compra, JTabbedPane.class).get(0).setSelectedIndex(1);
                imagen(compra, "gastos");
                boton(compra, "Cancelar compra").doClick();
                verificar(lineas.getRowCount() == 0);

                MargenControlador mc = new MargenControlador(margen, new AnalizadorMargen(), productos, INMEDIATO);
                margen.conectar(mc);
                mc.cargarRanking();
                JList<?> lista = componentes(margen, JList.class).stream().filter(l -> l.getModel().getSize() > 0).findFirst().orElseThrow();
                verificar(lista.getModel().getElementAt(0) == cafe);
                lista.setSelectedIndex(0);
                boton(margen, "Ver consejo").doClick();
                verificar(componentes(margen, JTextArea.class).get(0).getText().contains("Café: aporta 4000"));
                imagen(margen, "margen");
                margen.mostrarRanking(List.of());
                verificar(margen.getProductoSeleccionado() == null);

                List<Pantalla> destinos = new ArrayList<>();
                Navegacion navegacion = new Navegacion() {
                    public void abrirMenu(Usuario u) { }
                    public void abrir(Pantalla p) { destinos.add(p); }
                    public void volverAlLogin() { }
                };
                PrincipalControlador pc = new PrincipalControlador(principal, new ServiciosAplicacion(), navegacion, INMEDIATO);
                principal.conectar(pc);
                principal.getBtnVentas().doClick();
                verificar(destinos.equals(List.of(Pantalla.VENTA)));
                principal.getBtnAdministracion().doClick();
                FrmAdministracion panel = (FrmAdministracion) principal.getEscritorioPrincipal().getAllFrames()[0];
                verificar(!panel.isVisible());
                boton(principal, "Usuarios y permisos").doClick();
                verificar(destinos.get(1) == Pantalla.USUARIOS);
                principal.mostrarResumenSemaforo(new ResumenSemaforo(new EstadoAmarillo(), "Las ventas se acercan a la meta.", new BigDecimal("87.18"), true));
                verificar(componentes(principal.getPanelSemaforo(), JProgressBar.class).get(0).getString().equals("87.18 %"));
                principal.mostrarBalanceDashboard(new BalanceDia(LocalDate.now(), new BigDecimal("123456"),
                        BigDecimal.ZERO, new BigDecimal("123456"), BigDecimal.ZERO,
                        new BigDecimal("123456"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("100")));
                verificar(componentes(principal, JLabel.class).stream().anyMatch(l ->
                        util.FormatoMoneda.mostrar(new BigDecimal("123456")).equals(l.getText())));
                imagen(principal, "principal-administracion");
                principal.setSize(1000, 600);
                imagen(principal, "principal-administracion-compacto");
                principal.setSize(1600, 950);
                imagen(principal, "principal-administracion-amplio");
                principal.setSize(1150, 700);
                principal.getBtnAdministracion().doClick();
                imagen(principal, "principal-dashboard");
                principal.mostrarUsuario(new Jefe(3, "Luis", "luis", "hash_prueba", true, LocalDateTime.now()));
                verificar(principal.getBtnBalance().isVisible() && !principal.getBtnAdministracion().isVisible());
                imagen(principal, "principal-jefe");
                Usuario cajero = new Cajero(2, "Ana", "ana", "hash_prueba", true, LocalDateTime.now());
                principal.mostrarUsuario(cajero);
                verificar(principal.getBtnVentas().isVisible() && !principal.getBtnBalance().isVisible());
                verificar(!principal.getBtnAdministracion().isVisible() && !panel.isVisible());
                verificar(componentes(principal, JLabel.class).stream().noneMatch(l ->
                        util.FormatoMoneda.mostrar(new BigDecimal("123456")).equals(l.getText())));
                verificar(componentes(principal, JButton.class).stream().noneMatch(b ->
                        "Usuarios y permisos".equals(b.getText()) && b.isVisible()));
                principal.mostrarResumenSemaforo(new ResumenSemaforo(new EstadoRojo(), "No hay margen positivo.", BigDecimal.ZERO, false));
                verificar(componentes(principal.getPanelSemaforo(), JProgressBar.class).get(0).getString().equals("No calculable"));
                imagen(principal, "principal-cajero");
                principal.mostrarUsuario(admin);
                principal.mostrarBalanceDashboard(new BalanceDia(LocalDate.now(), new BigDecimal("123456"),
                        BigDecimal.ZERO, new BigDecimal("123456"), BigDecimal.ZERO,
                        new BigDecimal("123456"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("100")));
                principal.iniciarCargaResumen();
                verificar(componentes(principal, JLabel.class).stream().noneMatch(l ->
                        util.FormatoMoneda.mostrar(new BigDecimal("123456")).equals(l.getText())));
                principal.mostrarFalloResumen();
                verificar(componentes(principal.getPanelSemaforo(), JProgressBar.class).get(0).getString().equals("Sin datos"));
                System.out.println("OK: compras, gastos, ranking, consejos, navegación, administración, permisos y semáforo");
            } catch (Exception e) { throw new RuntimeException(e); }
            finally {
                compra.cerrar(); margen.cerrar(); principal.cerrar();
                Sesion.getInstancia().cerrar();
            }
        });
    }
}
