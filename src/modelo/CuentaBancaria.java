package modelo;

/** Cuenta bancaria tradicional con CCI para transferencias interbancarias. */
public class CuentaBancaria extends CuentaFinanciera {

    private String banco;
    private String cci;

    public CuentaBancaria(Integer id, Integer usuarioId, String numeroCuenta, Double saldo,
                          String banco, String cci) {
        super(id, usuarioId, numeroCuenta, saldo);
        this.banco = banco;
        this.cci = cci;
    }

    public CuentaBancaria(Integer usuarioId, String numeroCuenta, Double saldo,
                          String banco, String cci) {
        super(usuarioId, numeroCuenta, saldo);
        this.banco = banco;
        this.cci = cci;
    }

    @Override
    public String obtenerDetalleImprimible() {
        String cciResumido = (cci != null) ? cci.substring(0, Math.min(8, cci.length())) + "..." : "N/A";
        return String.format("Banco %s | Cuenta: %s | CCI: %s", banco, getNumeroCuenta(), cciResumido);
    }

    @Override
    public String getTipoCuenta() {
        return "BANCO";
    }

    @Override
    public boolean validarCuenta() {
        return getNumeroCuenta() != null && !getNumeroCuenta().isEmpty() && banco != null;
    }

    public boolean validarInterbancario() {
        return cci != null && cci.length() == 20;
    }

    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }

    public String getCci() { return cci; }
    public void setCci(String cci) { this.cci = cci; }
}
