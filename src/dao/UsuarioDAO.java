package dao;

import modelo.Usuario;
import util.DatabaseConnection;

import java.sql.*;

/** DAO para la entidad {@link Usuario}. */
public class UsuarioDAO {

    public Usuario buscarPorWhatsapp(String numeroWhatsApp) {
        String sql = "SELECT id, numero_whatsapp, nombre FROM usuarios WHERE numero_whatsapp = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, numeroWhatsApp);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(rs.getInt("id"), rs.getString("numero_whatsapp"), rs.getString("nombre"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por WhatsApp: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Usuario crearUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuarios (numero_whatsapp, nombre) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, usuario.getNumeroWhatsApp());
            pstmt.setString(2, usuario.getNombre());

            if (pstmt.executeUpdate() > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        usuario.setId(rs.getInt(1));
                        System.out.println("Usuario registrado con ID: " + usuario.getId());
                        return usuario;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al crear usuario: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean existeNumeroWhatsapp(String numeroWhatsApp) {
        return buscarPorWhatsapp(numeroWhatsApp) != null;
    }
}
