package vista;

import modelo.*;
import servicio.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.*;

/** Escenarios de propietario y simulaciones aisladas; no usa MySQL. */
public final class RevisionRentabilidad {
    private static BigDecimal d(String n) { return new BigDecimal(n); }
    private static void exigir(boolean valor) { if (!valor) throw new AssertionError("Rentabilidad incorrecta"); }
    private static BalanceDia balance(String ventas, String variables) {
        BigDecimal v = d(ventas), cv = d(variables), fijos = d("100000");
        CalculadoraFinanciera c = new CalculadoraFinanciera();
        BigDecimal margen = c.calcularMargenContribucion(v, cv), razon = c.calcularRazonMargen(margen, v);
        boolean calculable = v.signum() > 0 && razon.signum() > 0;
        BigDecimal meta = calculable ? c.calcularPuntoEquilibrio(fijos, razon) : BigDecimal.ZERO;
        return new BalanceDia(LocalDate.now(), v, cv, margen, fijos, margen.subtract(fijos), meta,
                calculable ? c.calcularFaltante(meta, v) : BigDecimal.ZERO,
                calculable ? c.calcularPorcentajeAvance(v, meta) : BigDecimal.ZERO, calculable, calculable);
    }
    private static <T extends Component> T buscar(Container p, Class<T> tipo, String texto) {
        for (Component c : p.getComponents()) {
            String contenido = c instanceof JLabel l ? l.getText() : c instanceof JButton b ? b.getText() : c instanceof JTextArea a ? a.getText() : "";
            if (tipo.isInstance(c) && contenido.contains(texto)) return tipo.cast(c);
            if (c instanceof Container contenedor) {
                T encontrado = buscar(contenedor, tipo, texto);
                if (encontrado != null) return encontrado;
            }
        }
        return null;
    }
    private static void imagen(FrmPrincipal principal, String nombre) throws Exception {
        principal.validate();
        Container p = principal.getContentPane();
        BufferedImage imagen = new BufferedImage(p.getWidth(), p.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imagen.createGraphics(); p.printAll(g); g.dispose();
        Path salida = Path.of("target/revision-rentabilidad"); Files.createDirectories(salida);
        ImageIO.write(imagen, "png", salida.resolve(nombre + ".png").toFile());
    }
    public static void main(String[] args) throws Exception {
        exigir(PlanVentas.ventasNecesarias(d("100000"), d("40"), d("0")).compareTo(d("250000")) == 0);
        exigir(PlanVentas.ventasNecesarias(d("100000"), d("40"), d("50000")).compareTo(d("375000")) == 0);
        exigir(PlanVentas.ventasNecesarias(d("1"), d("30"), d("0")).compareTo(d("3.34")) == 0);
        for (String n : new String[]{"0", "-1", "101"}) {
            try { PlanVentas.ventasNecesarias(d("1"), d(n), d("0")); throw new AssertionError("Margen invalido aceptado"); }
            catch (IllegalArgumentException esperado) { }
        }
        SwingUtilities.invokeAndWait(() -> {
            FrmPrincipal principal = new FrmPrincipal(new Administrador(1, "Propietario", "admin", "hash", true, LocalDateTime.now()));
            principal.setSize(1600, 1050);
            try {
                principal.addNotify();
                Negocio negocio = new Negocio("Tienda", 30, LocalTime.of(8,0), LocalTime.of(18,0));
                String[][] casos = {{"sin-ventas", "0", "0"}, {"falta-vender", "150000", "90000"},
                        {"equilibrio", "250000", "150000"}, {"ganancia", "500000", "300000"}, {"sin-margen", "100000", "120000"}};
                for (String[] caso : casos) {
                    BalanceDia b = balance(caso[1], caso[2]);
                    EstadoNegocio e = new Diagnostico().evaluar(b, negocio);
                    principal.mostrarResumenSemaforo(new ResumenSemaforo(e, e.getMensaje(b), b.getPorcentajeAvance(), b.isPuntoEquilibrioCalculable()));
                    principal.mostrarBalanceDashboard(b);
                    imagen(principal, caso[0]);
                }
                BalanceDia sinVentas = balance("0", "0");
                EstadoNegocio estado = new Diagnostico().evaluar(sinVentas, negocio);
                principal.mostrarResumenSemaforo(new ResumenSemaforo(estado, estado.getMensaje(sinVentas), BigDecimal.ZERO, false));
                principal.mostrarBalanceDashboard(sinVentas);
                JTextField margen = (JTextField)buscar(principal, JLabel.class, "Margen previsto").getLabelFor();
                JTextField ganancia = (JTextField)buscar(principal, JLabel.class, "Ganancia deseada").getLabelFor();
                margen.setText("40"); ganancia.setText("50000");
                JButton calcular = buscar(principal, JButton.class, "Calcular mi objetivo");
                calcular.doClick();
                exigir(buscar(principal, JTextArea.class, "375.000") != null);
                imagen(principal, "plan-antes-de-vender");
                margen.setText("0"); calcular.doClick();
                exigir(buscar(principal, JTextArea.class, "mayor que 0") != null);
                principal.mostrarFalloResumen(); exigir(!calcular.isEnabled());
                principal.mostrarUsuario(new Cajero(2, "Caja", "caja", "hash", true, LocalDateTime.now()));
                exigir(margen.getText().isEmpty());
            } catch (Exception e) { throw new RuntimeException(e); }
            finally { principal.cerrar(); }
        });
        System.out.println("OK: equilibrio, ganancia, margen negativo, simulador sin ventas, redondeo y limpieza por perfil");
    }
}
