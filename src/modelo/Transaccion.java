package modelo;

import java.util.Date;

public abstract class Transaccion {
    private String idTransaccion;
    private Date fecha;
    private Double monto;
    private Boolean esRecurrente;
    private Comision comision; // Composición
    private Recordatorio recordatorio; // Composición (opcional)
    private CuentaFinanciera cuenta; // Asociación

    public Transaccion(String idTransaccion, Date fecha, Double monto, Boolean esRecurrente, CuentaFinanciera cuenta) {
        this.idTransaccion = idTransaccion;
        this.fecha = fecha;
        this.monto = monto;
        this.esRecurrente = esRecurrente;
        this.cuenta = cuenta;
        this.comision = new Comision(1.0); // Comisión por defecto del 1%
    }

    public abstract void procesar();

    public void agregarRecordatorio(Recordatorio recordatorio) {
        if (this.esRecurrente) {
            this.recordatorio = recordatorio;
        }
    }

    // Getters y Setters
    public String getIdTransaccion() {
        return idTransaccion;
    }

    public void setIdTransaccion(String idTransaccion) {
        this.idTransaccion = idTransaccion;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Double getMonto() {
        return monto;
    }

    public void setMonto(Double monto) {
        this.monto = monto;
    }

    public Boolean getEsRecurrente() {
        return esRecurrente;
    }

    public void setEsRecurrente(Boolean esRecurrente) {
        this.esRecurrente = esRecurrente;
    }

    public Comision getComision() {
        return comision;
    }

    public void setComision(Comision comision) {
        this.comision = comision;
    }

    public Recordatorio getRecordatorio() {
        return recordatorio;
    }

    public CuentaFinanciera getCuenta() {
        return cuenta;
    }

    public void setCuenta(CuentaFinanciera cuenta) {
        this.cuenta = cuenta;
    }
}
