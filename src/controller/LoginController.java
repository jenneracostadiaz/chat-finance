package controller;

import dao.UsuarioDAO;
import modelo.Usuario;
import view.ConsoleView;

/** Controlador de autenticacion y menu principal. */
public class LoginController {

    private final ConsoleView vista;
    private final UsuarioDAO usuarioDAO;
    private final CuentaController cuentaController;
    private final OperacionesController operacionesController;
    private Usuario usuarioActual;

    public LoginController() {
        this.vista                 = new ConsoleView();
        this.usuarioDAO            = new UsuarioDAO();
        this.cuentaController      = new CuentaController(vista);
        this.operacionesController = new OperacionesController(vista);
    }

    public void iniciar() {
        try {
            if (autenticarUsuario()) mostrarMenuPrincipal();
        } finally {
            vista.cerrar();
        }
    }

    private boolean autenticarUsuario() {
        String numero = vista.solicitarNumeroWhatsApp();
        if (numero == null || numero.isEmpty()) {
            vista.mostrarError("El numero de WhatsApp no puede estar vacio.");
            return false;
        }

        usuarioActual = usuarioDAO.buscarPorWhatsapp(numero);

        if (usuarioActual != null) {
            vista.mostrarBienvenidaUsuarioExistente(usuarioActual.getNombre());
            return true;
        }
        return registrarNuevoUsuario(numero);
    }

    private boolean registrarNuevoUsuario(String numero) {
        String nombre = vista.solicitarNombre();
        if (nombre == null || nombre.isEmpty()) {
            vista.mostrarError("El nombre no puede estar vacio.");
            return false;
        }

        usuarioActual = usuarioDAO.crearUsuario(new Usuario(numero, nombre));

        if (usuarioActual != null) {
            vista.mostrarBienvenidaNuevoUsuario(usuarioActual.getNombre());
            return true;
        }
        vista.mostrarError("No se pudo completar el registro. Intente nuevamente.");
        return false;
    }

    private void mostrarMenuPrincipal() {
        boolean continuar = true;
        while (continuar) {
            vista.mostrarMenuPrincipal();
            int opcion = vista.leerEntero();

            switch (opcion) {
                case 1  -> cuentaController.verSaldos(usuarioActual);
                case 2  -> cuentaController.agregarCuenta(usuarioActual);
                case 3  -> operacionesController.mostrarMenuOperaciones(usuarioActual);
                case 4  -> { operacionesController.verReporteAnalitico(usuarioActual); vista.esperarEnter(); }
                case 5  -> { vista.mostrarDespedida(); continuar = false; }
                case 99 -> cuentaController.crearCuentasDePrueba(usuarioActual);
                default -> { vista.mostrarError("Opcion invalida."); vista.esperarEnter(); }
            }
        }
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }
}
