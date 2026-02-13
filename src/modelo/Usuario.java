package modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo que representa un Usuario del sistema.
 * En la Fase 1, la autenticación se realiza únicamente por número de WhatsApp.
 */
public class Usuario {
    private Integer id; // ID autoincremental de la base de datos
    private String numeroWhatsApp; // Llave de autenticación única
    private String nombre; // Nombre del usuario
    private List<CuentaFinanciera> cuentas; // Agregación
    private List<Transaccion> transacciones; // Asociación

    /**
     * Constructor vacío para instanciación desde DAO.
     */
    public Usuario() {
        this.cuentas = new ArrayList<>();
        this.transacciones = new ArrayList<>();
    }

    /**
     * Constructor con parámetros para crear nuevo usuario.
     * @param numeroWhatsApp Número de WhatsApp del usuario
     * @param nombre Nombre del usuario
     */
    public Usuario(String numeroWhatsApp, String nombre) {
        this();
        this.numeroWhatsApp = numeroWhatsApp;
        this.nombre = nombre;
    }

    /**
     * Constructor completo con ID (usado al recuperar de DB).
     * @param id ID del usuario en la base de datos
     * @param numeroWhatsApp Número de WhatsApp del usuario
     * @param nombre Nombre del usuario
     */
    public Usuario(Integer id, String numeroWhatsApp, String nombre) {
        this(numeroWhatsApp, nombre);
        this.id = id;
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

    // ==================== Getters y Setters ====================

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumeroWhatsApp() {
        return numeroWhatsApp;
    }

    public void setNumeroWhatsApp(String numeroWhatsApp) {
        this.numeroWhatsApp = numeroWhatsApp;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<CuentaFinanciera> getCuentas() {
        return cuentas;
    }

    public List<Transaccion> getTransacciones() {
        return transacciones;
    }
}
