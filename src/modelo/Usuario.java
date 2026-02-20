package modelo;

import java.util.ArrayList;
import java.util.List;

/** Representa un usuario del sistema autenticado por numero de WhatsApp. */
public class Usuario {

    private Integer id;
    private String numeroWhatsApp;
    private String nombre;
    private List<CuentaFinanciera> cuentas;

    public Usuario() {
        this.cuentas = new ArrayList<>();
    }

    public Usuario(String numeroWhatsApp, String nombre) {
        this();
        this.numeroWhatsApp = numeroWhatsApp;
        this.nombre = nombre;
    }

    public Usuario(Integer id, String numeroWhatsApp, String nombre) {
        this(numeroWhatsApp, nombre);
        this.id = id;
    }

    public void agregarCuenta(CuentaFinanciera cuenta) { this.cuentas.add(cuenta); }
    public void removerCuenta(CuentaFinanciera cuenta) { this.cuentas.remove(cuenta); }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNumeroWhatsApp() { return numeroWhatsApp; }
    public void setNumeroWhatsApp(String numeroWhatsApp) { this.numeroWhatsApp = numeroWhatsApp; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<CuentaFinanciera> getCuentas() { return cuentas; }
}
