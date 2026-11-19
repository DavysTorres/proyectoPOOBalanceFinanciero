package vista.contratos;

import modelo.BalanceDia;

public interface VistaBalance extends VistaBase {
    String getFecha();
    String getRutaExportacion();
    void mostrarBalance(BalanceDia balance);
}
