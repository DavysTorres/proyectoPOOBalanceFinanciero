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
    private final JPanel cifras = new JPanel(new FormularioZeroLoss.Rejilla(290, 2, 18)) {
        @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, getPreferredSize().height); }
    };
    private final JLabel ingresos = new JLabel("Pendiente de actualizar");
    private final JLabel resultado = new JLabel("Pendiente de actualizar");
    private final JLabel meta = new JLabel("Pendiente de actualizar");
    private final JLabel faltante = new JLabel("Pendiente de actualizar");
    private final JLabel costosVariables = new JLabel("Pendiente de actualizar");
    private final JLabel costosFijos = new JLabel("Pendiente de actualizar");
    private final JLabel margenContribucion = new JLabel("Pendiente de actualizar");
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
        cifras.setOpaque(false);
        String[] nombres = {"INGRESOS DEL D\u00cdA", "RESULTADO REGISTRADO", "META ESTIMADA DE VENTAS", "VENTAS ADICIONALES ESTIMADAS", "COSTO VENDIDO Y OTROS GASTOS VARIABLES", "COSTOS FIJOS DEL D\u00cdA", "MARGEN PARA CUBRIR COSTOS FIJOS"};
        String[] ayudas = {"Total de ventas registradas", "Margen menos costos fijos del d\u00eda",
                "Ventas necesarias para cubrir los costos", "Importe pendiente para alcanzar el equilibrio", "Productos vendidos y gastos variables registrados", "Cuota diaria de los costos mensuales registrados", "Ventas menos costo vendido y gastos variables"};
        JLabel[] valores = {ingresos, resultado, meta, faltante, costosVariables, costosFijos, margenContribucion};
        for (int i = 0; i < nombres.length; i++) {
            JPanel tarjeta = tarjeta();
            tarjeta.setLayout(new BorderLayout(0, 14));
            tarjeta.add(texto("<html>" + nombres[i] + "</html>", 12, TemaZeroLoss.ACENTO, true), BorderLayout.NORTH);
            valores[i].setText("\u2014");
            valores[i].setFont(new Font("Segoe UI", Font.BOLD, 25));
            valores[i].setForeground(TINTA);
            tarjeta.add(valores[i], BorderLayout.CENTER);
            JLabel ayuda = i == 2 ? notaMeta : new JLabel("<html>" + ayudas[i] + "</html>");
            ayuda.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            ayuda.setForeground(SUAVE);
            tarjeta.add(ayuda, BorderLayout.SOUTH);
            cifras.add(tarjeta);
        }
        bloque(cifras);
        bloque(Box.createVerticalStrut(24));
        JPanel estado = tarjeta();
        estado.setLayout(new BorderLayout(0, 18));
        estado.add(texto("Salud del negocio", 18, TINTA, true), BorderLayout.NORTH);
        estado.add(panelSemaforo, BorderLayout.CENTER);
        fechaDatos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fechaDatos.setForeground(SUAVE);
        estado.add(fechaDatos, BorderLayout.SOUTH);
        bloque(estado);
        bloque(Box.createVerticalStrut(22));
        guia.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
        if (componente instanceof JPanel panel)
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        dashboard.add(componente);
    }

    @Override public void mostrarBalanceDashboard(BalanceDia balance) {
        if (balance == null) {
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
        notaMeta.setText(balance.isPuntoEquilibrioEstimado() ? "<html>Estimaci\u00f3n seg\u00fan el margen de hoy; cambiar\u00e1 si cambian ventas o gastos.</html>"
                : "Ventas necesarias para cubrir los costos");
        faltante.setText(balance.isPuntoEquilibrioCalculable() ? util.FormatoMoneda.mostrar(balance.getFaltante()) : "No calculable");
        guia.setText("<html>Resultado seg\u00fan costos y gastos registrados. Las compras reponen inventario; "
                + "su pago afecta la caja y su costo se reconoce al vender. Sin ventas con margen positivo, "
                + "no se estima una meta. Cubrir costos no equivale a obtener una utilidad objetivo.</html>");
        notaMeta.setText(!balance.isPuntoEquilibrioCalculable()
                ? "<html>Sin ventas o sin margen positivo para estimar.</html>" : notaMeta.getText());

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
        consultas.setVisible(financiero);

        operaciones.removeAll();
        for (JButton b : new JButton[]{btnVentas, btnCompras, btnCierreCaja}) if (b.isVisible()) operaciones.add(b);
        consultas.removeAll();
        for (JButton b : new JButton[]{btnBalance, btnMargen, btnReportes, btnHistorial, btnModificarVenta})
            if (b.isVisible()) consultas.add(b);
        if (!admin || anterior != rolMostrado) gestion.setVisible(false);
        btnAdministracion.setText("Administraci" + (char)243 + "n  " + (gestion.isVisible() ? "-" : "+"));
        accesosAdmin.forEach(b -> { b.setVisible(admin); b.setEnabled(!estaOcupada()); });
        bienvenida.setText(admin ? "Panel de administraci\u00f3n" : financiero ? "Resumen de la jornada" : "Punto de venta");
        guia.setText("Selecciona una opci" + (char)243 + "n en la barra lateral para continuar.");
        orientacion.setText(admin ? "Indicadores clave y estado del negocio para tomar decisiones."
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
