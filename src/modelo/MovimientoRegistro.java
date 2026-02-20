package modelo;

import java.time.LocalDateTime;

/**
 * Modelo de persistencia para registrar movimientos financieros en la BD.
 * Representa una fila de la tabla `transacciones`.
 *
 * FASE 4: Categorías y Reportes Analíticos — añadida columna `categoria`.
 */
public class MovimientoRegistro {

    /** Tipos de movimiento permitidos */
    public enum Tipo {
        INGRESO, GASTO, TRANSFERENCIA
    }

    /** Categorías predefinidas para GASTOS */
    public static final String[] CATEGORIAS_GASTO = {
        "Alimentación 🍔",
        "Transporte 🚕",
        "Servicios 💡",
        "Entretenimiento 🎬",
        "Otros 📦"
    };

    /** Categorías predefinidas para INGRESOS */
    public static final String[] CATEGORIAS_INGRESO = {
        "Sueldo 💰",
        "Freelance 💻",
        "Otros 📦"
    };

    private Integer id;
    private Integer cuentaOrigenId;
    private Integer cuentaDestinoId;   // null para INGRESO y GASTO
    private Tipo tipo;
    private Double monto;
    private LocalDateTime fecha;
    private String descripcion;
    private String categoria;          // FASE 4: clasificación analítica

    // ─────────────────────────────────────────
    // Constructores
    // ─────────────────────────────────────────

    /** Constructor completo — usado al recuperar registros de la BD */
    public MovimientoRegistro(Integer id, Integer cuentaOrigenId, Integer cuentaDestinoId,
                               Tipo tipo, Double monto, LocalDateTime fecha,
                               String descripcion, String categoria) {
        this.id              = id;
        this.cuentaOrigenId  = cuentaOrigenId;
        this.cuentaDestinoId = cuentaDestinoId;
        this.tipo            = tipo;
        this.monto           = monto;
        this.fecha           = fecha;
        this.descripcion     = descripcion;
        this.categoria       = categoria;
    }

    /** Constructor para crear un nuevo registro (sin ID, fecha la asigna la BD) */
    public MovimientoRegistro(Integer cuentaOrigenId, Integer cuentaDestinoId,
                               Tipo tipo, Double monto, String descripcion, String categoria) {
        this.cuentaOrigenId  = cuentaOrigenId;
        this.cuentaDestinoId = cuentaDestinoId;
        this.tipo            = tipo;
        this.monto           = monto;
        this.descripcion     = descripcion;
        this.categoria       = categoria;
        this.fecha           = LocalDateTime.now();
    }

    // ─────────────────────────────────────────
    // Getters y Setters
    // ─────────────────────────────────────────

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCuentaOrigenId() { return cuentaOrigenId; }
    public void setCuentaOrigenId(Integer cuentaOrigenId) { this.cuentaOrigenId = cuentaOrigenId; }

    public Integer getCuentaDestinoId() { return cuentaDestinoId; }
    public void setCuentaDestinoId(Integer cuentaDestinoId) { this.cuentaDestinoId = cuentaDestinoId; }

    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    @Override
    public String toString() {
        return String.format("[%s][%s] %s | S/ %.2f | %s",
                tipo,
                categoria != null ? categoria : "-",
                descripcion,
                monto,
                fecha);
    }
}
