package modelo;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * Configuracion del negocio; no conserva ventas ni compras en memoria.
 */
public class Negocio {
    // Los porcentajes se expresan de 0 a 100, por ejemplo 70 significa 70 por ciento.
    private String nombre;
    private int diasOperacion;
    private LocalTime horaApertura;
    private LocalTime horaCierre;
    private BigDecimal umbralAmarillo;
    private BigDecimal umbralMargenBajo;

    // Los umbrales iniciales son los exigidos por la guia.
    public Negocio(String nombre, int diasOperacion, LocalTime horaApertura, LocalTime horaCierre) {
        this(nombre, diasOperacion, horaApertura, horaCierre,
                new BigDecimal("70"), new BigDecimal("15"));
    }

    // Permite cargar una configuracion completa con umbrales personalizados.
    public Negocio(String nombre, int diasOperacion, LocalTime horaApertura, LocalTime horaCierre,
                   BigDecimal umbralAmarillo, BigDecimal umbralMargenBajo) {
        setNombre(nombre);
        setDiasOperacion(diasOperacion);
        setHorario(horaApertura, horaCierre);
        setUmbralAmarillo(umbralAmarillo);
        setUmbralMargenBajo(umbralMargenBajo);
    }

    // El nombre identifica el negocio en los reportes.
    public String getNombre() {
        return nombre;
    }

    public final void setNombre(String nombre) {
        this.nombre = ValidacionModelo.texto(nombre, "El nombre del negocio");
    }

    // Se valida el divisor utilizado en costos fijos diarios.
    public int getDiasOperacion() {
        return diasOperacion;
    }

    public final void setDiasOperacion(int diasOperacion) {
        this.diasOperacion = ValidacionModelo.dias(diasOperacion);
    }

    // Cambia la apertura solo si el horario resultante sigue siendo valido.
    public LocalTime getHoraApertura() {
        return horaApertura;
    }

    public final void setHoraApertura(LocalTime horaApertura) {
        setHorario(horaApertura, this.horaCierre);
    }

    // La guia exige un cierre posterior a la apertura del mismo dia.
    public LocalTime getHoraCierre() {
        return horaCierre;
    }

    public final void setHoraCierre(LocalTime horaCierre) {
        setHorario(this.horaApertura, horaCierre);
    }

    // Valida ambos valores antes de asignarlos para no dejar un horario inconsistente.
    public final void setHorario(LocalTime apertura, LocalTime cierre) {
        ValidacionModelo.obligatorio(apertura, "La hora de apertura");
        ValidacionModelo.obligatorio(cierre, "La hora de cierre");
        if (!cierre.isAfter(apertura))
            throw new IllegalArgumentException("El cierre debe ser posterior a la apertura");
        this.horaApertura = apertura;
        this.horaCierre = cierre;
    }

    // Umbral del semaforo amarillo expresado como porcentaje.
    public BigDecimal getUmbralAmarillo() {
        return umbralAmarillo;
    }

    public final void setUmbralAmarillo(BigDecimal umbralAmarillo) {
        this.umbralAmarillo = ValidacionModelo.porcentaje(umbralAmarillo, "El umbral amarillo");
    }

    // Umbral que permite detectar productos con margen bajo.
    public BigDecimal getUmbralMargenBajo() {
        return umbralMargenBajo;
    }

    public final void setUmbralMargenBajo(BigDecimal umbralMargenBajo) {
        this.umbralMargenBajo = ValidacionModelo.porcentaje(umbralMargenBajo, "El umbral de margen");
    }
}
