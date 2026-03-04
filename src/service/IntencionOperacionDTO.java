package service;

/**
 * DTO (Data Transfer Object) que representa la intencion financiera
 * extraida del texto en lenguaje natural por el modelo de IA.
 *
 * Los campos se mapean directamente desde el JSON devuelto por Ollama.
 */
public class IntencionOperacionDTO {

    /** Tipo de operacion: "INGRESO" o "GASTO". */
    private String tipo;

    /** Monto de la operacion en soles. */
    private Double monto;

    /**
     * Categoria del movimiento.
     * Gastos: Alimentacion, Transporte, Servicios, Entretenimiento, Otros.
     * Ingresos: Sueldo, Freelance, Otros.
     */
    private String categoria;

    /**
     * Nombre o alias de la cuenta mencionada por el usuario
     * (ej: "Yape", "BCP", "Plin").
     */
    private String cuenta;

    /** Descripcion breve del movimiento. */
    private String descripcion;

    // Constructor vacio requerido por Gson
    public IntencionOperacionDTO() {}

    public IntencionOperacionDTO(String tipo, Double monto, String categoria,
                                  String cuenta, String descripcion) {
        this.tipo        = tipo;
        this.monto       = monto;
        this.categoria   = categoria;
        this.cuenta      = cuenta;
        this.descripcion = descripcion;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters y Setters
    // ─────────────────────────────────────────────────────────────────────────

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getCuenta() { return cuenta; }
    public void setCuenta(String cuenta) { this.cuenta = cuenta; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /** Valida que los campos criticos tengan valores utilizables. */
    public boolean esValido() {
        return tipo != null && !tipo.isBlank()
                && monto != null && monto > 0
                && cuenta != null && !cuenta.isBlank();
    }

    @Override
    public String toString() {
        return String.format("IntencionOperacionDTO{tipo='%s', monto=%.2f, categoria='%s', cuenta='%s', descripcion='%s'}",
                tipo, monto != null ? monto : 0.0, categoria, cuenta, descripcion);
    }
}

