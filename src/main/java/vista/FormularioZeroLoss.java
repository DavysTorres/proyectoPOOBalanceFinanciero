package vista;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;

/** Presentacion adaptable de los modulos, independiente de los servicios y permisos. */
final class FormularioZeroLoss {
    private FormularioZeroLoss() { }

    static JScrollPane envolver(Container original) {
        aplicar(original);
        if (original instanceof JPanel panel) panel.setBorder(BorderFactory.createEmptyBorder());
        JPanel tarjeta = TemaZeroLoss.tarjeta();
        tarjeta.setBorder(BorderFactory.createEmptyBorder(18, 20, 21, 20));
        tarjeta.setLayout(new BorderLayout());
        tarjeta.add(original);
        Lienzo lienzo = new Lienzo(tarjeta);
        JScrollPane scroll = new JScrollPane(lienzo);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(TemaZeroLoss.FONDO);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        scroll.getHorizontalScrollBar().setUnitIncrement(24);
        return scroll;
    }

    /** Estiliza solo los componentes de la aplicacion, sin recorrer los hijos internos del LAF. */
    private static void aplicar(Component c) {
        Font fuente = c.getFont();
        if (fuente != null) c.setFont(new Font("Segoe UI", fuente.getStyle(), Math.min(24, Math.max(14, fuente.getSize()))));
        if (c instanceof JLabel label) {
            Color color = label.getForeground();
            if (color == null || color.equals(Color.BLACK) || color.equals(new Color(51, 51, 51))) label.setForeground(TemaZeroLoss.TINTA);
        } else if (c instanceof JButton boton) {
            String texto = boton.getText() == null ? "" : boton.getText().toLowerCase(java.util.Locale.ROOT);
            boolean principal = texto.startsWith("guardar") || texto.equals("crear") || texto.startsWith("crear negocio")
                    || texto.equals("cobrar") || texto.equals("cerrar jornada");
            TemaZeroLoss.boton(boton, principal ? TemaZeroLoss.TipoBoton.PRINCIPAL : TemaZeroLoss.TipoBoton.SECUNDARIO);
            boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        } else if (c instanceof JTextComponent texto) {
            texto.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            texto.setForeground(TemaZeroLoss.TINTA);
            texto.setCaretColor(TemaZeroLoss.ACENTO);
            texto.setBackground(new Color(248, 250, 252));
            texto.setBorder(new BordeCampo());
            texto.addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { texto.repaint(); }
                @Override public void focusLost(FocusEvent e) { texto.repaint(); }
            });
        } else if (c instanceof JComboBox<?> combo) {
            combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            combo.setBackground(new Color(248, 250, 252));
            combo.setForeground(TemaZeroLoss.TINTA);
            combo.setBorder(BorderFactory.createLineBorder(new Color(214, 223, 232)));
            combo.setPreferredSize(new Dimension(220, 36));
        } else if (c instanceof JCheckBox check) {
            check.setOpaque(false);
            check.setForeground(TemaZeroLoss.TINTA);
        } else if (c instanceof JTable tabla) {
            tabla.setUI(new javax.swing.plaf.basic.BasicTableUI() {
                @Override public void paint(Graphics g, JComponent c) {
                    super.paint(g, c);
                    if (tabla.getRowCount() == 0) {
                        g.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                        g.setColor(TemaZeroLoss.SUAVE);
                        String mensaje = "No hay registros para mostrar.";
                        int x = Math.max(16, (c.getWidth() - g.getFontMetrics().stringWidth(mensaje)) / 2);
                        g.drawString(mensaje, x, 44);
                    }
                }
            });
            tabla.setRowHeight(32);
            tabla.setBackground(Color.WHITE);
            tabla.setForeground(TemaZeroLoss.TINTA);
            tabla.setSelectionBackground(new Color(218, 240, 239));
            tabla.setSelectionForeground(TemaZeroLoss.PROFUNDO);
            tabla.setShowVerticalLines(false);
            tabla.setGridColor(new Color(232, 239, 243));
            tabla.setIntercellSpacing(new Dimension(8, 1));
            tabla.setFillsViewportHeight(true);
            tabla.setPreferredScrollableViewportSize(new Dimension(760, 144));
            tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
            tabla.getTableHeader().setBackground(new Color(234, 244, 246));
            tabla.getTableHeader().setForeground(TemaZeroLoss.ACENTO);
            tabla.getTableHeader().setPreferredSize(new Dimension(0, 34));
            for (int i = 0; i < tabla.getColumnCount(); i++) {
                javax.swing.table.TableColumn columna = tabla.getColumnModel().getColumn(i);
                String nombre = tabla.getColumnName(i).toLowerCase(java.util.Locale.ROOT);
                boolean descripcion = nombre.contains("producto") || nombre.equals("nombre") || nombre.contains("motivo")
                        || nombre.contains("concepto") || nombre.contains("proveedor");
                columna.setPreferredWidth(descripcion ? 320 : 110);
                columna.setMinWidth(60);
                if (nombre.contains("precio") || nombre.contains("costo") || nombre.contains("monto") || nombre.contains("total")) {
                    javax.swing.table.DefaultTableCellRenderer dinero = new javax.swing.table.DefaultTableCellRenderer();
                    dinero.setHorizontalAlignment(SwingConstants.RIGHT);
                    columna.setCellRenderer(dinero);
                }
            }
            return;
        } else if (c instanceof JList<?> lista) {
            lista.setBackground(Color.WHITE);
            lista.setForeground(TemaZeroLoss.TINTA);
            lista.setSelectionBackground(new Color(218, 240, 239));
            lista.setSelectionForeground(TemaZeroLoss.PROFUNDO);
            lista.setVisibleRowCount(3);
            return;
        }
        if (c instanceof JScrollPane scroll) {
            scroll.setBorder(BorderFactory.createLineBorder(new Color(224, 234, 239)));
            scroll.getViewport().setBackground(Color.WHITE);
            scroll.getVerticalScrollBar().setUnitIncrement(24);
            Component vista = scroll.getViewport().getView();
            if (vista != null) aplicar(vista);
        } else if (c instanceof JTabbedPane pestanas) {
            pestanas.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
                @Override protected void installDefaults() {
                    super.installDefaults();
                    tabInsets = new Insets(9, 14, 9, 14);
                    contentBorderInsets = new Insets(12, 0, 0, 0);
                }
                @Override protected void paintContentBorder(Graphics g, int placement, int selected) { }
                @Override protected void paintTabBorder(Graphics g, int placement, int index, int x, int y, int w, int h, boolean selected) { }
                @Override protected void paintTabBackground(Graphics g, int placement, int index, int x, int y, int w, int h, boolean selected) {
                    Graphics2D g2 = TemaZeroLoss.suave(g);
                    g2.setColor(selected ? new Color(218, 240, 239) : Color.WHITE);
                    g2.fillRoundRect(x, y, w - 3, h - 2, 10, 10);
                    if (selected) { g2.setColor(TemaZeroLoss.ACENTO); g2.fillRoundRect(x + 12, y + h - 4, w - 27, 3, 3, 3); }
                    g2.dispose();
                }
            });
            pestanas.setOpaque(false);
            pestanas.setFont(new Font("Segoe UI", Font.BOLD, 13));
            pestanas.setBackground(new Color(234, 244, 246));
            pestanas.setForeground(TemaZeroLoss.ACENTO);
            pestanas.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
            for (int i = 0; i < pestanas.getTabCount(); i++) aplicar(pestanas.getComponentAt(i));
        } else if (c instanceof JPanel panel) {
            panel.setOpaque(false);
            if (panel.getLayout() instanceof FlowLayout f
                    && java.util.Arrays.stream(panel.getComponents()).anyMatch(hijo -> hijo instanceof JButton))
                panel.setLayout(new Acciones(f.getAlignment()));
            for (Component hijo : panel.getComponents()) aplicar(hijo);
        }
    }

    private static final class BordeCampo extends AbstractBorder {
        @Override public Insets getBorderInsets(Component c) { return new Insets(8, 11, 8, 11); }
        @Override public Insets getBorderInsets(Component c, Insets i) { i.set(8, 11, 8, 11); return i; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = TemaZeroLoss.suave(g);
            g2.setColor(c.hasFocus() ? TemaZeroLoss.ACENTO : new Color(214, 223, 232));
            g2.setStroke(new BasicStroke(c.hasFocus() ? 2f : 1f));
            g2.drawRoundRect(x + 1, y + 1, w - 3, h - 3, 10, 10);
            g2.dispose();
        }
    }

    static final class Pestanas extends JTabbedPane {
        Pestanas() {
            addChangeListener(e -> {
                revalidate();
                for (Container p = getParent(); p != null; p = p.getParent()) {
                    if (p instanceof Lienzo lienzo) { lienzo.revalidate(); break; }
                }
            });
        }
        @Override public Dimension getPreferredSize() {
            Component seleccionado = getSelectedComponent();
            if (seleccionado == null) return super.getPreferredSize();
            Dimension d = seleccionado.getPreferredSize();
            return new Dimension(Math.max(400, d.width + 8), d.height + 52);
        }
    }

    /** Tarjetas que pasan a una columna en espacios estrechos. */
    static final class Rejilla extends GridLayout {
        private final int anchoColumna;
        private final int maximoColumnas;
        Rejilla(int anchoColumna, int maximoColumnas, int espacio) {
            super(0, maximoColumnas, espacio, espacio);
            this.anchoColumna = anchoColumna;
            this.maximoColumnas = maximoColumnas;
        }
        private void adaptar(Container p) {
            Insets i = p.getInsets();
            int ancho = p.getWidth() > 0 ? p.getWidth() - i.left - i.right : 760;
            setColumns(Math.max(1, Math.min(maximoColumnas, (ancho + getHgap()) / (anchoColumna + getHgap()))));
        }
        @Override public Dimension preferredLayoutSize(Container p) { adaptar(p); return super.preferredLayoutSize(p); }
        @Override public void layoutContainer(Container p) { adaptar(p); super.layoutContainer(p); }
    }

    /** Conserva los pares etiqueta/editor y distribuye hasta tres columnas segun el ancho disponible. */
    static final class Campos implements LayoutManager {
        public void addLayoutComponent(String nombre, Component c) { }
        public void removeLayoutComponent(Component c) { }
        private int ancho(Container p) { return p.getWidth() > 0 ? p.getWidth() : 760; }
        private int columnas(Container p) {
            int pares = (p.getComponentCount() + 1) / 2;
            return Math.min(pares, ancho(p) >= 1200 ? 3 : ancho(p) >= 640 ? 2 : 1);
        }
        private int altoEditor(Component c) { return c instanceof JTextArea a ? Math.max(64, a.getRows() * 20 + 16) : c instanceof JCheckBox ? 24 : 36; }
        private int altoFila(Container p, int fila, int columnas) {
            int alto = 24;
            for (int col = 0; col < columnas; col++) {
                int indice = (fila * columnas + col) * 2 + 1;
                if (indice < p.getComponentCount()) alto = Math.max(alto, altoEditor(p.getComponent(indice)));
            }
            return alto + 24;
        }
        public Dimension preferredLayoutSize(Container p) {
            Insets i = p.getInsets();
            int col = Math.max(1, columnas(p)), pares = (p.getComponentCount() + 1) / 2, filas = (pares + col - 1) / col;
            int alto = i.top + i.bottom;
            for (int fila = 0; fila < filas; fila++) alto += altoFila(p, fila, col) + (fila == 0 ? 0 : 12);
            return new Dimension(760 + i.left + i.right, alto);
        }
        public Dimension minimumLayoutSize(Container p) { return new Dimension(240, preferredLayoutSize(p).height); }
        public void layoutContainer(Container p) {
            Insets i = p.getInsets();
            int col = Math.max(1, columnas(p)), ancho = p.getWidth() - i.left - i.right;
            int celda = Math.max(0, (ancho - (col - 1) * 24) / col), y = i.top;
            int pares = (p.getComponentCount() + 1) / 2;
            for (int fila = 0; fila < (pares + col - 1) / col; fila++) {
                int alto = altoFila(p, fila, col);
                for (int c = 0; c < col; c++) {
                    int n = (fila * col + c) * 2;
                    if (n >= p.getComponentCount()) break;
                    Component label = p.getComponent(n);
                    int x = i.left + c * (celda + 24);
                    label.setBounds(x, y, celda, 20);
                    if (n + 1 < p.getComponentCount()) {
                        Component editor = p.getComponent(n + 1);
                        editor.setBounds(x, y + 24, celda, altoEditor(editor));
                    }
                }
                y += alto + 12;
            }
        }
    }

    /** FlowLayout que reserva altura para todas las filas de acciones. */
    static final class Acciones extends FlowLayout {
        Acciones(int alineacion) { super(alineacion, 8, 6); }
        @Override public Dimension preferredLayoutSize(Container p) {
            int limite = p.getWidth() > 0 ? p.getWidth() : 760;
            Insets i = p.getInsets();
            int x = 0, fila = 0, alto = 0, max = 0;
            for (Component c : p.getComponents()) if (c.isVisible()) {
                Dimension d = c.getPreferredSize();
                if (x > 0 && x + d.width + 8 > limite - i.left - i.right - getHgap() * 2) {
                    max = Math.max(max, x); alto += fila + 6; x = 0; fila = 0;
                }
                x += d.width + 8; fila = Math.max(fila, d.height);
            }
            return new Dimension(Math.max(max, x) + i.left + i.right, alto + fila + 12 + i.top + i.bottom);
        }
    }

    /** Las tablas ocupan el espacio disponible; los editores conservan su altura natural. */
    private static final class Lienzo extends JPanel implements Scrollable {
        private final JPanel tarjeta;
        Lienzo(JPanel tarjeta) { super(null); this.tarjeta = tarjeta; add(tarjeta); }
        private int ancho() { return Math.max(240, (getWidth() > 0 ? getWidth() : 850) - 32); }
        private void distribuir(Container c) {
            c.doLayout();
            for (Component hijo : c.getComponents()) if (hijo instanceof Container contenedor) distribuir(contenedor);
        }
        private int alto() {
            tarjeta.setSize(ancho(), Math.max(600, tarjeta.getHeight()));
            distribuir(tarjeta);
            // Los layouts calculan su altura una vez conocida la anchura de campos y acciones.
            invalidar(tarjeta);
            int preferido = tarjeta.getPreferredSize().height;
            int disponible = getParent() instanceof JViewport viewport ? viewport.getExtentSize().height - 32 : 0;
            // BorderLayout entrega a tablas y listas el sobrante, sin agrandar los campos ni los botones.
            if (tieneRegistros(tarjeta)) {
                int minimo = Math.max(280, preferido);
                return Math.max(minimo, disponible);
            }
            return preferido;
        }
        private boolean tieneRegistros(Component c) {
            if (c instanceof JTable || c instanceof JList<?>) return true;
            if (c instanceof JTabbedPane tabs) return tabs.getSelectedComponent() != null && tieneRegistros(tabs.getSelectedComponent());
            if (c instanceof JScrollPane scroll) return scroll.getViewport().getView() != null && tieneRegistros(scroll.getViewport().getView());
            if (c instanceof Container padre) for (Component hijo : padre.getComponents())
                if (hijo.isVisible() && tieneRegistros(hijo)) return true;
            return false;
        }
        private void invalidar(Container c) {
            c.invalidate();
            for (Component hijo : c.getComponents()) if (hijo instanceof JPanel panel) invalidar(panel);
        }
        @Override public Dimension getPreferredSize() { return new Dimension(850, alto() + 32); }
        @Override public void doLayout() {
            int ancho = ancho(), alto = alto();
            tarjeta.setBounds(Math.max(16, (getWidth() - ancho) / 2), 16, ancho, alto);
            distribuir(tarjeta);
        }
        @Override protected void paintComponent(Graphics g) { TemaZeroLoss.pintarFondo(g, this, false); }
        public Dimension getPreferredScrollableViewportSize() { return new Dimension(850, 600); }
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 24; }
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 180; }
        public boolean getScrollableTracksViewportWidth() { return getParent() != null && getParent().getWidth() >= 272; }
        public boolean getScrollableTracksViewportHeight() { return getParent() != null && getParent().getHeight() >= getPreferredSize().height; }
    }
}
