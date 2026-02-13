package modelo;

/**
 * Clase que representa una Billetera Digital (ej: Yape, Plin).
 * Extiende CuentaFinanciera e implementa polimorfismo.
 * FASE 2: Gestión de Cuentas y Saldos
 */
public class BilleteraDigital extends CuentaFinanciera {
    private String alias;      // Ej: "Yape", "Plin"
    private String proveedor;  // Ej: "BCP", "Interbank"

    /**
     * Constructor completo (usado al recuperar de la BD)
     */
    public BilleteraDigital(Integer id, Integer usuarioId, String numeroCuenta, Double saldo,
                           String alias, String proveedor) {
        super(id, usuarioId, numeroCuenta, saldo);
        this.alias = alias;
        this.proveedor = proveedor;
    }

    /**
     * Constructor sin ID (usado al crear nuevas billeteras)
     */
    public BilleteraDigital(Integer usuarioId, String numeroCuenta, Double saldo,
                           String alias, String proveedor) {
        super(usuarioId, numeroCuenta, saldo);
        this.alias = alias;
        this.proveedor = proveedor;
    }

    @Override
    public String getDetalle() {
        return String.format("💳 %s | %s | Número: %s",
            alias,
            proveedor,
            getNumeroCuenta());
    }

    @Override
    public String getTipoCuenta() {
        return "BILLETERA";
    }

    @Override
    public boolean validarCuenta() {
        // Lógica de validación para billetera digital
        return getNumeroCuenta() != null &&
               !getNumeroCuenta().isEmpty() &&
               alias != null &&
               proveedor != null;
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
