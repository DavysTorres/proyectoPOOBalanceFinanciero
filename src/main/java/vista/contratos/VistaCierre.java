package vista.contratos;
import modelo.CierreDiario;
import java.math.BigDecimal;
public interface VistaCierre extends VistaBase {
    String getFecha();
    String getEfectivoContado();
    void mostrarEfectivoSistema(BigDecimal efectivo);
    void mostrarCierre(CierreDiario cierre);
}
