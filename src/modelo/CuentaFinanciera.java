package modelo;

/**
 * Clase abstracta que representa una cuenta financiera genérica.
 * Implementa el patrón Template Method con herencia y polimorfismo.
 * FASE 2: Gestión de Cuentas y Saldos
 */
public abstract class CuentaFinanciera {
    private Integer id;
    private Integer usuarioId;
    private String numeroCuenta;
    private Double saldo;

    /**
     * Constructor completo (usado al recuperar de la BD)
     */
    public CuentaFinanciera(Integer id, Integer usuarioId, String numeroCuenta, Double saldo) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldo;
    }

    /**
     * Constructor sin ID (usado al crear nuevas cuentas)
     */
    public CuentaFinanciera(Integer usuarioId, String numeroCuenta, Double saldo) {
        this.usuarioId = usuarioId;
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldo;
    }

    /**
     * Método abstracto que debe ser implementado por las subclases.
     * Devuelve una descripción detallada de la cuenta según su tipo.
     * @return String con el detalle de la cuenta
     */
    public abstract String getDetalle();

    /**
     * Método abstracto para validar la cuenta.
     * @return true si la cuenta es válida
     */
    public abstract boolean validarCuenta();

    /**
     * Obtiene el tipo de cuenta (discriminador para la BD)
     * @return String con el tipo de cuenta
     */
    public abstract String getTipoCuenta();

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public Double getSaldo() {
        return saldo;
    }

    public void setSaldo(Double saldo) {
        this.saldo = saldo;
    }
}
