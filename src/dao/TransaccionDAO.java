package dao;

import modelo.MovimientoRegistro;
import util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO para movimientos financieros. Garantiza atomicidad mediante transacciones SQL explícitas.
 * Implementa {@link CrudRepository} con {@link MovimientoRegistro} como tipo de entidad.
 *
 * Patrón de atomicidad aplicado en cada operación de escritura:
 *   setAutoCommit(false) → operaciones → commit()  /  rollback() en caso de error.
 */
public class TransaccionDAO implements CrudRepository<MovimientoRegistro, Integer> {

    @Override
    public MovimientoRegistro guardar(MovimientoRegistro movimiento) {
        String sql = "INSERT INTO transacciones " +
                     "(cuenta_origen_id, cuenta_destino_id, tipo, monto, descripcion, categoria) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, movimiento.getCuentaOrigenId());
            if (movimiento.getCuentaDestinoId() != null) {
                pstmt.setInt(2, movimiento.getCuentaDestinoId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setString(3, movimiento.getTipo().name());
            pstmt.setDouble(4, movimiento.getMonto());
            pstmt.setString(5, movimiento.getDescripcion());
            pstmt.setString(6, movimiento.getCategoria());
            pstmt.executeUpdate();

            try (ResultSet llaves = pstmt.getGeneratedKeys()) {
                if (llaves.next()) movimiento.setId(llaves.getInt(1));
            }
            return movimiento;

        } catch (SQLException e) {
            System.err.println("Error al guardar movimiento: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public MovimientoRegistro buscarPorId(Integer id) {
        String sql = "SELECT id, cuenta_origen_id, cuenta_destino_id, tipo, monto, " +
                     "       fecha, descripcion, categoria FROM transacciones WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapearFila(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar movimiento id=" + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<MovimientoRegistro> listarTodos() {
        List<MovimientoRegistro> lista = new ArrayList<>();
        String sql = "SELECT id, cuenta_origen_id, cuenta_destino_id, tipo, monto, " +
                     "       fecha, descripcion, categoria FROM transacciones ORDER BY fecha DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) lista.add(mapearFila(rs));

        } catch (SQLException e) {
            System.err.println("Error al listar movimientos: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

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
                try (ResultSet llaves = pstmt.getGeneratedKeys()) {
                    if (llaves.next()) nuevoId = llaves.getInt(1);
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
            System.err.println("Error al registrar ingreso. ROLLBACK ejecutado: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

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
                try (ResultSet llaves = pstmt.getGeneratedKeys()) {
                    if (llaves.next()) nuevoId = llaves.getInt(1);
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
            System.err.println("Error al registrar gasto. ROLLBACK ejecutado: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public MovimientoRegistro realizarTransferencia(int origenId, int destinoId,
                                                    double monto, String descripcion) {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        String sqlInsert    = "INSERT INTO transacciones " +
                              "(cuenta_origen_id, cuenta_destino_id, tipo, monto, descripcion, categoria) " +
                              "VALUES (?, ?, 'TRANSFERENCIA', ?, ?, 'Transferencia')";
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
                try (ResultSet llaves = pstmt.getGeneratedKeys()) {
                    if (llaves.next()) nuevoId = llaves.getInt(1);
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
                monto, descripcion, "Transferencia"
            );
            mov.setId(nuevoId);
            return mov;

        } catch (SQLException e) {
            rollback(conn);
            System.err.println("Error en transferencia. ROLLBACK de las 3 operaciones: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

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
                while (rs.next()) movimientos.add(mapearFila(rs));
            }

        } catch (SQLException e) {
            System.err.println("Error al listar movimientos del usuario " + usuarioId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return movimientos;
    }

    public Map<String, Double> obtenerResumenGastos(int usuarioId) {
        Map<String, Double> resumen = new HashMap<>();

        String sql = "SELECT COALESCE(t.categoria, 'Sin categoria') AS categoria, " +
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
            System.err.println("Error al obtener resumen de gastos: " + e.getMessage());
            e.printStackTrace();
        }
        return resumen;
    }

    public Map<String, Double> obtenerResumenIngresos(int usuarioId) {
        Map<String, Double> resumen = new HashMap<>();

        String sql = "SELECT COALESCE(t.categoria, 'Sin categoria') AS categoria, " +
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
            System.err.println("Error al obtener resumen de ingresos: " + e.getMessage());
            e.printStackTrace();
        }
        return resumen;
    }

    private MovimientoRegistro mapearFila(ResultSet rs) throws SQLException {
        int      id          = rs.getInt("id");
        int      origenId    = rs.getInt("cuenta_origen_id");
        Integer  destinoId   = rs.getObject("cuenta_destino_id") != null ? rs.getInt("cuenta_destino_id") : null;
        String   tipoStr     = rs.getString("tipo");
        double   monto       = rs.getDouble("monto");
        String   fechaStr    = rs.getString("fecha");
        String   descripcion = rs.getString("descripcion");
        String   categoria   = rs.getString("categoria");

        MovimientoRegistro.Tipo tipo = MovimientoRegistro.Tipo.valueOf(tipoStr);

        LocalDateTime fecha = (fechaStr != null)
                ? LocalDateTime.parse(fechaStr.replace(" ", "T"))
                : LocalDateTime.now();

        return new MovimientoRegistro(id, origenId, destinoId, tipo, monto, fecha, descripcion, categoria);
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
            conn.setAutoCommit(true);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
