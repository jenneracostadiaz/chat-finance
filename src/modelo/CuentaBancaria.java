package modelo;

public class CuentaBancaria extends CuentaFinanciera {
    private String banco;
    private String cci;

    public CuentaBancaria(String idCuenta, String numeroCuenta, Double saldo, String banco, String cci) {
        super(idCuenta, numeroCuenta, saldo);
        this.banco = banco;
        this.cci = cci;
    }

    @Override
    public boolean validarCuenta() {
        // Lógica de validación para cuenta bancaria
        return getNumeroCuenta() != null && !getNumeroCuenta().isEmpty() && banco != null;
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
