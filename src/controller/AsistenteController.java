package controller;

import dao.CuentaDAO;
import dao.TransaccionDAO;
import modelo.BilleteraDigital;
import modelo.CuentaBancaria;
import modelo.CuentaFinanciera;
import modelo.MovimientoRegistro;
import modelo.Usuario;
import service.AsistenteIAService;
import service.IntencionOperacionDTO;
import view.ConsoleView;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador del Asistente IA — Router de Intenciones Universal.
 *
 * Recibe texto libre del usuario, delega la clasificacion a {@link AsistenteIAService}
 * y enruta la accion al metodo correcto segun la intencion detectada:
 *   REGISTRAR_TRANSACCION → registrarTransaccion()
 *   CREAR_CUENTA          → crearCuenta()
 *   VER_REPORTE           → verReporte()
 *   VER_SALDOS            → verSaldos()
 */
public class AsistenteController {

    private final ConsoleView            vista;
    private final AsistenteIAService     servicioIA;
    private final CuentaDAO              cuentaDAO;
    private final TransaccionDAO         transaccionDAO;
    private final OperacionesController  operacionesController;
    private final CuentaController       cuentaController;

    public AsistenteController(ConsoleView vista) {
        this.vista                 = vista;
        this.servicioIA            = new AsistenteIAService();
        this.cuentaDAO             = new CuentaDAO();
        this.transaccionDAO        = new TransaccionDAO();
        this.operacionesController = new OperacionesController(vista);
        this.cuentaController      = new CuentaController(vista);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Punto de entrada — bucle de chat
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Inicia el bucle del asistente. El usuario puede escribir varias peticiones
     * en la misma sesion hasta escribir "salir".
     */
    public void iniciarAsistente(Usuario usuario) {
        vista.mostrarCabecera("ASISTENTE INTELIGENTE - ROUTER DE INTENCIONES");

        vista.mostrarMensaje("Verificando conexion con Ollama...");
        if (!servicioIA.verificarConexion()) {
            vista.mostrarError("Ollama no esta disponible en http://localhost:11434");
            vista.mostrarMensaje("  Asegurese de que Ollama este corriendo.");
            vista.mostrarMensaje("  Comando: ollama run llama3.2");
            vista.esperarEnter();
            return;
        }
        vista.mostrarMensaje("Conexion con Ollama establecida.");
        mostrarAyuda();

        while (true) {
            List<CuentaFinanciera> cuentas = cuentaDAO.listarPorUsuario(usuario.getId());
            String listaCuentas = construirListaCuentas(cuentas);

            vista.mostrarMensaje("\nCuentas: " + (listaCuentas.isBlank() ? "(ninguna aun)" : listaCuentas));
            System.out.print("Tu: ");
            String texto = vista.leerLinea();

            if (texto.equalsIgnoreCase("salir") || texto.isBlank()) {
                vista.mostrarOperacionCancelada();
                break;
            }

            vista.mostrarMensaje("Analizando... (puede tardar unos segundos)");
            IntencionOperacionDTO dto = servicioIA.interpretarTexto(texto, listaCuentas);

            if (dto == null || !dto.tieneIntencionValida()) {
                vista.mostrarError("No se pudo interpretar la solicitud. Intente de nuevo con mas detalle.");
                continue;
            }

            mostrarResumenInterpretado(dto);
            enrutarIntencion(dto, cuentas, usuario);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Router principal
    // ─────────────────────────────────────────────────────────────────────────

    private void enrutarIntencion(IntencionOperacionDTO dto,
                                   List<CuentaFinanciera> cuentas,
                                   Usuario usuario) {
        switch (dto.getIntencion()) {
            case "REGISTRAR_TRANSACCION" -> registrarTransaccion(dto, cuentas);
            case "CREAR_CUENTA"          -> crearCuenta(dto, usuario);
            case "VER_REPORTE"           -> verReporte(usuario);
            case "VER_SALDOS"            -> verSaldos(usuario);
            default -> vista.mostrarError("Intencion no reconocida: " + dto.getIntencion());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Caso: REGISTRAR_TRANSACCION
    // ─────────────────────────────────────────────────────────────────────────

    private void registrarTransaccion(IntencionOperacionDTO dto, List<CuentaFinanciera> cuentas) {
        if (!dto.esTransaccionValida()) {
            vista.mostrarError("Faltan datos para registrar la transaccion (tipo, monto o cuenta).");
            return;
        }

        CuentaFinanciera cuenta = resolverCuenta(dto.getNombreCuenta(), cuentas);
        if (cuenta == null) {
            vista.mostrarError("No se encontro la cuenta: \"" + dto.getNombreCuenta() + "\"");
            return;
        }

        String tipo       = dto.getTipoTransaccion();
        double monto      = dto.getMonto();
        String categoria  = dto.getCategoria() != null ? dto.getCategoria() : "Otros";
        String descripcion = dto.getDescripcion() != null ? dto.getDescripcion() : "Registro via Asistente IA";

        if ("GASTO".equals(tipo) && cuenta.getSaldo() < monto) {
            vista.mostrarError(String.format(
                "Saldo insuficiente en %s. Disponible: S/ %.2f | Solicitado: S/ %.2f",
                cuenta.obtenerDetalleImprimible(), cuenta.getSaldo(), monto));
            return;
        }

        System.out.print("Confirmar " + tipo + " de S/ " + String.format("%.2f", monto) + "? (s/n): ");
        if (!vista.leerLinea().equalsIgnoreCase("s")) {
            vista.mostrarOperacionCancelada();
            return;
        }

        MovimientoRegistro resultado = "INGRESO".equals(tipo)
                ? transaccionDAO.registrarIngreso(cuenta.getId(), monto, descripcion, categoria)
                : transaccionDAO.registrarGasto(cuenta.getId(), monto, descripcion, categoria);

        if (resultado != null) {
            CuentaFinanciera actualizada = cuentaDAO.buscarPorId(cuenta.getId());
            double nuevoSaldo = actualizada != null ? actualizada.getSaldo()
                    : cuenta.getSaldo() + ("INGRESO".equals(tipo) ? monto : -monto);
            vista.mostrarExitoOperacion(
                tipo + " REGISTRADO",
                String.format("%s S/ %.2f en %s  [%s]",
                    "INGRESO".equals(tipo) ? "+" : "-", monto,
                    cuenta.obtenerDetalleImprimible(), categoria),
                "Nuevo saldo: S/ " + String.format("%.2f", nuevoSaldo)
            );
        } else {
            vista.mostrarError("No se pudo registrar. Cambios revertidos.");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Caso: CREAR_CUENTA
    // ─────────────────────────────────────────────────────────────────────────

    private void crearCuenta(IntencionOperacionDTO dto, Usuario usuario) {
        if (!dto.esCrearCuentaValida()) {
            vista.mostrarError("Faltan datos para crear la cuenta (nombre o tipo).");
            vista.mostrarMensaje("  Ejemplo: 'Crea una cuenta Yape de tipo billetera con 100 soles'");
            return;
        }

        String nombre = dto.getNombreCuenta();
        String tipo   = dto.getTipoCuentaNueva();
        double saldo  = dto.getMonto() != null ? dto.getMonto() : 0.0;

        System.out.printf("Crear cuenta %s '%s' con S/ %.2f de saldo inicial? (s/n): ",
                tipo, nombre, saldo);
        if (!vista.leerLinea().equalsIgnoreCase("s")) {
            vista.mostrarOperacionCancelada();
            return;
        }

        CuentaFinanciera nuevaCuenta = switch (tipo) {
            case "BILLETERA" -> new BilleteraDigital(usuario.getId(), nombre, saldo, nombre, nombre);
            case "BANCO"     -> new CuentaBancaria(usuario.getId(), nombre, saldo, nombre, null);
            default -> {
                vista.mostrarError("Tipo de cuenta no reconocido: " + tipo + ". Use BANCO o BILLETERA.");
                yield null;
            }
        };

        if (nuevaCuenta == null) return;

        CuentaFinanciera guardada = cuentaDAO.guardar(nuevaCuenta);
        if (guardada != null) {
            vista.mostrarExitoOperacion(
                "CUENTA CREADA",
                guardada.obtenerDetalleImprimible(),
                "Saldo inicial: S/ " + String.format("%.2f", saldo)
            );
        } else {
            vista.mostrarError("No se pudo crear la cuenta.");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Caso: VER_REPORTE
    // ─────────────────────────────────────────────────────────────────────────

    private void verReporte(Usuario usuario) {
        operacionesController.verReporteAnalitico(usuario);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Caso: VER_SALDOS
    // ─────────────────────────────────────────────────────────────────────────

    private void verSaldos(Usuario usuario) {
        cuentaController.verSaldos(usuario);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void mostrarResumenInterpretado(IntencionOperacionDTO dto) {
        vista.mostrarMensaje("\n--- Interpretacion ---");
        vista.mostrarMensaje("  Intencion    : " + dto.getIntencion());
        if (dto.getTipoTransaccion() != null)
            vista.mostrarMensaje("  Tipo         : " + dto.getTipoTransaccion());
        if (dto.getMonto() != null)
            vista.mostrarMensaje("  Monto        : S/ " + String.format("%.2f", dto.getMonto()));
        if (dto.getCategoria() != null)
            vista.mostrarMensaje("  Categoria    : " + dto.getCategoria());
        if (dto.getNombreCuenta() != null)
            vista.mostrarMensaje("  Cuenta       : " + dto.getNombreCuenta());
        if (dto.getTipoCuentaNueva() != null)
            vista.mostrarMensaje("  Tipo cuenta  : " + dto.getTipoCuentaNueva());
        if (dto.getDescripcion() != null)
            vista.mostrarMensaje("  Descripcion  : " + dto.getDescripcion());
        vista.mostrarMensaje("-".repeat(50));
    }

    private void mostrarAyuda() {
        vista.mostrarMensaje("\nPuedes escribir en lenguaje natural. Ejemplos:");
        vista.mostrarMensaje("  - \"Gaste 50 soles en el cine con mi Yape\"");
        vista.mostrarMensaje("  - \"Recibi mi sueldo de 3000 soles en BCP\"");
        vista.mostrarMensaje("  - \"Crea una billetera Plin con 200 soles\"");
        vista.mostrarMensaje("  - \"Muestrame mis gastos del mes\"");
        vista.mostrarMensaje("  - \"Cuanto tengo en total?\"");
        vista.mostrarMensaje("  (Escribe 'salir' para volver al menu principal)");
    }

    /**
     * Resuelve la cuenta buscando primero coincidencia exacta, luego parcial.
     */
    private CuentaFinanciera resolverCuenta(String nombreIA, List<CuentaFinanciera> cuentas) {
        if (nombreIA == null || nombreIA.isBlank()) return null;
        String norm = nombreIA.toLowerCase().trim();

        for (CuentaFinanciera c : cuentas)
            if (c.obtenerDetalleImprimible().toLowerCase().contains(norm)) return c;

        for (CuentaFinanciera c : cuentas) {
            String detalle = c.obtenerDetalleImprimible().toLowerCase();
            for (String palabra : norm.split("\\s+"))
                if (detalle.contains(palabra)) return c;
        }
        return null;
    }

    private String construirListaCuentas(List<CuentaFinanciera> cuentas) {
        if (cuentas.isEmpty()) return "";
        return cuentas.stream()
                .map(CuentaFinanciera::obtenerDetalleImprimible)
                .collect(Collectors.joining(", "));
    }
}
