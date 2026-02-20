package dao;

import modelo.BilleteraDigital;
import modelo.CuentaBancaria;
import modelo.CuentaFinanciera;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * DAO para cuentas financieras. Implementa Single Table Inheritance.
 * Implementa {@link CrudRepository} exponiendo las operaciones CRUD estándar.
 */
public class CuentaDAO implements CrudRepository<CuentaFinanciera, Integer> {

    private static final String SQL_SELECCIONAR =
            "SELECT id, usuario_id, numero_cuenta, saldo, tipo_cuenta, alias, proveedor, banco, cci FROM cuentas";

    // ─────────────────────────────────────────────────────────────────────────
    // CrudRepository
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Persiste una nueva cuenta detectando su tipo concreto mediante instanceof.
     * Retorna la misma instancia con el ID asignado por la BD.
     */
    @Override
    public CuentaFinanciera guardar(CuentaFinanciera cuenta) {
        String sql = "INSERT INTO cuentas (usuario_id, numero_cuenta, saldo, tipo_cuenta, alias, proveedor, banco, cci) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, cuenta.getUsuarioId());
            pstmt.setString(2, cuenta.getNumeroCuenta());
            pstmt.setDouble(3, cuenta.getSaldo());
            pstmt.setString(4, cuenta.getTipoCuenta());

            if (cuenta instanceof BilleteraDigital billetera) {
                pstmt.setString(5, billetera.getAlias());
                pstmt.setString(6, billetera.getProveedor());
                pstmt.setNull(7, Types.VARCHAR);
                pstmt.setNull(8, Types.VARCHAR);
            } else if (cuenta instanceof CuentaBancaria bancaria) {
                pstmt.setNull(5, Types.VARCHAR);
                pstmt.setNull(6, Types.VARCHAR);
                pstmt.setString(7, bancaria.getBanco());
                pstmt.setString(8, bancaria.getCci());
            }

            int filas = pstmt.executeUpdate();
            if (filas > 0) {
                try (ResultSet llaves = pstmt.getGeneratedKeys()) {
                    if (llaves.next()) cuenta.setId(llaves.getInt(1));
                }
                return cuenta;
            }

        } catch (SQLException e) {
            System.err.println("Error al guardar la cuenta: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /** Alias de {@link #guardar(CuentaFinanciera)} para compatibilidad con código existente. */
    public CuentaFinanciera crear(CuentaFinanciera cuenta) {
        return guardar(cuenta);
    }

    @Override
    public CuentaFinanciera buscarPorId(Integer id) {
        String sql = SQL_SELECCIONAR + " WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapearFila(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar la cuenta con id=" + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retorna todas las cuentas del sistema (sin filtro de usuario).
     * Usar {@link #listarPorUsuario(int)} para consultas filtradas.
     */
    @Override
    public List<CuentaFinanciera> listarTodos() {
        List<CuentaFinanciera> cuentas = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_SELECCIONAR);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                CuentaFinanciera cuenta = mapearFila(rs);
                if (cuenta != null) cuentas.add(cuenta);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar todas las cuentas: " + e.getMessage());
            e.printStackTrace();
        }
        return cuentas;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Consultas específicas de dominio
    // ─────────────────────────────────────────────────────────────────────────

    public List<CuentaFinanciera> listarPorUsuario(int usuarioId) {
        List<CuentaFinanciera> cuentas = new ArrayList<>();
        String sql = SQL_SELECCIONAR + " WHERE usuario_id = ? ORDER BY fecha_creacion DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, usuarioId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CuentaFinanciera cuenta = mapearFila(rs);
                    if (cuenta != null) cuentas.add(cuenta);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar cuentas del usuario " + usuarioId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return cuentas;
    }

    public Double calcularPatrimonioTotal(int usuarioId) {
        String sql = "SELECT SUM(saldo) AS total FROM cuentas WHERE usuario_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, usuarioId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error al calcular patrimonio total: " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0;
    }

    public boolean actualizarSaldo(int id, double nuevoSaldo) {
        String sql = "UPDATE cuentas SET saldo = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, nuevoSaldo);
            pstmt.setInt(2, id);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar saldo de cuenta id=" + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mapeo ResultSet -> Objeto (ORM manual, Single Table Inheritance)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Construye la subclase concreta correcta según el discriminador {@code tipo_cuenta}.
     *
     * @param rs ResultSet posicionado en la fila a mapear
     * @return Instancia de {@link BilleteraDigital} o {@link CuentaBancaria}, o null si tipo desconocido
     */
    private CuentaFinanciera mapearFila(ResultSet rs) throws SQLException {
        int    id          = rs.getInt("id");
        int    usuarioId   = rs.getInt("usuario_id");
        String numero      = rs.getString("numero_cuenta");
        double saldo       = rs.getDouble("saldo");
        String tipo        = rs.getString("tipo_cuenta");

        return switch (tipo) {
            case "BILLETERA" -> new BilleteraDigital(id, usuarioId, numero, saldo,
                                    rs.getString("alias"), rs.getString("proveedor"));
            case "BANCO"     -> new CuentaBancaria(id, usuarioId, numero, saldo,
                                    rs.getString("banco"), rs.getString("cci"));
            default          -> null;
        };
    }
}
