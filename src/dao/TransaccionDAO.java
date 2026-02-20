package dao;

import modelo.MovimientoRegistro;
import util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO para gestionar los movimientos financieros (ingresos, gastos, transferencias).
 *
 * PRINCIPIO CLAVE: Atomicidad con transacciones SQL.
 *   connection.setAutoCommit(false) → operaciones → connection.commit()
 *   Si algo falla → connection.rollback()
 *
 * FASE 4: columna `categoria` añadida a todos los INSERT + métodos de reporte analítico.
 */
public class TransaccionDAO {

    // ─────────────────────────────────────────────────────────────────────────
    // INGRESO
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registra un ingreso en una cuenta (ACID).
     * 1. INSERT en transacciones (con categoría)
     * 2. UPDATE saldo += monto
     */
    public MovimientoRegistro registrarIngreso(int cuentaId, double monto,
                                               String descripcion, String categoria) {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        String sqlInsert = "INSERT INTO transacciones " +
                           "(cuenta_origen_id, cuenta_destino_id, tipo, monto, descripcion, categoria) " +
                           "VALUES (?, NULL, 'INGRESO', ?, ?, ?)";
        String sqlUpdate = "UPDATE cuentas SET saldo = saldo + ? WHERE id = ?";

        try {
            conn.setAutoCommit(false);

            int nuevoId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, cuentaId);
                pstmt.setDouble(2, monto);
                pstmt.setString(3, descripcion);
                pstmt.setString(4, categoria);
                pstmt.executeUpdate();
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) nuevoId = keys.getInt(1);
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                pstmt.setDouble(1, monto);
                pstmt.setInt(2, cuentaId);
                pstmt.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

            MovimientoRegistro mov = new MovimientoRegistro(
                cuentaId, null, MovimientoRegistro.Tipo.INGRESO, monto, descripcion, categoria
            );
            mov.setId(nuevoId);
            return mov;

        } catch (SQLException e) {
            rollback(conn);
            System.err.println("✗ Error al registrar ingreso. ROLLBACK ejecutado.");
            e.printStackTrace();
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GASTO
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registra un gasto en una cuenta (ACID).
     * 1. INSERT en transacciones (con categoría)
     * 2. UPDATE saldo -= monto
     */
    public MovimientoRegistro registrarGasto(int cuentaId, double monto,
                                             String descripcion, String categoria) {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        String sqlInsert = "INSERT INTO transacciones " +
                           "(cuenta_origen_id, cuenta_destino_id, tipo, monto, descripcion, categoria) " +
                           "VALUES (?, NULL, 'GASTO', ?, ?, ?)";
        String sqlUpdate = "UPDATE cuentas SET saldo = saldo - ? WHERE id = ?";

        try {
            conn.setAutoCommit(false);

            int nuevoId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, cuentaId);
                pstmt.setDouble(2, monto);
                pstmt.setString(3, descripcion);
                pstmt.setString(4, categoria);
                pstmt.executeUpdate();
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) nuevoId = keys.getInt(1);
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                pstmt.setDouble(1, monto);
                pstmt.setInt(2, cuentaId);
                pstmt.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

            MovimientoRegistro mov = new MovimientoRegistro(
                cuentaId, null, MovimientoRegistro.Tipo.GASTO, monto, descripcion, categoria
            );
            mov.setId(nuevoId);
            return mov;

        } catch (SQLException e) {
            rollback(conn);
            System.err.println("✗ Error al registrar gasto. ROLLBACK ejecutado.");
            e.printStackTrace();
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TRANSFERENCIA
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Realiza una transferencia entre dos cuentas (ACID — 3 operaciones).
     * Las transferencias usan categoría fija "Transferencia 🔄".
     */
    public MovimientoRegistro realizarTransferencia(int origenId, int destinoId,
                                                    double monto, String descripcion) {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        String sqlInsert    = "INSERT INTO transacciones " +
                              "(cuenta_origen_id, cuenta_destino_id, tipo, monto, descripcion, categoria) " +
                              "VALUES (?, ?, 'TRANSFERENCIA', ?, ?, 'Transferencia 🔄')";
        String sqlDescontar = "UPDATE cuentas SET saldo = saldo - ? WHERE id = ?";
        String sqlAcreditar = "UPDATE cuentas SET saldo = saldo + ? WHERE id = ?";

        try {
            conn.setAutoCommit(false);

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

            try (PreparedStatement pstmt = conn.prepareStatement(sqlDescontar)) {
                pstmt.setDouble(1, monto);
                pstmt.setInt(2, origenId);
                pstmt.executeUpdate();
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sqlAcreditar)) {
                pstmt.setDouble(1, monto);
                pstmt.setInt(2, destinoId);
                pstmt.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

            MovimientoRegistro mov = new MovimientoRegistro(
                origenId, destinoId, MovimientoRegistro.Tipo.TRANSFERENCIA,
                monto, descripcion, "Transferencia 🔄"
            );
            mov.setId(nuevoId);
            return mov;

        } catch (SQLException e) {
            rollback(conn);
            System.err.println("✗ Error en transferencia. ROLLBACK de las 3 operaciones.");
            e.printStackTrace();
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LISTAR últimos movimientos
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Devuelve los últimos N movimientos de todas las cuentas del usuario.
     */
    public List<MovimientoRegistro> listarUltimosMovimientos(int usuarioId, int limite) {
        List<MovimientoRegistro> movimientos = new ArrayList<>();

        String sql = "SELECT t.id, t.cuenta_origen_id, t.cuenta_destino_id, t.tipo, " +
                     "       t.monto, t.fecha, t.descripcion, t.categoria " +
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
                    int id             = rs.getInt("id");
                    int origenId       = rs.getInt("cuenta_origen_id");
                    Integer destinoId  = rs.getObject("cuenta_destino_id") != null
                                        ? rs.getInt("cuenta_destino_id") : null;
                    String tipoStr     = rs.getString("tipo");
                    double monto       = rs.getDouble("monto");
                    String fechaStr    = rs.getString("fecha");
                    String descripcion = rs.getString("descripcion");
                    String categoria   = rs.getString("categoria");

                    MovimientoRegistro.Tipo tipo = MovimientoRegistro.Tipo.valueOf(tipoStr);

                    LocalDateTime fecha = (fechaStr != null)
                            ? LocalDateTime.parse(fechaStr.replace(" ", "T"))
                            : LocalDateTime.now();

                    movimientos.add(new MovimientoRegistro(
                        id, origenId, destinoId, tipo, monto, fecha, descripcion, categoria
                    ));
                }
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al listar movimientos.");
            e.printStackTrace();
        }

        return movimientos;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FASE 4: REPORTES ANALÍTICOS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Agrupa y suma los GASTOS del usuario por categoría.
     * Usa GROUP BY + SUM() para analítica.
     *
     * @param usuarioId ID del usuario
     * @return Map ordenado de categoria → total gastado (mayor a menor)
     */
    public Map<String, Double> obtenerResumenGastos(int usuarioId) {
        // LinkedHashMap preserva el orden de inserción (ORDER BY en SQL ya lo ordena)
        Map<String, Double> resumen = new LinkedHashMap<>();

        String sql = "SELECT COALESCE(t.categoria, 'Sin categoría') AS categoria, " +
                     "       SUM(t.monto) AS total " +
                     "FROM transacciones t " +
                     "INNER JOIN cuentas c ON t.cuenta_origen_id = c.id " +
                     "WHERE c.usuario_id = ? AND t.tipo = 'GASTO' " +
                     "GROUP BY t.categoria " +
                     "ORDER BY total DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, usuarioId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resumen.put(rs.getString("categoria"), rs.getDouble("total"));
                }
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al obtener resumen de gastos.");
            e.printStackTrace();
        }

        return resumen;
    }

    /**
     * Agrupa y suma los INGRESOS del usuario por categoría.
     *
     * @param usuarioId ID del usuario
     * @return Map ordenado de categoria → total ingresado (mayor a menor)
     */
    public Map<String, Double> obtenerResumenIngresos(int usuarioId) {
        Map<String, Double> resumen = new LinkedHashMap<>();

        String sql = "SELECT COALESCE(t.categoria, 'Sin categoría') AS categoria, " +
                     "       SUM(t.monto) AS total " +
                     "FROM transacciones t " +
                     "INNER JOIN cuentas c ON t.cuenta_origen_id = c.id " +
                     "WHERE c.usuario_id = ? AND t.tipo = 'INGRESO' " +
                     "GROUP BY t.categoria " +
                     "ORDER BY total DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, usuarioId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resumen.put(rs.getString("categoria"), rs.getDouble("total"));
                }
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al obtener resumen de ingresos.");
            e.printStackTrace();
        }

        return resumen;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilidad privada
    // ─────────────────────────────────────────────────────────────────────────

    /** Ejecuta rollback y restaura autoCommit sin propagar excepciones. */
    private void rollback(Connection conn) {
        try {
            conn.rollback();
            conn.setAutoCommit(true);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
