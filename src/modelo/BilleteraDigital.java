package modelo;

public class BilleteraDigital extends CuentaFinanciera {
    private String alias;
    private String proveedor;

    public BilleteraDigital(String idCuenta, String numeroCuenta, Double saldo, String alias, String proveedor) {
        super(idCuenta, numeroCuenta, saldo);
        this.alias = alias;
        this.proveedor = proveedor;
    }

    @Override
    public boolean validarCuenta() {
        // Lógica de validación para billetera digital
        return getNumeroCuenta() != null && !getNumeroCuenta().isEmpty();
    }

    public String generarQR() {
        // Lógica para generar código QR
        return "QR:" + getProveedor() + ":" + getAlias() + ":" + getNumeroCuenta();
    }

    // Getters y Setters
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }
}
