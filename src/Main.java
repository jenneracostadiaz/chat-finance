import controller.LoginController;
import util.DatabaseConnection;

/**
 *
 * @author ChatFinance Team
 * @version 1.0.1
 */
public class Main {
    public static void main(String[] args) {
        // Banner de bienvenida
        mostrarBanner();

        try {
            // Inicializar conexión a la base de datos (Singleton)
            // Esto crea la base de datos y las tablas necesarias si no existen
            DatabaseConnection.getInstance();

            // Crear e iniciar el controlador de login
            LoginController loginController = new LoginController();
            loginController.iniciar();

        } catch (Exception e) {
            System.err.println("✗ Error crítico en la aplicación:");
            e.printStackTrace();
        } finally {
            // Cerrar la conexión a la base de datos al finalizar
            DatabaseConnection.getInstance().cerrarConexion();
        }
    }

    /**
     * Muestra el banner de bienvenida de la aplicación.
     */
    private static void mostrarBanner() {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("\n  💰 Sistema de Gestión Financiera Personal 💰");
        System.out.println("  📱 Autenticación: WhatsApp");
        System.out.println("  🗄️  Base de Datos: SQLite");
        System.out.println("  ⚙️  Fase 2: Gestión de Cuentas y Saldos");
        System.out.println("═".repeat(60) + "\n");
    }
}