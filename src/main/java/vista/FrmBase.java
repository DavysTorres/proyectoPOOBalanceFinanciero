package vista;

import controlador.ControladorBase;
import util.Mensajes;
import vista.contratos.*;

import javax.swing.*;
import java.awt.Cursor;
import java.awt.event.*;
import java.util.*;

/** Soporte compartido de eventos, tareas y cierre de los formularios. */
public abstract class FrmBase extends JFrame implements VistaBase {
    private final Map<AccionVista, JButton> botones = new EnumMap<>(AccionVista.class);
    private boolean ocupada;
    private boolean presentacionPreparada;
    private java.awt.Component padreDialogos = this;
    private java.util.function.Consumer<Boolean> alOcupar = valor -> { };
    private Runnable alCerrar = this::cerrar;
    private ControladorBase<?> controlador;

    protected FrmBase(String titulo) {
        super(titulo);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                solicitarCierre();
            }
        });
    }

    protected final void registrarBoton(AccionVista accion, JButton boton) {
        if (botones.putIfAbsent(accion, boton) != null)
            throw new IllegalArgumentException("Accion ya registrada: " + accion);
    }

    @Override public final void alAccionar(AccionVista accion, Runnable manejador) {
        JButton boton = Objects.requireNonNull(botones.get(accion), "Accion sin boton: " + accion);
        boton.addActionListener(e -> { if (!ocupada) manejador.run(); });
    }

    protected final void accionar(AccionVista accion) {
        JButton boton = botones.get(accion);
        if (!ocupada && boton != null && boton.isEnabled()) boton.doClick();
    }

    public final void conectar(ControladorBase<?> controlador) {
        this.controlador = Objects.requireNonNull(controlador);
    }

    public final void alCerrar(Runnable accion) { alCerrar = Objects.requireNonNull(accion); }
    public final void solicitarCierre() { if (!ocupada) alCerrar.run(); }
    public final void alOcupar(java.util.function.Consumer<Boolean> accion) { alOcupar = Objects.requireNonNull(accion); }
    public final java.awt.Container extraerContenido(java.awt.Component anfitrion) {
        prepararPresentacion();
        java.awt.Container contenido = getContentPane();
        setContentPane(new JPanel());
        padreDialogos = anfitrion;
        return contenido;
    }
    protected final java.awt.Component padreDialogos() { return padreDialogos; }
    public final void mostrar() {
        prepararPresentacion();
        java.awt.Rectangle area = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        java.awt.Dimension minimo = getMinimumSize();
        setMinimumSize(new java.awt.Dimension(Math.min(minimo.width, area.width - 24), Math.min(minimo.height, area.height - 24)));
        setSize(Math.min(getWidth(), area.width - 24), Math.min(getHeight(), area.height - 24));
        setLocationRelativeTo(null);
        setVisible(true);
    }
    protected boolean usaTemaFormulario() { return true; }
    private void prepararPresentacion() {
        if (presentacionPreparada || !usaTemaFormulario()) return;
        presentacionPreparada = true;
        setContentPane(FormularioZeroLoss.envolver(getContentPane()));
    }
    @Override public void addNotify() { prepararPresentacion(); super.addNotify(); }
    public final void cerrar() {
        if (controlador != null) controlador.desvincular();
        dispose();
    }
    public final boolean estaOcupada() { return ocupada; }

    @Override public void setOcupada(boolean valor) {
        ocupada = valor;
        botones.values().forEach(b -> b.setEnabled(!valor));
        setCursor(Cursor.getPredefinedCursor(valor ? Cursor.WAIT_CURSOR : Cursor.DEFAULT_CURSOR));
        alOcupar.accept(valor);
    }
    @Override public void mostrarError(String mensaje) { Mensajes.error(padreDialogos, mensaje); }
    @Override public void mostrarInformacion(String mensaje) { Mensajes.informacion(padreDialogos, mensaje); }
}
