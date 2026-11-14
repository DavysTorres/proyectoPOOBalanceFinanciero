package vista;

import modelo.*;
import servicio.*;
import vista.contratos.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;

/** Menú lateral con permisos del modelo y administración en el escritorio. */
public class FrmPrincipal extends FrmBase implements VistaPrincipal {
    private static final class PanelDashboard extends JPanel implements Scrollable {
        @Override protected void paintComponent(Graphics g) { TemaZeroLoss.pintarFondo(g, this, false); }
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 20; }
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 150; }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return getParent() != null && getParent().getHeight() > getPreferredSize().height; }
    }
    private final CardLayout distribucion = new CardLayout();
    private final JPanel contenidoCentral = new JPanel(distribucion);
    private final Map<FrmBase, Component> modulos = new IdentityHashMap<>();
    private FrmBase moduloActual;
    private boolean ocupacionPropia;
    private final JButton btnInicio = new JButton("Resumen del d" + (char)237 + "a");
    private final JLabel ruta = new JLabel("Resumen del d" + (char)237 + "a");
    private final JLabel actividad = new JLabel(" ");
    private final JPanel dashboard = new PanelDashboard();
    private final JScrollPane desplazamiento = new JScrollPane(dashboard);
    private final JDesktopPane escritorioPrincipal = new JDesktopPane() {
        @Override public void doLayout() {
            contenidoCentral.setBounds(0, 0, getWidth(), getHeight());
        }
    };
    private final JPanel encabezadoDashboard = new JPanel();
    private final JLabel bienvenida = new JLabel();
    private final JLabel orientacion = new JLabel();
    private final JPanel cifras = new JPanel(new GridLayout(0, 2, 16, 16) {
        private void adaptar(Container p) { setColumns(p.getWidth() >= 1048 ? 4 : p.getWidth() >= 516 ? 2 : 1); }
        @Override public Dimension preferredLayoutSize(Container p) { adaptar(p); return super.preferredLayoutSize(p); }
        @Override public void layoutContainer(Container p) { adaptar(p); super.layoutContainer(p); }
    }) {
        @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, getPreferredSize().height); }
    };
    private final JLabel ingresos = new JLabel("Pendiente de actualizar");
    private final JLabel resultado = new JLabel("Pendiente de actualizar");
    private final JLabel meta = new JLabel("Pendiente de actualizar");
    private final JLabel faltante = new JLabel("Pendiente de actualizar");
    private final JLabel costosVariables = new JLabel("Pendiente de actualizar");
    private final JLabel costosFijos = new JLabel("Pendiente de actualizar");
    private final JLabel margenContribucion = new JLabel("Pendiente de actualizar");
    private final JLabel lecturaDia = new JLabel("Actualiza el resumen para conocer tu avance.");
    private final JPanel detalleCostos = new JPanel(new FormularioZeroLoss.Rejilla(240, 3, 16));
    private final PanelPlanVentas planVentas = new PanelPlanVentas();
    private final JPanel zonaPlan = TemaZeroLoss.tarjeta();
    private final JButton verDetalle = new JButton("Ver costos y entender mi resultado");
    private final JLabel fechaDatos = new JLabel("Actualice para consultar la jornada.");
    private final JPanel operaciones = new JPanel(new GridLayout(0, 1, 0, 4));
    private final JPanel consultas = new JPanel(new GridLayout(0, 1, 0, 4));
    private final JPanel gestion = new JPanel(new GridLayout(0, 1, 0, 4));
    private final java.util.List<JButton> accesosAdmin = new ArrayList<>();
    private final JButton btnVentas = EstiloZeroLoss.botonMenu("Ventas");
    private final JButton btnCompras = EstiloZeroLoss.botonMenu("Compras / Gastos");
    private final JButton btnBalance = EstiloZeroLoss.botonMenu("Balance");
    private final JButton btnMargen = EstiloZeroLoss.botonMenu("Margen por Producto");
    private final JButton btnHistorial = EstiloZeroLoss.botonMenu("Historial");
    private final JButton btnCierreCaja = EstiloZeroLoss.botonMenu("Cierre de Caja");
    private final JButton btnAdministracion = EstiloZeroLoss.botonMenu("Administración");
    private final JButton btnModificarVenta = EstiloZeroLoss.botonMenu("Modificar venta");
    private final JButton btnReportes = EstiloZeroLoss.botonMenu("Reportes");
    private final JButton btnActualizar = new JButton("Actualizar semáforo");
    private final JLabel lblUsuario = new JLabel("Usuario: ");
    private final PanelSemaforo panelSemaforo = new PanelSemaforo();
    private final FrmAdministracion administracion = new FrmAdministracion();
    private final Map<JButton, Permiso> permisos = new LinkedHashMap<>();
    private Rol rolMostrado;
    private final JLabel notaMeta = new JLabel("Ventas necesarias para cubrir los costos");
    private final JLabel guia = new JLabel();
    private static final Color TINTA = TemaZeroLoss.TINTA;
    private static final Color SUAVE = TemaZeroLoss.SUAVE;
    private static final Color LATERAL = TemaZeroLoss.PROFUNDO;

    @Override protected boolean usaTemaFormulario() { return false; }

    public FrmPrincipal() { this(null); }

    public FrmPrincipal(Usuario usuario) {
        super("Sistema de Balance Diario - ZeroLoss");
        setLayout(new BorderLayout());
        escritorioPrincipal.setBackground(new Color(50, 85, 130));
        contenidoCentral.add(desplazamiento, "INICIO");
        escritorioPrincipal.add(contenidoCentral, JLayeredPane.DEFAULT_LAYER);
        escritorioPrincipal.add(administracion, JLayeredPane.PALETTE_LAYER);
        add(escritorioPrincipal, BorderLayout.CENTER);
        registrar(btnVentas, AccionVista.ABRIR_VENTA, Permiso.REGISTRAR_VENTA);
        registrar(btnCompras, AccionVista.ABRIR_COMPRA_GASTO, Permiso.REGISTRAR_COMPRA);
        registrar(btnBalance, AccionVista.ABRIR_BALANCE, Permiso.VER_BALANCE);
        registrar(btnMargen, AccionVista.ABRIR_MARGEN, Permiso.VER_BALANCE);
        registrar(btnHistorial, AccionVista.ABRIR_HISTORIAL, Permiso.VER_HISTORIAL);
        registrar(btnModificarVenta, AccionVista.ABRIR_MODIFICAR_VENTA, Permiso.MODIFICAR_VENTA);
        registrar(btnCierreCaja, AccionVista.ABRIR_CIERRE, Permiso.CERRAR_CAJA);
        registrar(btnReportes, AccionVista.ABRIR_REPORTES, Permiso.VER_REPORTES);
        registrar(administracion.getBtnUsuarios(), AccionVista.ABRIR_USUARIOS, Permiso.GESTIONAR_USUARIOS);
        registrar(administracion.getBtnProductos(), AccionVista.ABRIR_PRODUCTOS, Permiso.GESTIONAR_PRODUCTOS);
        registrar(administracion.getBtnCostosFijos(), AccionVista.ABRIR_COSTOS_FIJOS, Permiso.DEFINIR_COSTOS_FIJOS);
        registrar(administracion.getBtnConfiguracion(), AccionVista.ABRIR_CONFIGURACION, Permiso.CONFIGURAR);
        // Los dos accesos repetidos delegan en los botones ya registrados para no duplicar acciones.
        administracion.getBtnHistorial().addActionListener(e -> btnHistorial.doClick());
        administracion.getBtnReportes().addActionListener(e -> btnReportes.doClick());
        btnAdministracion.addActionListener(e -> abrirAdministracion());
        construirDashboard();
        construirBarraLateral();

        JPanel superior = new JPanel(new BorderLayout(20, 0));
        superior.setBackground(Color.WHITE);
        superior.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));
        ruta.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ruta.setForeground(TINTA);
        superior.add(ruta, BorderLayout.WEST);
        JPanel acciones = new JPanel(new FormularioZeroLoss.Acciones(FlowLayout.RIGHT));
        acciones.setOpaque(false);
        actividad.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        actividad.setForeground(TemaZeroLoss.ACENTO);
        acciones.add(actividad);
        btnActualizar.setText("Actualizar indicadores");
        JButton cerrarSesion = new JButton("Cerrar sesi" + (char)243 + "n");
        TemaZeroLoss.boton(btnActualizar, TemaZeroLoss.TipoBoton.PRINCIPAL);
        TemaZeroLoss.boton(cerrarSesion, TemaZeroLoss.TipoBoton.SECUNDARIO);
        registrarBoton(AccionVista.ACTUALIZAR, btnActualizar);
        registrarBoton(AccionVista.CERRAR_SESION, cerrarSesion);
        acciones.add(btnActualizar);
        acciones.add(cerrarSesion);
        superior.add(acciones, BorderLayout.CENTER);
        add(superior, BorderLayout.NORTH);
        mostrarUsuario(usuario);
        setMinimumSize(new Dimension(1000, 600));
        setSize(1150, 700);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void construirDashboard() {
        dashboard.setLayout(new BoxLayout(dashboard, BoxLayout.Y_AXIS));
        dashboard.setBackground(new Color(244, 247, 251));
        dashboard.setBorder(BorderFactory.createEmptyBorder(28, 30, 30, 30));
        desplazamiento.setBorder(null);
        estilizarScroll(desplazamiento, false);
        desplazamiento.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        desplazamiento.getVerticalScrollBar().setUnitIncrement(20);
        bienvenida.setFont(new Font("Segoe UI", Font.BOLD, 27));
        bienvenida.setForeground(TINTA);
        orientacion.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        orientacion.setForeground(SUAVE);
        JPanel cabecera = encabezadoDashboard;
        cabecera.setOpaque(false);
        cabecera.setLayout(new BoxLayout(cabecera, BoxLayout.Y_AXIS));
        JLabel fecha = texto(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern(
                "EEEE, d 'de' MMMM", java.util.Locale.forLanguageTag("es-CO"))), 12, TemaZeroLoss.ACENTO, true);
        fecha.setAlignmentX(Component.LEFT_ALIGNMENT);
        cabecera.add(fecha);
        cabecera.add(Box.createVerticalStrut(10));
        bienvenida.setAlignmentX(Component.LEFT_ALIGNMENT);
        cabecera.add(bienvenida);
        cabecera.add(Box.createVerticalStrut(8));
        orientacion.setAlignmentX(Component.LEFT_ALIGNMENT);
        cabecera.add(orientacion);
        bloque(cabecera);
        bloque(Box.createVerticalStrut(26));
        JPanel estado = tarjeta();
        estado.setLayout(new BorderLayout(0, 12));
        lecturaDia.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lecturaDia.setForeground(TINTA);
        estado.add(lecturaDia, BorderLayout.NORTH);
        estado.add(panelSemaforo, BorderLayout.CENTER);
        fechaDatos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fechaDatos.setForeground(SUAVE);
        estado.add(fechaDatos, BorderLayout.SOUTH);
        bloque(estado);
        bloque(Box.createVerticalStrut(18));
        cifras.setOpaque(false);
        String[] nombres = {"LO QUE HAS VENDIDO HOY", "PARA CUBRIR TUS COSTOS", "TE FALTA VENDER", "GANANCIA O P\u00c9RDIDA DE HOY"};
        String[] ayudas = {"Ventas registradas hasta este momento", "Punto de equilibrio estimado con el margen de hoy",
                "Ventas adicionales estimadas para llegar al equilibrio", "Ventas menos el costo vendido y todos los gastos registrados"};
        JLabel[] valores = {ingresos, meta, faltante, resultado};
        for (int i = 0; i < nombres.length; i++) {
            JPanel tarjeta = tarjeta();
            tarjeta.setLayout(new BorderLayout(0, 12));
            tarjeta.setPreferredSize(new Dimension(250, 190));
            tarjeta.add(texto("<html>" + nombres[i] + "</html>", 12, TemaZeroLoss.ACENTO, true), BorderLayout.NORTH);
            valores[i].setText("\u2014");
            valores[i].setFont(new Font("Segoe UI", Font.BOLD, 25));
            valores[i].setForeground(TINTA);
            tarjeta.add(valores[i], BorderLayout.CENTER);
            JLabel ayuda = i == 1 ? notaMeta : new JLabel("<html>" + ayudas[i] + "</html>");
            ayuda.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            ayuda.setForeground(SUAVE);
            tarjeta.add(ayuda, BorderLayout.SOUTH);
            cifras.add(tarjeta);
        }
        bloque(cifras);
        bloque(Box.createVerticalStrut(18));
        zonaPlan.setLayout(new BorderLayout());
        zonaPlan.add(planVentas);
        bloque(zonaPlan);
        bloque(Box.createVerticalStrut(16));
        TemaZeroLoss.boton(verDetalle, TemaZeroLoss.TipoBoton.SECUNDARIO);
        verDetalle.addActionListener(e -> {
            if (estaOcupada()) return;
            detalleCostos.setVisible(!detalleCostos.isVisible());
            verDetalle.setText(detalleCostos.isVisible() ? "Ocultar detalle de costos" : "Ver costos y entender mi resultado");
            dashboard.revalidate();
        });
        bloque(verDetalle);
        bloque(Box.createVerticalStrut(12));
        detalleCostos.setOpaque(false);
        String[] titulosDetalle = {"COSTO DE LO VENDIDO Y GASTOS VARIABLES", "COSTOS FIJOS QUE CORRESPONDEN A HOY", "LO QUE QUEDA PARA PAGAR LOS COSTOS FIJOS"};
        JLabel[] cifrasDetalle = {costosVariables, costosFijos, margenContribucion};
        for (int i = 0; i < titulosDetalle.length; i++) {
            JPanel tarjeta = tarjeta();
            tarjeta.setLayout(new BorderLayout(0, 12));
            tarjeta.add(texto("<html>" + titulosDetalle[i] + "</html>", 12, SUAVE, true), BorderLayout.NORTH);
            cifrasDetalle[i].setFont(new Font("Segoe UI", Font.BOLD, 22));
            tarjeta.add(cifrasDetalle[i]);
            detalleCostos.add(tarjeta);
        }
        detalleCostos.setVisible(false);
        bloque(detalleCostos);
        bloque(Box.createVerticalStrut(14));
        guia.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        guia.setForeground(SUAVE);
        bloque(guia);
    }

    private void construirBarraLateral() {
        JPanel lateral = TemaZeroLoss.degradado(true);
        lateral.setLayout(new BoxLayout(lateral, BoxLayout.Y_AXIS));
        lateral.setBackground(LATERAL);
        lateral.setBorder(BorderFactory.createEmptyBorder(22, 14, 20, 14));
        JLabel marca = texto("ZeroLoss", 23, Color.WHITE, true);
        marca.setAlignmentX(Component.LEFT_ALIGNMENT);
        marca.setIcon(new TemaZeroLoss.IconoMarca(38));
        marca.setIconTextGap(12);
        lateral.add(marca);
        lateral.add(Box.createVerticalStrut(8));
        JLabel eslogan = texto("Cada venta cuenta.", 12, new Color(187, 232, 230), false);
        eslogan.setAlignmentX(Component.LEFT_ALIGNMENT);
        lateral.add(eslogan);
        lateral.add(Box.createVerticalStrut(10));
        lblUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUsuario.setForeground(new Color(185, 199, 218));
        lblUsuario.setAlignmentX(Component.LEFT_ALIGNMENT);
        lateral.add(lblUsuario);
        lateral.add(Box.createVerticalStrut(26));
        estiloNavegacion(btnInicio);
        btnInicio.setForeground(new Color(116, 204, 207));
        btnInicio.putClientProperty("activo", Boolean.TRUE);
        btnInicio.addActionListener(e -> mostrarInicio());
        lateral.add(btnInicio);
        lateral.add(Box.createVerticalStrut(20));
        operaciones.add(btnVentas); operaciones.add(btnCompras); operaciones.add(btnCierreCaja);
        consultas.add(btnBalance); consultas.add(btnMargen); consultas.add(btnReportes);
        consultas.add(btnHistorial); consultas.add(btnModificarVenta);
        JButton[] destinos = {administracion.getBtnUsuarios(), administracion.getBtnProductos(),
                administracion.getBtnCostosFijos(), administracion.getBtnConfiguracion()};
        String[] textos = {"Usuarios y permisos", "Productos e inventario", "Costos fijos", "Configuraci\u00f3n"};
        for (int i = 0; i < destinos.length; i++) {
            JButton destino = destinos[i];
            JButton acceso = new JButton(textos[i]);
            acceso.addActionListener(e -> { if (!estaOcupada()) destino.doClick(); });
            accesosAdmin.add(acceso);
            gestion.add(acceso);
        }
        JPanel[] grupos = {operaciones, consultas, gestion};
        String[] titulos = {"OPERACI\u00d3N", "CONSULTAS", "ADMINISTRACI\u00d3N"};
        for (int i = 0; i < grupos.length; i++) {
            JPanel grupo = grupos[i];
            grupo.setOpaque(false);
            grupo.setAlignmentX(Component.LEFT_ALIGNMENT);
            grupo.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), titulos[i],
                            0, 0, new Font("Segoe UI", Font.BOLD, 10), new Color(148, 168, 191)),
                    BorderFactory.createEmptyBorder(7, 0, 12, 0)));
            for (Component c : grupo.getComponents()) estiloNavegacion((JButton)c);
            if (grupo == gestion) {
                estiloNavegacion(btnAdministracion);
                lateral.add(btnAdministracion);
            }
            lateral.add(grupo);
        }

        lateral.add(Box.createVerticalGlue());
        JScrollPane scroll = new JScrollPane(lateral);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(238, 0));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        estilizarScroll(scroll, true);
        add(scroll, BorderLayout.WEST);
        JButton menu = new JButton("Men\u00fa");
        menu.addActionListener(e -> { scroll.setVisible(!scroll.isVisible()); revalidate(); });
        JPanel barraMenu = new JPanel(new BorderLayout());
        barraMenu.add(menu, BorderLayout.WEST);
        add(barraMenu, BorderLayout.SOUTH);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                boolean compacto = getWidth() < 900;
                scroll.setVisible(!compacto);
                barraMenu.setVisible(compacto);
                revalidate();
            }
        });
        barraMenu.setVisible(false);
    }

    private void estiloNavegacion(JButton boton) {
        TemaZeroLoss.boton(boton, TemaZeroLoss.TipoBoton.NAVEGACION);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setPreferredSize(new Dimension(205, 37));
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 37));
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void estilizarScroll(JScrollPane scroll, boolean oscuro) {
        scroll.getViewport().setBackground(oscuro ? LATERAL : TemaZeroLoss.FONDO);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = oscuro ? new Color(62, 119, 131) : new Color(169, 204, 208);
                trackColor = oscuro ? LATERAL : TemaZeroLoss.FONDO;
            }
            private JButton vacio() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setMinimumSize(new Dimension(0, 0));
                b.setMaximumSize(new Dimension(0, 0));
                return b;
            }
            @Override protected JButton createDecreaseButton(int orientation) { return vacio(); }
            @Override protected JButton createIncreaseButton(int orientation) { return vacio(); }
        });
    }

    private JLabel texto(String valor, int tamano, Color color, boolean negrita) {
        JLabel label = new JLabel(valor);
        label.setFont(new Font("Segoe UI", negrita ? Font.BOLD : Font.PLAIN, tamano));
        label.setForeground(color);
        return label;
    }

    private JPanel tarjeta() { return TemaZeroLoss.tarjeta(); }

    private void bloque(Component componente) {
        if (componente instanceof JComponent jc) jc.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (componente instanceof JPanel panel) {
            JPanel envoltura = new JPanel(new BorderLayout()) {
                @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, getPreferredSize().height); }
            };
            envoltura.setOpaque(false);
            envoltura.setAlignmentX(Component.LEFT_ALIGNMENT);
            envoltura.add(panel);
            dashboard.add(envoltura);
        } else dashboard.add(componente);
    }

    @Override public void mostrarBalanceDashboard(BalanceDia balance) {
        planVentas.mostrarBalance(balance);
        if (balance == null) {
            lecturaDia.setText("Actualiza el resumen para conocer tu avance.");
            notaMeta.setText("Esperando los registros del negocio.");
            for (JLabel valor : new JLabel[]{ingresos, resultado, meta, faltante, costosVariables, costosFijos, margenContribucion}) valor.setText("\u2014");
            return;
        }
        if (!cifras.isVisible()) return;
        costosVariables.setText(util.FormatoMoneda.mostrar(balance.getCostosVariables()));
        costosFijos.setText(util.FormatoMoneda.mostrar(balance.getCostosFijosDia()));
        margenContribucion.setText(util.FormatoMoneda.mostrar(balance.getMargenContribucion()));
        ingresos.setText(util.FormatoMoneda.mostrar(balance.getIngresos()));
        resultado.setText(util.FormatoMoneda.mostrar(balance.getResultado()));
        resultado.setForeground(balance.getResultado().signum() < 0 ? new Color(185, 28, 28) : new Color(21, 128, 61));
        meta.setText(!balance.isPuntoEquilibrioCalculable() ? "No calculable"
                : util.FormatoMoneda.mostrar(balance.getPuntoEquilibrio()));
        notaMeta.setText(balance.isPuntoEquilibrioEstimado() ? "<html>Estimado con las ventas de hoy. Cambia con nuevos movimientos.</html>"
                : "Ventas necesarias para cubrir los costos");
        faltante.setText(balance.isPuntoEquilibrioCalculable() ? util.FormatoMoneda.mostrar(balance.getFaltante()) : "No calculable");
        if (balance.getIngresos().signum() == 0) {
            lecturaDia.setText("Todav\u00eda no hay ventas registradas hoy");
            meta.setText("Por planificar");
            faltante.setText("Esperando ventas");
            notaMeta.setText("<html>Sin ventas: usa el simulador con tu margen previsto.</html>");
        } else if (!balance.isPuntoEquilibrioCalculable()) {
            lecturaDia.setText("Las ventas a\u00fan no dejan margen para cubrir tus costos");
            notaMeta.setText("<html>Revisa precios y costos: no hay margen positivo para estimar.</html>");
        } else if (balance.getResultado().signum() < 0) {
            lecturaDia.setText("A\u00fan te falta vender " + util.FormatoMoneda.mostrar(balance.getFaltante()) + " para cubrir los costos de hoy");
        } else if (balance.getResultado().signum() == 0) {
            lecturaDia.setText("Llegaste al equilibrio: cubres tus costos, con ganancia cero");
        } else {
            lecturaDia.setText("Ya cubres tus costos y llevas " + util.FormatoMoneda.mostrar(balance.getResultado()) + " de ganancia registrada");
        }
        lecturaDia.setText("<html>" + lecturaDia.getText() + "</html>");
        guia.setText("<html><b>\u00bfCompras inventario cada semana o cada mes?</b> Registrar una compra aumenta tu inventario y reduce tu caja. "
                + "Para calcular la ganancia, se descuenta el costo de los productos cuando los vendes, aunque los hayas comprado otro d\u00eda. "
                + "El equilibrio significa ganancia cero; las ventas que lo superan no son ganancia completa, porque tambi\u00e9n tienen costo. "
                + "La ganancia depende de que est\u00e9n registrados todos los costos y gastos.</html>");
        dashboard.revalidate();
        dashboard.repaint();

    }

    private void registrar(JButton boton, AccionVista accion, Permiso permiso) {
        registrarBoton(accion, boton);
        permisos.put(boton, permiso);
        boton.setVisible(false);
    }

    private void abrirAdministracion() {
        if (estaOcupada() || !btnAdministracion.isVisible()) return;
        gestion.setVisible(!gestion.isVisible());
        btnAdministracion.setText(gestion.isVisible() ? "Administraci" + (char)243 + "n  -" : "Administraci" + (char)243 + "n  +");
        revalidate();
        repaint();
    }

    @Override public void mostrarUsuario(Usuario usuario) {
        Rol anterior = rolMostrado;
        rolMostrado = usuario == null ? null : usuario.getRol();
        lblUsuario.setText(usuario == null ? "Usuario: " : "<html>" + usuario.getNombre().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "<br>" + usuario.getRol() + "</html>");
        permisos.forEach((boton, permiso) -> boton.setVisible(usuario != null && usuario.isActivo() && usuario.tienePermiso(permiso)));
        boolean admin = usuario != null && usuario.isActivo() && usuario.getRol() == Rol.ADMINISTRADOR;
        btnAdministracion.setVisible(admin);
        boolean financiero = usuario != null && usuario.isActivo() && usuario.tienePermiso(Permiso.VER_BALANCE);
        cifras.setVisible(financiero);
        zonaPlan.setVisible(financiero);
        verDetalle.setVisible(financiero);
        lecturaDia.setVisible(financiero);
        if (!financiero || anterior != rolMostrado) {
            detalleCostos.setVisible(false);
            planVentas.limpiar();
        }
        consultas.setVisible(financiero);

        operaciones.removeAll();
        for (JButton b : new JButton[]{btnVentas, btnCompras, btnCierreCaja}) if (b.isVisible()) operaciones.add(b);
        consultas.removeAll();
        for (JButton b : new JButton[]{btnBalance, btnMargen, btnReportes, btnHistorial, btnModificarVenta})
            if (b.isVisible()) consultas.add(b);
        if (!admin || anterior != rolMostrado) gestion.setVisible(false);
        btnAdministracion.setText("Administraci" + (char)243 + "n  " + (gestion.isVisible() ? "-" : "+"));
        accesosAdmin.forEach(b -> { b.setVisible(admin); b.setEnabled(!estaOcupada()); });
        bienvenida.setText(admin ? "Rentabilidad de mi negocio" : financiero ? "Rentabilidad de la jornada" : "Punto de venta");
        guia.setText("Selecciona una opci" + (char)243 + "n en la barra lateral para continuar.");
        orientacion.setText(admin ? "Primero cubre tus costos; luego convierte tus ventas en ganancia."
                : financiero ? "Supervisa los resultados y organiza las operaciones del d\u00eda."
                : "Registra las ventas y consulta c\u00f3mo avanza el negocio hoy.");
        if (rolMostrado != null) {
            for (JLabel valor : new JLabel[]{ingresos, resultado, meta, faltante, costosVariables, costosFijos, margenContribucion}) valor.setText("\u2014");
        }
        ((BoxLayout)encabezadoDashboard.getLayout()).invalidateLayout(encabezadoDashboard);
        encabezadoDashboard.setMaximumSize(new Dimension(Integer.MAX_VALUE, encabezadoDashboard.getPreferredSize().height));
        administracion.mostrarPermisos(admin ? usuario : null);
        if (!admin) administracion.setVisible(false);
        for (JPanel grupo : new JPanel[]{operaciones, consultas, gestion})
            grupo.setMaximumSize(new Dimension(Integer.MAX_VALUE, grupo.getPreferredSize().height));
        revalidate();
        repaint();
    }

    @Override public void iniciarCargaResumen() {
        mostrarBalanceDashboard(null);
        panelSemaforo.sinDatos("Consultando los registros del negocio...");
        fechaDatos.setText("Actualizando...");
    }

    @Override public void mostrarFalloResumen() {
        mostrarBalanceDashboard(null);
        panelSemaforo.sinDatos("No se pudo actualizar el resumen. Revise la conexion y la configuracion.");
        fechaDatos.setText("Sin datos actualizados");
    }

    @Override public void mostrarSemaforo(EstadoNegocio estado) { panelSemaforo.mostrarEstado(estado); }
    @Override public void mostrarResumenSemaforo(ResumenSemaforo resumen) {
        panelSemaforo.mostrarResumen(resumen);
        fechaDatos.setText("\u00daltima actualizaci\u00f3n: " + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }
    public Rol getRolMostrado() { return rolMostrado; }
    public void solicitarActualizacion() { btnActualizar.doClick(); }

    @Override public void setOcupada(boolean ocupada) {
        ocupacionPropia = ocupada;
        actualizarBloqueo();
    }

    private void actualizarBloqueo() {
        boolean ocupada = ocupacionPropia || (moduloActual != null && moduloActual.estaOcupada());
        super.setOcupada(ocupada);
        actividad.setText(ocupada ? "Procesando..." : " ");
        btnInicio.setEnabled(!ocupada);
        btnAdministracion.setEnabled(!ocupada);
        accesosAdmin.forEach(b -> b.setEnabled(!ocupada));
        verDetalle.setEnabled(!ocupada);
        administracion.getBtnHistorial().setEnabled(!ocupada);
        administracion.getBtnReportes().setEnabled(!ocupada);
    }

    public void mostrarModulo(FrmBase modulo) {
        if (moduloActual != null && moduloActual.estaOcupada()) return;
        if (!modulos.containsKey(modulo)) {
            Container contenido = modulo.extraerContenido(this);
            JScrollPane scroll = (JScrollPane)contenido;
            scroll.setBorder(null);
            estilizarScroll(scroll, false);
            scroll.getVerticalScrollBar().setUnitIncrement(20);
            modulos.put(modulo, scroll);
            contenidoCentral.add(scroll, Integer.toString(System.identityHashCode(modulo)));
            modulo.alOcupar(valor -> actualizarBloqueo());
        }
        moduloActual = modulo;
        actualizarSeleccion();
        distribucion.show(contenidoCentral, Integer.toString(System.identityHashCode(modulo)));
        actualizarBloqueo();
        contenidoCentral.revalidate();
        contenidoCentral.repaint();
    }

    public void mostrarInicio() {
        if (estaOcupada()) return;
        moduloActual = null;
        actualizarSeleccion();
        distribucion.show(contenidoCentral, "INICIO");
        actualizarBloqueo();
    }

    public void retirarModulo(FrmBase modulo) {
        Component contenido = modulos.remove(modulo);
        if (contenido != null) contenidoCentral.remove(contenido);
        modulo.alOcupar(valor -> { });
        if (moduloActual == modulo) {
            moduloActual = null;
            actualizarSeleccion();
            distribucion.show(contenidoCentral, "INICIO");
        }
        actualizarBloqueo();
        contenidoCentral.revalidate();
        contenidoCentral.repaint();
    }

    private void actualizarSeleccion() {
        JButton activo = moduloActual == null ? btnInicio
                : moduloActual instanceof FrmVenta ? btnVentas
                : moduloActual instanceof FrmCompraGasto ? btnCompras
                : moduloActual instanceof FrmReportes ? btnReportes
                : moduloActual instanceof FrmBalance ? btnBalance
                : moduloActual instanceof FrmMargen ? btnMargen
                : moduloActual instanceof FrmHistorial ? btnHistorial
                : moduloActual instanceof FrmModificarVenta ? btnModificarVenta
                : moduloActual instanceof FrmCierreCaja ? btnCierreCaja
                : moduloActual instanceof FrmUsuarios ? accesosAdmin.get(0)
                : moduloActual instanceof FrmProductos ? accesosAdmin.get(1)
                : moduloActual instanceof FrmCostosFijos ? accesosAdmin.get(2)
                : moduloActual instanceof FrmConfiguracion ? accesosAdmin.get(3) : null;
        java.util.List<JButton> botones = new ArrayList<>(permisos.keySet());
        botones.addAll(accesosAdmin);
        botones.add(btnInicio);
        for (JButton b : botones) {
            b.putClientProperty("activo", b == activo);
            b.repaint();
        }
        if (accesosAdmin.contains(activo)) {
            gestion.setVisible(true);
            btnAdministracion.setText("Administraci" + (char)243 + "n  -");
        }
        ruta.setText(moduloActual == null ? "Resumen del d" + (char)237 + "a"
                : moduloActual.getTitle().replace(" - ZeroLoss", ""));
    }

    public FrmBase getModuloActual() { return moduloActual; }

    public JButton getBtnVentas() { return btnVentas; }
    public JButton getBtnCompras() { return btnCompras; }
    public JButton getBtnBalance() { return btnBalance; }
    public JButton getBtnMargen() { return btnMargen; }
    public JButton getBtnHistorial() { return btnHistorial; }
    public JButton getBtnCierreCaja() { return btnCierreCaja; }
    public JButton getBtnAdministracion() { return btnAdministracion; }
    public JLabel getLblUsuario() { return lblUsuario; }
    public PanelSemaforo getPanelSemaforo() { return panelSemaforo; }
    public JDesktopPane getEscritorioPrincipal() { return escritorioPrincipal; }
}
