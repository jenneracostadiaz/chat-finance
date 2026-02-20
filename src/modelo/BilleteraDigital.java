package modelo;

/** Billetera digital vinculada a un número de celular (ej: Yape, Plin). */
public class BilleteraDigital extends CuentaFinanciera {

    private String alias;
    private String proveedor;

    public BilleteraDigital(Integer id, Integer usuarioId, String numeroCuenta, Double saldo,
                            String alias, String proveedor) {
        super(id, usuarioId, numeroCuenta, saldo);
        this.alias = alias;
        this.proveedor = proveedor;
    }

    public BilleteraDigital(Integer usuarioId, String numeroCuenta, Double saldo,
                            String alias, String proveedor) {
        super(usuarioId, numeroCuenta, saldo);
        this.alias = alias;
        this.proveedor = proveedor;
    }

    @Override
    public String obtenerDetalleImprimible() {
        return String.format("Billetera %s | %s | N. %s", alias, proveedor, getNumeroCuenta());
    }

    @Override
    public String getTipoCuenta() {
        return "BILLETERA";
    }

    @Override
    public boolean validarCuenta() {
        return getNumeroCuenta() != null && !getNumeroCuenta().isEmpty()
                && alias != null && proveedor != null;
    }

    public String generarQR() {
        return "QR:" + proveedor + ":" + alias + ":" + getNumeroCuenta();
    }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
}
