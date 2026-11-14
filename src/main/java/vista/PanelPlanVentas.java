package vista;

import modelo.BalanceDia;
import servicio.PlanVentas;
import util.FormatoMoneda;
import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/** Herramienta de planificacion separada de los resultados reales de la jornada. */
final class PanelPlanVentas extends JPanel {
    private BalanceDia balance;
    private final JTextField margen = new JTextField(8);
    private final JTextField ganancia = new JTextField("0", 10);
    private final JButton calcular = new JButton("Calcular mi objetivo");
    private final JTextArea respuesta = texto("Ingresa tu margen previsto y la ganancia que quieres obtener hoy.", 3);
    private final JLabel supuestos = new JLabel();
    PanelPlanVentas() {
        super(new BorderLayout(0, 12));
        setOpaque(false);
        JLabel titulo = new JLabel("\u00bfCu\u00e1nto vender para ganar lo que quiero?");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(TemaZeroLoss.TINTA);
        JPanel cabecera = new JPanel(new BorderLayout(0, 7));
        cabecera.setOpaque(false);
        cabecera.add(titulo, BorderLayout.NORTH);
        JTextArea ayuda = texto("Simulador del d\u00eda. Margen: lo que queda de cada venta tras pagar el producto vendido y los gastos variables. "
                + "Si de $100 quedan $30, escribe 30 %. La ganancia se obtiene despu\u00e9s de cubrir tambi\u00e9n los costos fijos.", 3);
        ayuda.setForeground(TemaZeroLoss.SUAVE);
        ayuda.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cabecera.add(ayuda, BorderLayout.SOUTH);
        add(cabecera, BorderLayout.NORTH);
        JPanel campos = new JPanel(new FormularioZeroLoss.Campos());
        campos.setOpaque(false);
        EstiloZeroLoss.campo(campos, "Margen previsto (%)", margen);
        EstiloZeroLoss.campo(campos, "Ganancia deseada hoy ($)", ganancia);
        for (JTextField campo : new JTextField[]{margen, ganancia}) {
            campo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            campo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(214, 223, 232)),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            campo.addActionListener(e -> calcular.doClick());
            campo.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                private void invalidar() { respuesta.setText("Los valores cambiaron. Pulsa Calcular mi objetivo."); }
                public void insertUpdate(javax.swing.event.DocumentEvent e) { invalidar(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { invalidar(); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { invalidar(); }
            });
        }
        add(campos, BorderLayout.CENTER);
        JPanel pie = new JPanel(new BorderLayout(0, 8));
        pie.setOpaque(false);
        JPanel acciones = new JPanel(new FormularioZeroLoss.Acciones(FlowLayout.LEFT));
        acciones.setOpaque(false);
        TemaZeroLoss.boton(calcular, TemaZeroLoss.TipoBoton.PRINCIPAL);
        calcular.addActionListener(e -> calcular());
        acciones.add(calcular);
        pie.add(acciones, BorderLayout.NORTH);
        respuesta.setFont(new Font("Segoe UI", Font.BOLD, 14));
        respuesta.setForeground(TemaZeroLoss.ACENTO);
        pie.add(respuesta, BorderLayout.CENTER);
        supuestos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        supuestos.setForeground(TemaZeroLoss.SUAVE);
        pie.add(supuestos, BorderLayout.SOUTH);
        add(pie, BorderLayout.SOUTH);
        mostrarBalance(null);
    }
    private static JTextArea texto(String contenido, int filas) {
        JTextArea area = new JTextArea(contenido, filas, 35);
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }
    void mostrarBalance(BalanceDia valor) {
        balance = valor;
        calcular.setEnabled(valor != null);
        respuesta.setText(valor == null ? "Actualiza el resumen para usar el simulador." : "Ingresa tus supuestos y calcula tu objetivo de ventas.");
        supuestos.setText(valor == null ? " " : "<html>Usa " + FormatoMoneda.mostrar(valor.getCostosFijosDia())
                + " de costos fijos diarios registrados. Es una simulaci\u00f3n; no cambia el sem\u00e1foro ni los datos reales.</html>");
    }
    void limpiar() { margen.setText(""); ganancia.setText("0"); mostrarBalance(null); }
    private BigDecimal numero(String valor) {
        if (!valor.trim().matches("[0-9]+([.,][0-9]+)?")) throw new IllegalArgumentException("Escribe montos sin separadores de miles y porcentajes positivos.");
        return new BigDecimal(valor.trim().replace(',', '.'));
    }
    private void calcular() {
        if (balance == null) return;
        try {
            BigDecimal porcentaje = numero(margen.getText()), utilidad = numero(ganancia.getText());
            BigDecimal equilibrio = PlanVentas.ventasNecesarias(balance.getCostosFijosDia(), porcentaje, BigDecimal.ZERO);
            BigDecimal objetivo = PlanVentas.ventasNecesarias(balance.getCostosFijosDia(), porcentaje, utilidad);
            respuesta.setText("Para cubrir costos: " + FormatoMoneda.mostrar(equilibrio) + " en ventas. Para ganar "
                    + FormatoMoneda.mostrar(utilidad) + ": vende " + FormatoMoneda.mostrar(objetivo)
                    + " en total hoy.\nSe cumple si mantienes el margen previsto de " + porcentaje.toPlainString() + " % y registras todos tus costos.");
        } catch (IllegalArgumentException e) { respuesta.setText(e.getMessage()); }
        revalidate();
    }
}
