package modelo;

import java.time.LocalDateTime;

/**
 * Modelo de persistencia para registrar movimientos financieros en la BD.
 * Representa una fila de la tabla `transacciones`.
 *
 * Tipos soportados:
 *   - INGRESO      : Dinero que entra a una cuenta
 *   - GASTO        : Dinero que sale de una cuenta
 *   - TRANSFERENCIA: Movimiento entre dos cuentas del mismo usuario
 *
 * FASE 3: Motor de Transacciones
 */
public class MovimientoRegistro {

    /** Tipos de movimiento permitidos */
    public enum Tipo {
        INGRESO, GASTO, TRANSFERENCIA
    }

    private Integer id;
    private Integer cuentaOrigenId;
    private Integer cuentaDestinoId;   // null para INGRESO y GASTO
    private Tipo tipo;
    private Double monto;
    private LocalDateTime fecha;
    private String descripcion;

    // ─────────────────────────────────────────
    // Constructores
    // ─────────────────────────────────────────

    /** Constructor para recuperar un registro existente de la BD */
    public MovimientoRegistro(Integer id, Integer cuentaOrigenId, Integer cuentaDestinoId,
                               Tipo tipo, Double monto, LocalDateTime fecha, String descripcion) {
        this.id              = id;
        this.cuentaOrigenId  = cuentaOrigenId;
        this.cuentaDestinoId = cuentaDestinoId;
        this.tipo            = tipo;
        this.monto           = monto;
        this.fecha           = fecha;
        this.descripcion     = descripcion;
    }

    /** Constructor para crear un nuevo registro (sin ID, sin fecha — la BD la asigna) */
    public MovimientoRegistro(Integer cuentaOrigenId, Integer cuentaDestinoId,
                               Tipo tipo, Double monto, String descripcion) {
        this.cuentaOrigenId  = cuentaOrigenId;
        this.cuentaDestinoId = cuentaDestinoId;
        this.tipo            = tipo;
        this.monto           = monto;
        this.descripcion     = descripcion;
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

    @Override
    public String toString() {
        return String.format("[%s] %s | Monto: S/ %.2f | %s",
                tipo, descripcion, monto, fecha);
    }
}

