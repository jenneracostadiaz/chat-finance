package dao;

import modelo.MovimientoRegistro;
import util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestionar los movimientos financieros (ingresos, gastos, transferencias).
 *
 * PRINCIPIO CLAVE: Atomicidad con transacciones SQL.
 * Cada operación que toca múltiples tablas usa:
 *   connection.setAutoCommit(false) → operaciones → connection.commit()
 * Si algo falla → connection.rollback(), dejando la BD intacta.
 *
 * FASE 3: Motor de Transacciones
 */
public class TransaccionDAO {

    // ─────────────────────────────────────────────────────────────────────────
    // INGRESO: suma dinero a una cuenta
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registra un ingreso de dinero en una cuenta.
     * Operaciones atómicas:
     *   1. INSERT en transacciones
     *   2. UPDATE saldo en cuentas (saldo += monto)
     *
     * @param cuentaId   ID de la cuenta destino del ingreso
     * @param monto      Monto a ingresar (debe ser positivo)
     * @param descripcion Descripción del ingreso (ej: "Sueldo enero")
     * @return MovimientoRegistro creado, o null si falló
     */
    public MovimientoRegistro registrarIngreso(int cuentaId, double monto, String descripcion) {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        // SQL para insertar la transacción
        String sqlInsert = "INSERT INTO transacciones (cuenta_origen_id, cuenta_destino_id, tipo, monto, descripcion) " +
                           "VALUES (?, NULL, 'INGRESO', ?, ?)";

        // SQL para actualizar saldo: INGRESO suma al saldo
        String sqlUpdate = "UPDATE cuentas SET saldo = saldo + ? WHERE id = ?";

        try {
            // ── Inicio de transacción atómica ──────────────────────────────
            conn.setAutoCommit(false);

            // Paso 1: Registrar el movimiento
            int nuevoId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, cuentaId);
                pstmt.setDouble(2, monto);
                pstmt.setString(3, descripcion);
                pstmt.executeUpdate();

                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) nuevoId = keys.getInt(1);
                }
            }

            // Paso 2: Actualizar el saldo de la cuenta
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                pstmt.setDouble(1, monto);
                pstmt.setInt(2, cuentaId);
                pstmt.executeUpdate();
            }

            // ── Confirmar ambas operaciones ────────────────────────────────
            conn.commit();
            conn.setAutoCommit(true);

            // Construir y devolver el objeto resultado
            MovimientoRegistro mov = new MovimientoRegistro(
                cuentaId, null, MovimientoRegistro.Tipo.INGRESO, monto, descripcion
            );
            mov.setId(nuevoId);
            return mov;

        } catch (SQLException e) {
            // ── Revertir todo si algo falló ────────────────────────────────
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("✗ Error al registrar ingreso. Se hizo ROLLBACK.");
            e.printStackTrace();
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GASTO: resta dinero de una cuenta
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registra un gasto en una cuenta.
     * NOTA: La validación de saldo suficiente se hace en el Controlador,
     * pero aquí también protegemos contra saldo negativo con CHECK en la BD.
     *
     * Operaciones atómicas:
     *   1. INSERT en transacciones
     *   2. UPDATE saldo en cuentas (saldo -= monto)
     *
     * @param cuentaId    ID de la cuenta de origen del gasto
     * @param monto       Monto a gastar (debe ser positivo)
     * @param descripcion Descripción del gasto (ej: "Almuerzo")
     * @return MovimientoRegistro creado, o null si falló
     */
    public MovimientoRegistro registrarGasto(int cuentaId, double monto, String descripcion) {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        String sqlInsert = "INSERT INTO transacciones (cuenta_origen_id, cuenta_destino_id, tipo, monto, descripcion) " +
                           "VALUES (?, NULL, 'GASTO', ?, ?)";
        String sqlUpdate = "UPDATE cuentas SET saldo = saldo - ? WHERE id = ?";

        try {
            conn.setAutoCommit(false);

            // Paso 1: Insertar el movimiento
            int nuevoId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, cuentaId);
                pstmt.setDouble(2, monto);
                pstmt.setString(3, descripcion);
                pstmt.executeUpdate();

                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) nuevoId = keys.getInt(1);
                }
            }

            // Paso 2: Restar el monto del saldo
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                pstmt.setDouble(1, monto);
                pstmt.setInt(2, cuentaId);
                pstmt.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

            MovimientoRegistro mov = new MovimientoRegistro(
                cuentaId, null, MovimientoRegistro.Tipo.GASTO, monto, descripcion
            );
            mov.setId(nuevoId);
            return mov;

        } catch (SQLException e) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("✗ Error al registrar gasto. Se hizo ROLLBACK.");
            e.printStackTrace();
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TRANSFERENCIA: mueve dinero entre dos cuentas del mismo usuario
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Realiza una transferencia entre dos cuentas.
     * Operaciones atómicas (3 en total):
     *   1. INSERT en transacciones (tipo TRANSFERENCIA con origen y destino)
     *   2. UPDATE saldo cuenta origen  (saldo -= monto)
     *   3. UPDATE saldo cuenta destino (saldo += monto)
     *
     * @param origenId  ID de la cuenta que envía el dinero
     * @param destinoId ID de la cuenta que recibe el dinero
     * @param monto     Monto a transferir (debe ser positivo)
     * @param descripcion Descripción opcional
     * @return MovimientoRegistro creado, o null si falló
     */
    public MovimientoRegistro realizarTransferencia(int origenId, int destinoId, double monto, String descripcion) {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        String sqlInsert = "INSERT INTO transacciones (cuenta_origen_id, cuenta_destino_id, tipo, monto, descripcion) " +
                           "VALUES (?, ?, 'TRANSFERENCIA', ?, ?)";
        String sqlDescontar = "UPDATE cuentas SET saldo = saldo - ? WHERE id = ?";
        String sqlAcreditar  = "UPDATE cuentas SET saldo = saldo + ? WHERE id = ?";

        try {
            conn.setAutoCommit(false);

            // Paso 1: Registrar la transacción
            int nuevoId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, origenId);
                pstmt.setInt(2, destinoId);
                pstmt.setDouble(3, monto);
                pstmt.setString(4, descripcion);
                pstmt.executeUpdate();

                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) nuevoId = keys.getInt(1);
                }
            }

            // Paso 2: Descontar de la cuenta origen
            try (PreparedStatement pstmt = conn.prepareStatement(sqlDescontar)) {
                pstmt.setDouble(1, monto);
                pstmt.setInt(2, origenId);
                pstmt.executeUpdate();
            }

            // Paso 3: Acreditar en la cuenta destino
            try (PreparedStatement pstmt = conn.prepareStatement(sqlAcreditar)) {
                pstmt.setDouble(1, monto);
                pstmt.setInt(2, destinoId);
                pstmt.executeUpdate();
            }

            // ── Confirmar las 3 operaciones como una sola unidad ──────────
            conn.commit();
            conn.setAutoCommit(true);

            MovimientoRegistro mov = new MovimientoRegistro(
                origenId, destinoId, MovimientoRegistro.Tipo.TRANSFERENCIA, monto, descripcion
            );
            mov.setId(nuevoId);
            return mov;

        } catch (SQLException e) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("✗ Error en la transferencia. Se hizo ROLLBACK de las 3 operaciones.");
            e.printStackTrace();
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LISTAR últimos movimientos de un usuario
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Obtiene los últimos N movimientos de todas las cuentas de un usuario.
     *
     * @param usuarioId ID del usuario
     * @param limite    Cantidad máxima de registros a devolver
     * @return Lista de movimientos ordenados por fecha descendente
     */
    public List<MovimientoRegistro> listarUltimosMovimientos(int usuarioId, int limite) {
        List<MovimientoRegistro> movimientos = new ArrayList<>();

        // JOIN con cuentas para filtrar solo las del usuario
        String sql = "SELECT t.id, t.cuenta_origen_id, t.cuenta_destino_id, t.tipo, t.monto, t.fecha, t.descripcion " +
                     "FROM transacciones t " +
                     "INNER JOIN cuentas c ON (t.cuenta_origen_id = c.id OR t.cuenta_destino_id = c.id) " +
                     "WHERE c.usuario_id = ? " +
                     "GROUP BY t.id " +
                     "ORDER BY t.fecha DESC " +
                     "LIMIT ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, usuarioId);
            pstmt.setInt(2, limite);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id                = rs.getInt("id");
                    int origenId          = rs.getInt("cuenta_origen_id");
                    // cuenta_destino_id puede ser NULL
                    Integer destinoId     = rs.getObject("cuenta_destino_id") != null
                                           ? rs.getInt("cuenta_destino_id") : null;
                    String tipoStr        = rs.getString("tipo");
                    double monto          = rs.getDouble("monto");
                    String fechaStr       = rs.getString("fecha");
                    String descripcion    = rs.getString("descripcion");

                    MovimientoRegistro.Tipo tipo = MovimientoRegistro.Tipo.valueOf(tipoStr);

                    // Parsear fecha (SQLite la guarda como texto)
                    LocalDateTime fecha = (fechaStr != null)
                            ? LocalDateTime.parse(fechaStr.replace(" ", "T"))
                            : LocalDateTime.now();

                    movimientos.add(new MovimientoRegistro(id, origenId, destinoId, tipo, monto, fecha, descripcion));
                }
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al listar los movimientos.");
            e.printStackTrace();
        }

        return movimientos;
    }
}

