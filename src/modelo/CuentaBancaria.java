package modelo;

/**
 * Clase que representa una Cuenta Bancaria tradicional.
 * Extiende CuentaFinanciera e implementa polimorfismo.
 * FASE 2: Gestión de Cuentas y Saldos
 */
public class CuentaBancaria extends CuentaFinanciera {
    private String banco;  // Ej: "BCP", "Interbank", "BBVA"
    private String cci;    // Código de Cuenta Interbancario (20 dígitos)

    /**
     * Constructor completo (usado al recuperar de la BD)
     */
    public CuentaBancaria(Integer id, Integer usuarioId, String numeroCuenta, Double saldo,
                         String banco, String cci) {
        super(id, usuarioId, numeroCuenta, saldo);
        this.banco = banco;
        this.cci = cci;
    }

    /**
     * Constructor sin ID (usado al crear nuevas cuentas)
     */
    public CuentaBancaria(Integer usuarioId, String numeroCuenta, Double saldo,
                         String banco, String cci) {
        super(usuarioId, numeroCuenta, saldo);
        this.banco = banco;
        this.cci = cci;
    }

    @Override
    public String getDetalle() {
        return String.format("🏦 Banco %s | Cuenta: %s | CCI: %s",
            banco,
            getNumeroCuenta(),
            cci != null ? cci.substring(0, Math.min(8, cci.length())) + "..." : "N/A");
    }

    @Override
    public String getTipoCuenta() {
        return "BANCO";
    }

    @Override
    public boolean validarCuenta() {
        // Lógica de validación para cuenta bancaria
        return getNumeroCuenta() != null &&
               !getNumeroCuenta().isEmpty() &&
               banco != null;
    }

    public boolean validarInterbancario() {
        // Lógica para validar transferencias interbancarias
        return cci != null && cci.length() == 20;
    }

    // Getters y Setters
    public String getBanco() {
        return banco;
    }

    public void setBanco(String banco) {
        this.banco = banco;
    }

    public String getCci() {
        return cci;
    }

    public void setCci(String cci) {
        this.cci = cci;
    }
}
