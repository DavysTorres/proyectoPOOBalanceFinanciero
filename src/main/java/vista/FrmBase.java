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

    public final void conectar(ControladorBase<?> controlador) {
        this.controlador = Objects.requireNonNull(controlador);
    }

    public final void alCerrar(Runnable accion) { alCerrar = Objects.requireNonNull(accion); }
    public final void solicitarCierre() { if (!ocupada) alCerrar.run(); }
    public final void alOcupar(java.util.function.Consumer<Boolean> accion) { alOcupar = Objects.requireNonNull(accion); }
    public final java.awt.Container extraerContenido(java.awt.Component anfitrion) {
        java.awt.Container contenido = getContentPane();
        setContentPane(new JPanel());
        padreDialogos = anfitrion;
        return contenido;
    }
    protected final java.awt.Component padreDialogos() { return padreDialogos; }
    public final void mostrar() { setVisible(true); }
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
