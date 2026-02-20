import controller.LoginController;
import util.DatabaseConnection;

/**
 * Punto de entrada de la aplicacion ChatFinance.
 */
public class Main {

    public static void main(String[] args) {
        mostrarBanner();
        try {
            DatabaseConnection.getInstance();
            new LoginController().iniciar();
        } catch (Exception e) {
            System.err.println("Error critico en la aplicacion:");
            e.printStackTrace();
        } finally {
            DatabaseConnection.getInstance().cerrarConexion();
        }
    }

    private static void mostrarBanner() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("\n  ChatFinance - Sistema de Gestion Financiera Personal");
        System.out.println("=".repeat(60) + "\n");
    }
}