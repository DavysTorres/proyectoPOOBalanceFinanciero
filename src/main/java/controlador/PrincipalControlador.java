package controlador;

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

    }
}
