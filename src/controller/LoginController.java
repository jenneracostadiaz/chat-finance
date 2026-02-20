package controller;

import dao.UsuarioDAO;
import modelo.Usuario;
import view.ConsoleView;

/**
 * Controlador de Login y gestión del menú principal.
 * Coordina la lógica entre la Vista (ConsoleView) y el Modelo (Usuario/UsuarioDAO).
 * FASE 3: Integración con Motor de Transacciones
 */
public class LoginController {
    private ConsoleView vista;
    private UsuarioDAO usuarioDAO;
    private CuentaController cuentaController;
    private OperacionesController operacionesController;
    private Usuario usuarioActual;

    /**
     * Constructor que inicializa el controlador con la vista y el DAO.
     */
    public LoginController() {
        this.vista                  = new ConsoleView();
        this.usuarioDAO             = new UsuarioDAO();
        this.cuentaController       = new CuentaController(vista);
        this.operacionesController  = new OperacionesController(vista);
        this.usuarioActual          = null;
    }

    /**
     * Inicia el proceso de autenticación y menú principal.
     * Este es el método principal que controla el flujo de la aplicación.
     */
    public void iniciar() {
        try {
            // FASE 1: Autenticación por número de WhatsApp
            if (autenticarUsuario()) {
                // FASE 2: Mostrar menú principal
                mostrarMenuPrincipal();
            }
        } finally {
            // Cerrar la vista al terminar
            vista.cerrar();
        }
    }

    /**
     * Gestiona el proceso de autenticación del usuario.
     * @return true si el usuario fue autenticado o registrado correctamente, false en caso contrario
     */
    private boolean autenticarUsuario() {
        // Solicitar número de WhatsApp
        String numeroWhatsApp = vista.solicitarNumeroWhatsApp();

        // Validar que no esté vacío
        if (numeroWhatsApp == null || numeroWhatsApp.isEmpty()) {
            vista.mostrarError("El número de WhatsApp no puede estar vacío.");
            return false;
        }

        // Buscar usuario en la base de datos
        usuarioActual = usuarioDAO.buscarPorWhatsapp(numeroWhatsApp);

        if (usuarioActual != null) {
            // CASO A: Usuario existente
            vista.mostrarBienvenidaUsuarioExistente(usuarioActual.getNombre());
            return true;
        } else {
            // CASO B: Usuario nuevo - Solicitar registro
            return registrarNuevoUsuario(numeroWhatsApp);
        }
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * @param numeroWhatsApp Número de WhatsApp del nuevo usuario
     * @return true si el registro fue exitoso, false en caso contrario
     */
    private boolean registrarNuevoUsuario(String numeroWhatsApp) {
        // Solicitar nombre
        String nombre = vista.solicitarNombre();

        // Validar que el nombre no esté vacío
        if (nombre == null || nombre.isEmpty()) {
            vista.mostrarError("El nombre no puede estar vacío.");
            return false;
        }

        // Crear nuevo usuario
        Usuario nuevoUsuario = new Usuario(numeroWhatsApp, nombre);

        // Guardar en la base de datos
        usuarioActual = usuarioDAO.crearUsuario(nuevoUsuario);

        if (usuarioActual != null) {
            vista.mostrarBienvenidaNuevoUsuario(usuarioActual.getNombre());
            return true;
        } else {
            vista.mostrarError("No se pudo completar el registro. Intente nuevamente.");
            return false;
        }
    }

    /**
     * Muestra el menú principal y gestiona las opciones seleccionadas.
     */
    private void mostrarMenuPrincipal() {
        boolean continuar = true;

        while (continuar) {
            vista.mostrarMenuPrincipal();
            int opcion = vista.leerEntero();

            switch (opcion) {
                case 1:
                    // Ver Mis Cuentas y Saldos (FASE 2)
                    cuentaController.verSaldos(usuarioActual);
                    break;

                case 2:
                    // Agregar Nueva Cuenta (FASE 2)
                    cuentaController.agregarCuenta(usuarioActual);
                    break;

                case 3:
                    // Operaciones Financieras (FASE 3)
                    operacionesController.mostrarMenuOperaciones(usuarioActual);
                    break;

                case 4:
                    // Reportes y Analítica (FASE 4)
                    operacionesController.verReporteAnalitico(usuarioActual);
                    vista.esperarEnter();
                    break;

                case 5:
                    // Salir
                    vista.mostrarDespedida();
                    continuar = false;
                    break;

                case 99:
                    // Opción oculta: cuentas de prueba
                    cuentaController.crearCuentasDePrueba(usuarioActual);
                    break;

                default:
                    vista.mostrarError("Opción inválida. Por favor, seleccione una opción válida.");
                    vista.esperarEnter();
                    break;
            }
        }
    }

    /**
     * Obtiene el usuario actualmente autenticado.
     * @return Usuario actual o null si no hay sesión activa
     */
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
}
