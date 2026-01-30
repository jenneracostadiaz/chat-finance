package modelo;

import java.util.ArrayList;
import java.util.List;

public class Usuario {
    private String idUsuario;
    private String numeroWhatsApp;
    private String pinSeguridad;
    private List<CuentaFinanciera> cuentas; // Agregación
    private List<Transaccion> transacciones; // Asociación

    public Usuario(String idUsuario, String numeroWhatsApp, String pinSeguridad) {
        this.idUsuario = idUsuario;
        this.numeroWhatsApp = numeroWhatsApp;
        this.pinSeguridad = pinSeguridad;
        this.cuentas = new ArrayList<>();
        this.transacciones = new ArrayList<>();
    }

    public boolean autenticar(String pin) {
        return this.pinSeguridad.equals(pin);
    }

    public void registrarTransaccion(Transaccion transaccion) {
        this.transacciones.add(transaccion);
    }

    public void agregarCuenta(CuentaFinanciera cuenta) {
        this.cuentas.add(cuenta);
    }

    public void removerCuenta(CuentaFinanciera cuenta) {
        this.cuentas.remove(cuenta);
    }

    // Getters y Setters
    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNumeroWhatsApp() {
        return numeroWhatsApp;
    }

    public void setNumeroWhatsApp(String numeroWhatsApp) {
        this.numeroWhatsApp = numeroWhatsApp;
    }

    public String getPinSeguridad() {
        return pinSeguridad;
    }

    public void setPinSeguridad(String pinSeguridad) {
        this.pinSeguridad = pinSeguridad;
    }

    public List<CuentaFinanciera> getCuentas() {
        return cuentas;
    }

    public List<Transaccion> getTransacciones() {
        return transacciones;
    }
}
