package dao;

import modelo.Usuario;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object (DAO) para la entidad Usuario.
 * Gestiona todas las operaciones de persistencia relacionadas con usuarios.
 */
public class UsuarioDAO {

    /**
     * Busca un usuario por su número de WhatsApp.
     * @param numeroWhatsApp Número de WhatsApp a buscar
     * @return Usuario si existe, null si no se encuentra
     */
    public Usuario buscarPorWhatsapp(String numeroWhatsApp) {
        String sql = "SELECT id, numero_whatsapp, nombre FROM usuarios WHERE numero_whatsapp = ?";
        Usuario usuario = null;

        // Usar try-with-resources para cerrar automáticamente los recursos
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Establecer el parámetro de forma segura (previene SQL Injection)
            pstmt.setString(1, numeroWhatsApp);

            // Ejecutar la consulta
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Construir el objeto Usuario desde el ResultSet
                    usuario = new Usuario(
                        rs.getInt("id"),
                        rs.getString("numero_whatsapp"),
                        rs.getString("nombre")
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al buscar usuario por WhatsApp: " + numeroWhatsApp);
            e.printStackTrace();
        }

        return usuario;
    }

    /**
     * Crea un nuevo usuario en la base de datos.
     * @param usuario Objeto Usuario con los datos a guardar (numeroWhatsApp y nombre)
     * @return Usuario con el ID generado, o null si hubo error
     */
    public Usuario crearUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuarios (numero_whatsapp, nombre) VALUES (?, ?)";

        // Usar try-with-resources para gestión automática de recursos
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql,
                     PreparedStatement.RETURN_GENERATED_KEYS)) {

            // Establecer los parámetros de forma segura
            pstmt.setString(1, usuario.getNumeroWhatsApp());
            pstmt.setString(2, usuario.getNombre());

            // Ejecutar la inserción
            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                // Obtener el ID generado automáticamente
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        usuario.setId(rs.getInt(1));
                        System.out.println("✓ Usuario registrado exitosamente con ID: " + usuario.getId());
                        return usuario;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al crear usuario: " + usuario.getNombre());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Verifica si un número de WhatsApp ya está registrado.
     * @param numeroWhatsApp Número a verificar
     * @return true si existe, false si no
     */
    public boolean existeNumeroWhatsapp(String numeroWhatsApp) {
        return buscarPorWhatsapp(numeroWhatsApp) != null;
    }
}
