package modelo;

/**
 * Clase abstracta que representa una cuenta financiera.
 * Subclases concretas: {@link BilleteraDigital}, {@link CuentaBancaria}.
 */
public abstract class CuentaFinanciera {

    private Integer id;
    private Integer usuarioId;
    private String numeroCuenta;
    private Double saldo;

    public CuentaFinanciera(Integer id, Integer usuarioId, String numeroCuenta, Double saldo) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldo;
    }

    public CuentaFinanciera(Integer usuarioId, String numeroCuenta, Double saldo) {
        this.usuarioId = usuarioId;
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldo;
    }

    /**
     * Retorna una cadena descriptiva legible de la cuenta, específica por tipo.
     * Implementada mediante polimorfismo en cada subclase.
     */
    public abstract String obtenerDetalleImprimible();

    /** Retorna el discriminador de tipo usado en la tabla Single Table Inheritance. */
    public abstract String getTipoCuenta();

    /** Valida que los campos obligatorios del tipo de cuenta sean correctos. */
    public abstract boolean validarCuenta();

    // Getters y Setters

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String numeroCuenta) { this.numeroCuenta = numeroCuenta; }

    public Double getSaldo() { return saldo; }
    public void setSaldo(Double saldo) { this.saldo = saldo; }
}
