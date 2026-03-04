package service;

/**
 * DTO universal del Asistente IA.
 * Representa cualquier intencion que el usuario exprese en lenguaje natural.
 * Gson mapea los campos directamente desde el JSON devuelto por Ollama.
 *
 * Intenciones soportadas:
 *   REGISTRAR_TRANSACCION, CREAR_CUENTA, VER_REPORTE, VER_SALDOS
 */
public class IntencionOperacionDTO {

    // ── Clasificacion principal ───────────────────────────────────────────────

    /**
     * Intencion detectada por la IA.
     * Valores: "REGISTRAR_TRANSACCION" | "CREAR_CUENTA" | "VER_REPORTE" | "VER_SALDOS"
     */
    private String intencion;

    // ── Campos para REGISTRAR_TRANSACCION ─────────────────────────────────────

    /** "INGRESO" o "GASTO". Null si no aplica. */
    private String tipoTransaccion;

    /** Monto en soles. Null si no aplica. */
    private Double monto;

    /** Categoria del movimiento. Null si no aplica. */
    private String categoria;

    // ── Campos compartidos (transaccion y crear cuenta) ───────────────────────

    /** Nombre/alias de la cuenta existente o de la nueva cuenta a crear. */
    private String nombreCuenta;

    // ── Campos para CREAR_CUENTA ──────────────────────────────────────────────

    /** "BANCO" o "BILLETERA". Null si no aplica. */
    private String tipoCuentaNueva;

    // ── Campo general ─────────────────────────────────────────────────────────

    /** Descripcion breve del movimiento o nota adicional. */
    private String descripcion;

    // Constructor vacio requerido por Gson
    public IntencionOperacionDTO() {}

    // ── Validaciones por intencion ────────────────────────────────────────────

    /** Verifica que los campos criticos para REGISTRAR_TRANSACCION esten presentes. */
    public boolean esTransaccionValida() {
        return tipoTransaccion != null && !tipoTransaccion.isBlank()
                && monto != null && monto > 0
                && nombreCuenta != null && !nombreCuenta.isBlank();
    }

    /** Verifica que los campos criticos para CREAR_CUENTA esten presentes. */
    public boolean esCrearCuentaValida() {
        return nombreCuenta != null && !nombreCuenta.isBlank()
                && tipoCuentaNueva != null && !tipoCuentaNueva.isBlank();
    }

    /** Retorna true si la intencion es reconocida. */
    public boolean tieneIntencionValida() {
        return intencion != null && (
                intencion.equals("REGISTRAR_TRANSACCION") ||
                intencion.equals("CREAR_CUENTA")          ||
                intencion.equals("VER_REPORTE")           ||
                intencion.equals("VER_SALDOS")
        );
    }

    // ── Getters y Setters ─────────────────────────────────────────────────────

    public String getIntencion() { return intencion; }
    public void setIntencion(String intencion) {
        this.intencion = intencion != null ? intencion.toUpperCase().trim() : null;
    }

    public String getTipoTransaccion() { return tipoTransaccion; }
    public void setTipoTransaccion(String tipoTransaccion) {
        this.tipoTransaccion = tipoTransaccion != null ? tipoTransaccion.toUpperCase().trim() : null;
    }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getNombreCuenta() { return nombreCuenta; }
    public void setNombreCuenta(String nombreCuenta) { this.nombreCuenta = nombreCuenta; }

    public String getTipoCuentaNueva() { return tipoCuentaNueva; }
    public void setTipoCuentaNueva(String tipoCuentaNueva) {
        this.tipoCuentaNueva = tipoCuentaNueva != null ? tipoCuentaNueva.toUpperCase().trim() : null;
    }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    // Alias de compatibilidad para codigo existente que usa getCuenta()
    public String getCuenta() { return nombreCuenta; }
    public void setCuenta(String cuenta) { this.nombreCuenta = cuenta; }

    // Alias de compatibilidad para codigo existente que usa getTipo()
    public String getTipo() { return tipoTransaccion; }
    public void setTipo(String tipo) { setTipoTransaccion(tipo); }

    @Override
    public String toString() {
        return String.format(
            "RespuestaIADTO{intencion='%s', tipoTx='%s', monto=%s, categoria='%s', " +
            "nombreCuenta='%s', tipoCuentaNueva='%s', descripcion='%s'}",
            intencion, tipoTransaccion,
            monto != null ? String.format("%.2f", monto) : "null",
            categoria, nombreCuenta, tipoCuentaNueva, descripcion);
    }
}
