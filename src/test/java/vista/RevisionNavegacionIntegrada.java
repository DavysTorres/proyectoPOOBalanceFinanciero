package vista;

import controlador.*;
import modelo.*;
import servicio.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

/** Comprueba alojamiento, conservacion de formularios y bloqueo sin acceder a MySQL. */
public final class RevisionNavegacionIntegrada {
    private static void verificar(boolean valor) {
        if (!valor) throw new AssertionError("Fallo de navegacion integrada");
    }
    private static JButton boton(Container padre, String texto) {
        for (Component c : padre.getComponents()) {
            if (c instanceof JButton b && texto.equals(b.getText())) return b;
            if (c instanceof Container contenedor) {
                JButton resultado = boton(contenedor, texto);
                if (resultado != null) return resultado;
            }
        }
        return null;
    }
    private static void imagen(FrmPrincipal principal, String nombre) throws Exception {
        principal.validate();
        Container contenido = principal.getContentPane();
        BufferedImage imagen = new BufferedImage(contenido.getWidth(), contenido.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imagen.createGraphics();
        contenido.printAll(g);
        g.dispose();
        Path destino = Path.of("target/revision-navegacion-integrada");
        Files.createDirectories(destino);
        ImageIO.write(imagen, "png", destino.resolve(nombre + ".png").toFile());
    }
    private static JTable primeraTabla(Container padre) {
        for (Component c : padre.getComponents()) {
            if (c instanceof JTable t) return t;
            if (c instanceof Container contenedor) {
                JTable t = primeraTabla(contenedor);
                if (t != null) return t;
            }
        }
        return null;
    }
    private static void verificarCampos(Container padre) {
        if (padre.getLayout() instanceof FormularioZeroLoss.Campos) {
            for (Component c : padre.getComponents()) {
                verificar(c.getWidth() > 0 && c.getHeight() > 0);
                verificar(c.getX() >= 0 && c.getY() >= 0);
                verificar(c.getX() + c.getWidth() <= padre.getWidth() + 1);
                verificar(c.getY() + c.getHeight() <= padre.getHeight() + 1);
                if (c instanceof JTextField) verificar(c.getHeight() <= 40);
            }
        }
        for (Component c : padre.getComponents()) if (c.isVisible() && c instanceof Container contenedor)
            verificarCampos(contenedor);
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Usuario admin = new Administrador(1, "Ada", "ada", "hash_prueba", true, LocalDateTime.now());
            FrmPrincipal principal = new FrmPrincipal(admin);
            List<FrmBase> modulos = List.of(new FrmVenta(), new FrmCompraGasto(), new FrmBalance(),
                    new FrmReportes(), new FrmMargen(), new FrmHistorial(), new FrmModificarVenta(),
                    new FrmCierreCaja(), new FrmUsuarios(), new FrmProductos(), new FrmCostosFijos(), new FrmConfiguracion());
            try {
                Sesion.getInstancia().iniciar(admin);
                principal.addNotify();
                principal.validate();
                java.util.List<Producto> catalogo = new java.util.ArrayList<>();
                for (int i = 1; i <= 18; i++) catalogo.add(new Producto(i, "Producto de ejemplo " + i,
                        new java.math.BigDecimal("11500"), new java.math.BigDecimal("7000"), 35, 3));
                ((FrmVenta)modulos.get(0)).mostrarProductos(catalogo);
                Venta carrito = new Venta(admin);
                for (Producto p : catalogo) carrito.agregarDetalle(p, 1);
                ((FrmVenta)modulos.get(0)).mostrarVenta(carrito);
                long visibles = java.util.Arrays.stream(Window.getWindows()).filter(Window::isVisible).count();
                for (FrmBase modulo : modulos) {
                    Container contenido = modulo.getContentPane();
                    principal.mostrarModulo(modulo);
                    verificar(principal.getModuloActual() == modulo);
                    verificar(SwingUtilities.isDescendingFrom(contenido, principal));
                    verificar(!modulo.isVisible() && !modulo.isDisplayable());
                    principal.setSize(1000, 650);
                    imagen(principal, modulo.getClass().getSimpleName() + "-compacto");
                    verificarCampos(principal.getContentPane());
                    principal.setSize(1600, 950);
                    imagen(principal, modulo.getClass().getSimpleName() + "-amplio");
                    verificarCampos(principal.getContentPane());
                    if (modulo instanceof FrmVenta) {
                        JTable tabla = primeraTabla(contenido);
                        // Reserva al menos ocho renglones tras el buscador y el area de cobro.
                        verificar(tabla.getParent().getHeight() >= tabla.getRowHeight() * 8);
                        verificar(tabla.getRowCount() == 18);
                        verificar(contenido.getWidth() >= (principal.getContentPane().getWidth() - 238) * 0.9);
                    }
                    principal.setSize(1150, 700);
                    verificar(java.util.Arrays.stream(Window.getWindows()).filter(Window::isVisible).count() == visibles);
                    principal.mostrarInicio();
                    verificar(principal.getModuloActual() == null);
                    principal.mostrarModulo(modulo);
                    verificar(SwingUtilities.isDescendingFrom(contenido, principal));
                }
                FrmVenta venta = (FrmVenta)modulos.get(0);
                venta.campos.getComponents();
                ((JTextField)((JLabel)venta.campos.getComponent(0)).getLabelFor()).setText("Cafe pendiente");
                principal.mostrarModulo(venta);
                verificar(Boolean.TRUE.equals(principal.getBtnVentas().getClientProperty("activo")));
                principal.mostrarModulo(modulos.get(8));
                verificar(Boolean.FALSE.equals(principal.getBtnVentas().getClientProperty("activo")));
                principal.mostrarModulo(venta);
                verificar(venta.getFiltro().equals("Cafe pendiente"));
                final int[] nuevas = {0};
                venta.alAccionar(vista.contratos.AccionVista.NUEVA_VENTA, () -> nuevas[0]++);
                javax.swing.Action nueva = venta.cuerpo.getActionMap().get("Nueva venta");
                nueva.actionPerformed(new java.awt.event.ActionEvent(venta, 0, "F2"));
                verificar(nuevas[0] == 0);
                venta.setOcupada(true);
                nueva.actionPerformed(new java.awt.event.ActionEvent(venta, 0, "F2"));
                verificar(nuevas[0] == 0);
                verificar(!principal.getBtnVentas().isEnabled());
                verificar(!boton(principal, "Cerrar sesi" + (char)243 + "n").isEnabled());
                principal.mostrarInicio();
                verificar(principal.getModuloActual() == venta);
                principal.mostrarModulo(modulos.get(8));
                verificar(principal.getModuloActual() == venta);
                principal.setOcupada(true);
                venta.setOcupada(false);
                verificar(!principal.getBtnVentas().isEnabled());
                principal.setOcupada(false);
                verificar(principal.getBtnVentas().isEnabled());
                final boolean[] cerrado = {false};
                venta.alCerrar(() -> { cerrado[0] = true; principal.retirarModulo(venta); venta.cerrar(); });
                boton(principal, "\u2190").doClick();
                verificar(cerrado[0] && principal.getModuloActual() == null);
                // La navegacion del controlador entrega la vista antes de finalizar su estado ocupado.
                EjecutorTareas inmediato = new EjecutorTareas() {
                    public <T> void ejecutar(Operacion<T> op, Consumer<T> ok, Consumer<Exception> error) {
                        try { ok.accept(op.ejecutar()); } catch (Exception e) { error.accept(e); }
                    }
                };
                Navegacion navegacion = new Navegacion() {
                    public void abrirMenu(Usuario usuario) { }
                    public void volverAlLogin() { }
                    public void abrir(Pantalla destino) { principal.mostrarModulo(modulos.get(8)); }
                };
                PrincipalControlador controlador = new PrincipalControlador(principal, new ServiciosAplicacion(), navegacion, inmediato);
                principal.conectar(controlador);
                controlador.abrirUsuarios();
                verificar(principal.getModuloActual() == modulos.get(8));
                verificar(principal.getBtnVentas().isEnabled());
                imagen(principal, "usuarios-integrado");
                principal.mostrarModulo(modulos.get(1));
                imagen(principal, "compras-integrado");
                boton(principal, "Resumen del d" + (char)237 + "a").doClick();
                verificar(principal.getModuloActual() == null);
                verificar(Boolean.TRUE.equals(boton(principal, "Resumen del d" + (char)237 + "a").getClientProperty("activo")));
            } catch (Exception e) { throw new RuntimeException(e); }
            finally {
                modulos.forEach(FrmBase::cerrar);
                principal.cerrar();
                Sesion.getInstancia().cerrar();
            }
        });
        System.out.println("OK: 12 modulos integrados, borradores, bloqueo, volver y dashboard sin ventanas adicionales");
    }
}
