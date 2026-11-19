package controlador;

<<<<<<< HEAD
import javax.swing.JInternalFrame;
import java.awt.Dimension;
import modelo.Usuario;
import modelo.Usuario.Permiso; 
import vista.main.FrmPrincipal;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class PrincipalControlador {
    private final FrmPrincipal vista;
    private final Usuario usuarioActual;

    public PrincipalControlador(FrmPrincipal vista, Usuario usuarioActual) {
        this.vista = vista;
        this.usuarioActual = usuarioActual;
        
        inicializarVista();
        configurarEventos();
    }

    private void inicializarVista() {
        vista.getLblUsuario().setText("Usuario: " + usuarioActual.getNombre() + " (" + usuarioActual.getRol() + ")");
        
        // Uso de polimorfismo y permisos para habilitar botones
        List<Permiso> permisos = usuarioActual.obtenerPermisos();
        
        if (permisos.contains(Permiso.REGISTRAR_VENTA)) vista.getBtnVentas().setVisible(true);
        if (permisos.contains(Permiso.REGISTRAR_COMPRA)) vista.getBtnCompras().setVisible(true);
        if (permisos.contains(Permiso.VER_BALANCE)) vista.getBtnBalance().setVisible(true);
        if (permisos.contains(Permiso.VER_REPORTES)) vista.getBtnMargen().setVisible(true);
        if (permisos.contains(Permiso.VER_HISTORIAL)) vista.getBtnHistorial().setVisible(true);
        if (permisos.contains(Permiso.CERRAR_CAJA)) vista.getBtnCierreCaja().setVisible(true);
        if (permisos.contains(Permiso.CONFIGURAR)) vista.getBtnAdministracion().setVisible(true);
        
        vista.setVisible(true);
    }

    private void configurarEventos() {
        vista.getBtnVentas().addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Abriendo módulo de ventas...");
                // Aquí instanciarás FrmVenta en el futuro
            }
        });
        vista.getBtnAdministracion().addActionListener(e -> abrirModuloAdministracion());
    }
    
    private void abrirModuloAdministracion() {
        // Evitar abrir múltiples ventanas iguales
        JInternalFrame[] framesAbiertos = vista.getEscritorioPrincipal().getAllFrames();
        for (JInternalFrame frame : framesAbiertos) {
            if (frame instanceof vista.main.FrmAdministracion) {
                try {
                    frame.setSelected(true);
                } catch (java.beans.PropertyVetoException ex) {}
                return;
            }
        }

        // Si no está abierta, crea la instancia
        vista.main.FrmAdministracion frmAdmin = new vista.main.FrmAdministracion();
        
        // Agregarla al panel central
        vista.getEscritorioPrincipal().add(frmAdmin);
        
        // Centrar la ventana interna relativa al tamaño del escritorio
        Dimension desktopSize = vista.getEscritorioPrincipal().getSize();
        Dimension jInternalFrameSize = frmAdmin.getSize();
        frmAdmin.setLocation((desktopSize.width - jInternalFrameSize.width) / 2,
                             (desktopSize.height - jInternalFrameSize.height) / 2);                      
        frmAdmin.setVisible(true);

=======
import vista.contratos.*;
import servicio.*;
import modelo.Permiso;

import java.util.Objects;

/**
 * Controla menu, semaforo y cierre de sesion sin cargar ventanas concretas.
 */
public final class PrincipalControlador extends ControladorBase<VistaPrincipal> {
    private final ServiciosAplicacion servicios;
    private final Navegacion navegacion;

    // Los botones se conectan una vez al construir el controlador.
    public PrincipalControlador(VistaPrincipal vista, ServiciosAplicacion servicios, Navegacion navegacion, EjecutorTareas tareas) {
        super(vista, tareas);
        this.servicios = Objects.requireNonNull(servicios);
        this.navegacion = Objects.requireNonNull(navegacion);
        vista.alAccionar(AccionVista.ACTUALIZAR, this::cargarSemaforo);
        vista.alAccionar(AccionVista.CERRAR_SESION, this::cerrarSesion);
        vista.alAccionar(AccionVista.ABRIR_VENTA, this::abrirVenta);
        vista.alAccionar(AccionVista.ABRIR_BALANCE, this::abrirBalance);
        vista.alAccionar(AccionVista.ABRIR_COMPRA_GASTO, this::abrirCompraGasto);
        vista.alAccionar(AccionVista.ABRIR_MARGEN, this::abrirMargen);
        vista.alAccionar(AccionVista.ABRIR_HISTORIAL, this::abrirHistorial);
        vista.alAccionar(AccionVista.ABRIR_MODIFICAR_VENTA, this::abrirModificarVenta);
        vista.alAccionar(AccionVista.ABRIR_CIERRE, this::abrirCierre);
        vista.alAccionar(AccionVista.ABRIR_USUARIOS, this::abrirUsuarios);
        vista.alAccionar(AccionVista.ABRIR_PRODUCTOS, this::abrirProductos);
        vista.alAccionar(AccionVista.ABRIR_COSTOS_FIJOS, this::abrirCostosFijos);
        vista.alAccionar(AccionVista.ABRIR_CONFIGURACION, this::abrirConfiguracion);
        vista.alAccionar(AccionVista.ABRIR_REPORTES, this::abrirReportes);
    }

    // Solo se entrega un estado, para que el cajero no reciba las cifras financieras restringidas.
    public void cargarSemaforo() {
        preparar(() -> {
            vista.iniciarCargaResumen();
            ejecutar(() -> {
                try {
                    var usuario = Sesion.getInstancia().getUsuarioActual();
                    modelo.BalanceDia balance = usuario != null && usuario.tienePermiso(Permiso.VER_BALANCE)
                            ? servicios.getBalance().obtenerBalanceDelDia(java.time.LocalDate.now()) : null;
                    var resumen = balance == null ? servicios.getBalance().obtenerResumenDelDia()
                            : servicios.getBalance().resumirBalance(balance);
                    return new DatosInicio(resumen, balance, null);
                } catch (Exception e) {
                    return new DatosInicio(null, null, e);
                }
            }, datos -> {
                if (datos.error() != null) {
                    vista.mostrarFalloResumen();
                    mostrarError(datos.error());
                    return;
                }
                vista.mostrarUsuario(Sesion.getInstancia().getUsuarioActual());
                vista.mostrarResumenSemaforo(datos.resumen());
                vista.mostrarBalanceDashboard(datos.balance());
            });
        });
    }

    private record DatosInicio(ResumenSemaforo resumen, modelo.BalanceDia balance, Exception error) { }

    public void actualizarSemaforo() {
        cargarSemaforo();
    }

    // Comprobar el permiso al navegar mejora la interfaz; el servicio lo vuelve a exigir al operar.
    private void abrir(Pantalla pantalla, Permiso permiso) {
        ejecutar(() -> {
            Sesion.getInstancia().exigirPermiso(permiso);
            return pantalla;
        }, navegacion::abrir);
    }

    public void abrirVenta() {
        abrir(Pantalla.VENTA, Permiso.REGISTRAR_VENTA);
    }

    public void abrirCompraGasto() {
        abrir(Pantalla.COMPRA_GASTO, Permiso.REGISTRAR_COMPRA);
    }

    public void abrirBalance() {
        abrir(Pantalla.BALANCE, Permiso.VER_BALANCE);
    }

    public void abrirMargen() {
        abrir(Pantalla.MARGEN, Permiso.VER_BALANCE);
    }

    public void abrirHistorial() {
        abrir(Pantalla.HISTORIAL, Permiso.VER_HISTORIAL);
    }

    public void abrirModificarVenta() {
        abrir(Pantalla.MODIFICAR_VENTA, Permiso.MODIFICAR_VENTA);
    }

    public void abrirCierre() {
        abrir(Pantalla.CIERRE, Permiso.CERRAR_CAJA);
    }

    public void abrirUsuarios() {
        abrir(Pantalla.USUARIOS, Permiso.GESTIONAR_USUARIOS);
    }

    public void abrirProductos() {
        abrir(Pantalla.PRODUCTOS, Permiso.GESTIONAR_PRODUCTOS);
    }

    public void abrirCostosFijos() {
        abrir(Pantalla.COSTOS_FIJOS, Permiso.DEFINIR_COSTOS_FIJOS);
    }

    public void abrirConfiguracion() {
        abrir(Pantalla.CONFIGURACION, Permiso.CONFIGURAR);
    }

    public void abrirReportes() {
        abrir(Pantalla.REPORTES, Permiso.VER_REPORTES);
    }

    // El carrito se cancela antes de cerrar la sesion; ninguna cancelacion mueve stock.
    public void cerrarSesion() {
        ejecutar(() -> {
            servicios.cerrarSesion();
            return null;
        }, ignorado -> navegacion.volverAlLogin());
>>>>>>> c6b38029e9d5f9f3c350cb56d301388f5b5859f1
    }
}
