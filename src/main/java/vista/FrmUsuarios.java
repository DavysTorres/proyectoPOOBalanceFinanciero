package vista;

import modelo.*;
import vista.contratos.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

public final class FrmUsuarios extends FrmModulo implements VistaUsuarios {
    private final JTextField nombre = texto("Nombre:", "");
    private final JTextField username = texto("Usuario:", "");
    private final JPasswordField clave = campo("Clave nueva (mínimo 8 caracteres):", new JPasswordField());
    private final JComboBox<Rol> rol = campo("Rol:", new JComboBox<>(Rol.values()));
    private final JCheckBox activo = campo("Estado:", new JCheckBox("Activo", true));
    private final DefaultTableModel modelo = EstiloZeroLoss.tabla("Número", "Nombre", "Usuario", "Rol", "Activo");
    private final JTable tabla = tabla(modelo);
    private List<Usuario> usuarios = List.of();
    public FrmUsuarios() {
        super("Gestión de usuarios", "Administrador · Cree cuentas y asigne el perfil correspondiente.");
        cuerpo.add(new JScrollPane(tabla), BorderLayout.CENTER);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                Usuario u = usuarios.get(tabla.convertRowIndexToModel(tabla.getSelectedRow()));
                nombre.setText(u.getNombre()); username.setText(u.getUsername()); rol.setSelectedItem(u.getRol()); activo.setSelected(u.isActivo()); limpiarClave();
            }
        });
        local("Limpiar", () -> { tabla.clearSelection(); nombre.setText(""); username.setText(""); limpiarClave(); rol.setSelectedItem(Rol.CAJERO); activo.setSelected(true); });
        accion("Crear", AccionVista.CREAR);
        accion("Editar", AccionVista.EDITAR);
        accion("Desactivar", AccionVista.DESACTIVAR);
        accion("Restablecer clave", AccionVista.RESTABLECER_CLAVE);
        accion("Actualizar lista", AccionVista.LISTAR);
        rol.setSelectedItem(Rol.CAJERO);
        estado.setText("Editar cambia el perfil y el estado. Para cambiar la clave use Restablecer clave.");
        setSize(1050, 660);
    }
    @Override public Integer getIdSeleccionado() { return idSeleccionado(tabla); }
    @Override public String getNombre() { return leer(nombre); }
    @Override public String getUsername() { return leer(username); }
    @Override public char[] getClave() { return clave.getPassword(); }
    @Override public Rol getRol() { return (Rol)rol.getSelectedItem(); }
    @Override public boolean isActivo() { return activo.isSelected(); }
    @Override public void limpiarClave() { clave.setText(""); }
    @Override public void mostrarUsuarios(List<Usuario> lista) {
        usuarios = new ArrayList<>(lista); modelo.setRowCount(0); limpiarClave();
        for (Usuario u : lista) modelo.addRow(new Object[]{u.getId(), u.getNombre(), u.getUsername(), u.getRol(), u.isActivo() ? "Sí" : "No"});
    }
}
