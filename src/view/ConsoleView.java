package view;

import java.util.Scanner;

/**
 * Clase de Vista para manejar la interacción con el usuario a través de la consola.
 * Centraliza todas las operaciones de entrada/salida en un solo lugar.
 */
public class ConsoleView {
    private Scanner scanner;

    /**
     * Constructor que inicializa el Scanner para leer de la consola.
     */
    public ConsoleView() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Lee una línea de texto desde la consola.
     * @return String con el texto ingresado por el usuario
     */
    public String leerLinea() {
        return scanner.nextLine().trim();
    }

    /**
     * Lee un número entero desde la consola con validación.
     * @return int con el número ingresado
     */
    public int leerEntero() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                mostrarError("Por favor, ingrese un número válido.");
            }
        }
    }

    /**
     * Solicita al usuario que ingrese su número de WhatsApp.
     * @return String con el número de WhatsApp ingresado
     */
    public String solicitarNumeroWhatsApp() {
        mostrarMensaje("\n" + "═".repeat(50));
        mostrarMensaje("📱 INGRESE SU NÚMERO DE WHATSAPP");
        mostrarMensaje("═".repeat(50));
        System.out.print("➤ Número: ");
        return leerLinea();
    }

    /**
     * Solicita al usuario que ingrese su nombre para registrarse.
     * @return String con el nombre ingresado
     */
    public String solicitarNombre() {
        mostrarMensaje("\n" + "─".repeat(50));
        mostrarMensaje("📝 REGISTRO DE NUEVO USUARIO");
        mostrarMensaje("─".repeat(50));
        mostrarMensaje("Número no reconocido. Por favor, ingrese su nombre para registrarse.");
        System.out.print("➤ Nombre: ");
        return leerLinea();
    }

    /**
     * Muestra un mensaje de bienvenida para un usuario existente.
     * @param nombre Nombre del usuario
     */
    public void mostrarBienvenidaUsuarioExistente(String nombre) {
        mostrarMensaje("\n" + "═".repeat(50));
        mostrarMensaje("✓ BIENVENIDO NUEVAMENTE, " + nombre.toUpperCase() + "! 👋");
        mostrarMensaje("═".repeat(50));
    }

    /**
     * Muestra un mensaje de bienvenida para un nuevo usuario.
     * @param nombre Nombre del usuario
     */
    public void mostrarBienvenidaNuevoUsuario(String nombre) {
        mostrarMensaje("\n" + "═".repeat(50));
        mostrarMensaje("✓ ¡REGISTRO EXITOSO! BIENVENIDO, " + nombre.toUpperCase() + "! 🎉");
        mostrarMensaje("═".repeat(50));
    }

    /**
     * Muestra el menú principal de la aplicación.
     */
    public void mostrarMenuPrincipal() {
        mostrarMensaje("\n" + "─".repeat(50));
        mostrarMensaje("💰 MENÚ PRINCIPAL - CHATFINANCE");
        mostrarMensaje("─".repeat(50));
        mostrarMensaje("1. Ver Mis Cuentas y Saldos");
        mostrarMensaje("2. Agregar Cuenta (Próximamente)");
        mostrarMensaje("3. Salir");
        mostrarMensaje("\n💡 Tip: Opción 99 para datos de prueba");
        mostrarMensaje("─".repeat(50));
        System.out.print("➤ Seleccione una opción: ");
    }

    /**
     * Muestra un mensaje genérico en la consola.
     * @param mensaje Texto a mostrar
     */
    public void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    /**
     * Muestra un mensaje de error en la consola.
     * @param error Texto del error a mostrar
     */
    public void mostrarError(String error) {
        System.err.println("✗ ERROR: " + error);
    }

    /**
     * Muestra un mensaje de funcionalidad en desarrollo.
     */
    public void mostrarProximamente() {
        mostrarMensaje("\n⏳ Esta funcionalidad estará disponible próximamente...");
    }

    /**
     * Muestra un mensaje de despedida.
     */
    public void mostrarDespedida() {
        mostrarMensaje("\n" + "═".repeat(50));
        mostrarMensaje("👋 ¡HASTA PRONTO! Gracias por usar ChatFinance.");
        mostrarMensaje("═".repeat(50) + "\n");
    }

    /**
     * Espera a que el usuario presione Enter para continuar.
     */
    public void esperarEnter() {
        System.out.print("\n➤ Presione Enter para continuar...");
        scanner.nextLine();
    }

    /**
     * Cierra el Scanner.
     */
    public void cerrar() {
        if (scanner != null) {
            scanner.close();
        }
    }
}
