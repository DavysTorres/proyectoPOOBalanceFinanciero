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
                long visibles = java.util.Arrays.stream(Window.getWindows()).filter(Window::isVisible).count();
                for (FrmBase modulo : modulos) {
                    Container contenido = modulo.getContentPane();
                    principal.mostrarModulo(modulo);
                    verificar(principal.getModuloActual() == modulo);
                    verificar(SwingUtilities.isDescendingFrom(contenido, principal));
                    verificar(!modulo.isVisible() && !modulo.isDisplayable());
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
                principal.mostrarModulo(modulos.get(8));
                principal.mostrarModulo(venta);
                verificar(venta.getFiltro().equals("Cafe pendiente"));
                venta.setOcupada(true);
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
