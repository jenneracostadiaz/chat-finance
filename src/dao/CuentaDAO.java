package dao;

import modelo.BilleteraDigital;
import modelo.CuentaBancaria;
import modelo.CuentaFinanciera;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para gestionar las cuentas financieras.
 * Implementa el patrón Single Table Inheritance con mapeo manual (ORM manual).
 * FASE 2: Gestión de Cuentas y Saldos
 */
public class CuentaDAO {

    /**
     * Crea una nueva cuenta en la base de datos.
     * Detecta automáticamente el tipo de cuenta (Billetera o Banco) y guarda con el discriminador correcto.
     *
     * @param cuenta La cuenta a crear (BilleteraDigital o CuentaBancaria)
     * @return La cuenta creada con el ID asignado, o null si hubo un error
     */
    public CuentaFinanciera crear(CuentaFinanciera cuenta) {
        String sql = """
            INSERT INTO cuentas (usuario_id, numero_cuenta, saldo, tipo_cuenta, alias, proveedor, banco, cci)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Parámetros comunes
            pstmt.setInt(1, cuenta.getUsuarioId());
            pstmt.setString(2, cuenta.getNumeroCuenta());
            pstmt.setDouble(3, cuenta.getSaldo());
            pstmt.setString(4, cuenta.getTipoCuenta());

            // Parámetros específicos según el tipo (Polimorfismo)
            if (cuenta instanceof BilleteraDigital) {
                BilleteraDigital billetera = (BilleteraDigital) cuenta;
                pstmt.setString(5, billetera.getAlias());
                pstmt.setString(6, billetera.getProveedor());
                pstmt.setNull(7, Types.VARCHAR);  // banco
                pstmt.setNull(8, Types.VARCHAR);  // cci
            } else if (cuenta instanceof CuentaBancaria) {
                CuentaBancaria cuentaBanco = (CuentaBancaria) cuenta;
                pstmt.setNull(5, Types.VARCHAR);  // alias
                pstmt.setNull(6, Types.VARCHAR);  // proveedor
                pstmt.setString(7, cuentaBanco.getBanco());
                pstmt.setString(8, cuentaBanco.getCci());
            }

            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                // La inserción fue exitosa
                System.out.println("✓ Cuenta insertada en la base de datos correctamente.");

                // Intentar obtener el ID generado
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        cuenta.setId(generatedKeys.getInt(1));
                        System.out.println("✓ ID generado: " + cuenta.getId());
                    } else {
                        System.out.println("⚠️  Advertencia: No se pudo obtener el ID generado, pero la cuenta fue creada.");
                    }
                } catch (SQLException e) {
                    System.out.println("⚠️  Advertencia: Error al obtener ID generado: " + e.getMessage());
                }

                // Retornar la cuenta aunque no tengamos el ID
                return cuenta;
            } else {
                System.out.println("⚠️  No se insertaron filas en la base de datos.");
            }
        } catch (SQLException e) {
            System.err.println("✗ Error al crear la cuenta en la base de datos.");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Lista todas las cuentas de un usuario específico.
     * Implementa el mapeo polimórfico: según el discriminador 'tipo_cuenta',
     * instancia BilleteraDigital o CuentaBancaria.
     *
     * @param usuarioId ID del usuario
     * @return Lista de cuentas financieras del usuario
     */
    public List<CuentaFinanciera> listarPorUsuario(int usuarioId) {
        List<CuentaFinanciera> cuentas = new ArrayList<>();
        String sql = """
            SELECT id, usuario_id, numero_cuenta, saldo, tipo_cuenta, 
                   alias, proveedor, banco, cci
            FROM cuentas
            WHERE usuario_id = ?
            ORDER BY fecha_creacion DESC
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, usuarioId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Leer campos comunes
                    Integer id = rs.getInt("id");
                    Integer userId = rs.getInt("usuario_id");
                    String numeroCuenta = rs.getString("numero_cuenta");
                    Double saldo = rs.getDouble("saldo");
                    String tipoCuenta = rs.getString("tipo_cuenta");

                    // Mapeo polimórfico según el discriminador
                    CuentaFinanciera cuenta = null;

                    if ("BILLETERA".equals(tipoCuenta)) {
                        String alias = rs.getString("alias");
                        String proveedor = rs.getString("proveedor");
                        cuenta = new BilleteraDigital(id, userId, numeroCuenta, saldo, alias, proveedor);
                    } else if ("BANCO".equals(tipoCuenta)) {
                        String banco = rs.getString("banco");
                        String cci = rs.getString("cci");
                        cuenta = new CuentaBancaria(id, userId, numeroCuenta, saldo, banco, cci);
                    }

                    if (cuenta != null) {
                        cuentas.add(cuenta);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("✗ Error al listar las cuentas del usuario.");
            e.printStackTrace();
        }

        return cuentas;
    }

    /**
     * Calcula el patrimonio total de un usuario (suma de saldos de todas sus cuentas).
     *
     * @param usuarioId ID del usuario
     * @return Suma total de los saldos
     */
    public Double calcularPatrimonioTotal(int usuarioId) {
        String sql = "SELECT SUM(saldo) as total FROM cuentas WHERE usuario_id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, usuarioId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            System.err.println("✗ Error al calcular el patrimonio total.");
            e.printStackTrace();
        }

        return 0.0;
    }

    /**
     * Busca una cuenta por su ID.
     *
     * @param id ID de la cuenta
     * @return La cuenta encontrada o null si no existe
     */
    public CuentaFinanciera buscarPorId(int id) {
        String sql = """
            SELECT id, usuario_id, numero_cuenta, saldo, tipo_cuenta, 
                   alias, proveedor, banco, cci
            FROM cuentas
            WHERE id = ?
        """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Integer cuentaId = rs.getInt("id");
                    Integer usuarioId = rs.getInt("usuario_id");
                    String numeroCuenta = rs.getString("numero_cuenta");
                    Double saldo = rs.getDouble("saldo");
                    String tipoCuenta = rs.getString("tipo_cuenta");

                    if ("BILLETERA".equals(tipoCuenta)) {
                        String alias = rs.getString("alias");
                        String proveedor = rs.getString("proveedor");
                        return new BilleteraDigital(cuentaId, usuarioId, numeroCuenta, saldo, alias, proveedor);
                    } else if ("BANCO".equals(tipoCuenta)) {
                        String banco = rs.getString("banco");
                        String cci = rs.getString("cci");
                        return new CuentaBancaria(cuentaId, usuarioId, numeroCuenta, saldo, banco, cci);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("✗ Error al buscar la cuenta por ID.");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Actualiza el saldo de una cuenta.
     *
     * @param id ID de la cuenta
     * @param nuevoSaldo Nuevo saldo a establecer
     * @return true si la actualización fue exitosa
     */
    public boolean actualizarSaldo(int id, double nuevoSaldo) {
        String sql = "UPDATE cuentas SET saldo = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, nuevoSaldo);
            pstmt.setInt(2, id);

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            System.err.println("✗ Error al actualizar el saldo de la cuenta.");
            e.printStackTrace();
        }

        return false;
    }
}
