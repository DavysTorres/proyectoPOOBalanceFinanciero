package controlador;

import vista.contratos.*;
import modelo.*;
import servicio.EstadoNegocio;

import java.math.BigDecimal;
import java.util.*;

/**
 * Vista de prueba: implementa los contratos sin JFrame y registra lo que presenta cada controlador.
 */
final class VistaPrueba implements VistaLogin, VistaPrincipal, VistaVenta, VistaCompraGasto, VistaBalance, VistaMargen, VistaHistorial, VistaModificarVenta, VistaCierre, VistaUsuarios, VistaProductos, VistaCostosFijos, VistaConfiguracion {
    final Map<AccionVista, Runnable> acciones = new EnumMap<>(AccionVista.class);
    final List<Boolean> estadosOcupada = new ArrayList<>();
    String error, informacion, filtro = "", cantidad = "1", costo = "6000", precio = "10000", proveedor = "Proveedor";
    String concepto = "Transporte", monto = "1000", dias = "30", fecha = "2026-11-06", ruta;
    String username = "ana", nombre = "Nombre", stock = "100", minimo = "2", apertura = "08:00", cierre = "18:00", amarillo = "70", margen = "15";
    String ventaId = "1", nuevoTotal = "19000", motivo = "Ajuste autorizado";
    char[] clave = "Clave123".toCharArray();
    boolean claveLimpiada, activo = true, fijo, ocupada;
    Integer id;
    Rol rol = Rol.CAJERO;
    Producto seleccionado, productoMostrado;
    Usuario usuarioMostrado;
    Venta ventaMostrada, ventaCobrada;
    Compra compraMostrada;
    BalanceDia balanceMostrado;
    CierreDiario cierreMostrado;
    EstadoNegocio estadoMostrado;
    BigDecimal efectivoMostrado;
    Negocio negocioMostrado;
    String consejoMostrado;
    List<Producto> productosMostrados, rankingMostrado;
    List<Compra> comprasMostradas;
    List<Gasto> gastosMostrados;
    List<Usuario> usuariosMostrados;
    List<CostoFijo> costosMostrados;
    List<RegistroCambio> registrosMostrados;

    // La prueba falla si se intenta conectar dos veces el mismo boton.
    public void alAccionar(AccionVista accion, Runnable manejador) {
        if (acciones.putIfAbsent(accion, manejador) != null) throw new AssertionError("Listener duplicado: " + accion);
    }

    void pulsar(AccionVista accion) {
        if (!acciones.containsKey(accion)) throw new AssertionError("No hay listener para " + accion);
        acciones.get(accion).run();
    }

    public void mostrarError(String mensaje) {
        error = mensaje;
    }

    public void mostrarInformacion(String mensaje) {
        informacion = mensaje;
    }

    public void setOcupada(boolean valor) {
        ocupada = valor;
        estadosOcupada.add(valor);
    }

    public String getUsername() {
        return username;
    }

    public char[] getContrasena() {
        return clave;
    }

    public void limpiarContrasena() {
        claveLimpiada = true;
    }

    public void mostrarUsuario(modelo.Usuario usuario) {
        usuarioMostrado = usuario;
    }

    public void mostrarSemaforo(servicio.EstadoNegocio estado) {
        estadoMostrado = estado;
    }

    public String getFiltro() {
        return filtro;
    }

    public modelo.Producto getProductoSeleccionado() {
        return seleccionado;
    }

    public String getCantidad() {
        return cantidad;
    }

    public void mostrarProductos(java.util.List<modelo.Producto> productos) {
        productosMostrados = productos;
    }

    public void mostrarVenta(modelo.Venta venta) {
        ventaMostrada = venta;
    }

    public void mostrarVentaCobrada(modelo.Venta venta) {
        ventaCobrada = venta;
    }

    public String getCostoUnitario() {
        return costo;
    }

    public String getProveedor() {
        return proveedor;
    }

    public String getConceptoGasto() {
        return concepto;
    }

    public String getMontoGasto() {
        return monto;
    }

    public boolean esGastoFijo() {
        return fijo;
    }

    public String getDiasOperacion() {
        return dias;
    }

    public void mostrarCompra(modelo.Compra compra) {
        compraMostrada = compra;
    }

    public void mostrarCompras(java.util.List<modelo.Compra> compras) {
        comprasMostradas = compras;
    }

    public void mostrarGastos(java.util.List<modelo.Gasto> gastos) {
        gastosMostrados = gastos;
    }

    public String getFecha() {
        return fecha;
    }

    public String getRutaExportacion() {
        return ruta;
    }

    public void mostrarBalance(modelo.BalanceDia balance) {
        balanceMostrado = balance;
    }

    public void mostrarRanking(java.util.List<modelo.Producto> productos) {
        rankingMostrado = productos;
    }

    public void mostrarConsejo(String consejo) {
        consejoMostrado = consejo;
    }

    public void mostrarRegistros(java.util.List<modelo.RegistroCambio> registros) {
        registrosMostrados = registros;
    }

    public String getVentaId() {
        return ventaId;
    }

    public String getNuevoTotal() {
        return nuevoTotal;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getEfectivoContado() {
        return monto;
    }

    public void mostrarEfectivoSistema(java.math.BigDecimal efectivo) {
        efectivoMostrado = efectivo;
    }

    public void mostrarCierre(modelo.CierreDiario cierre) {
        cierreMostrado = cierre;
    }

    public Integer getIdSeleccionado() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public char[] getClave() {
        return clave;
    }

    public modelo.Rol getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void limpiarClave() {
        claveLimpiada = true;
    }

    public void mostrarUsuarios(java.util.List<modelo.Usuario> usuarios) {
        usuariosMostrados = usuarios;
    }

    public String getPrecio() {
        return precio;
    }

    public String getCosto() {
        return costo;
    }

    public String getStock() {
        return stock;
    }

    public String getStockMinimo() {
        return minimo;
    }

    public void mostrarProducto(modelo.Producto producto) {
        productoMostrado = producto;
    }

    public String getConcepto() {
        return concepto;
    }

    public String getMontoMensual() {
        return monto;
    }

    public void mostrarCostos(java.util.List<modelo.CostoFijo> costos) {
        costosMostrados = costos;
    }

    public String getHoraApertura() {
        return apertura;
    }

    public String getHoraCierre() {
        return cierre;
    }

    public String getUmbralAmarillo() {
        return amarillo;
    }

    public String getUmbralMargenBajo() {
        return margen;
    }

    public void mostrarNegocio(modelo.Negocio negocio) {
        negocioMostrado = negocio;
    }
}
