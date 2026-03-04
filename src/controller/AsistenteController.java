package controller;

import dao.CuentaDAO;
import dao.TransaccionDAO;
import modelo.CuentaFinanciera;
import modelo.MovimientoRegistro;
import modelo.Usuario;
import service.AsistenteIAService;
import service.IntencionOperacionDTO;
import view.ConsoleView;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador del Asistente Inteligente (Fase 5).
 * Recibe texto libre del usuario, delega la interpretacion a {@link AsistenteIAService}
 * y persiste la operacion usando los DAOs existentes.
 */
public class AsistenteController {

    private final ConsoleView        vista;
    private final AsistenteIAService servicioIA;
    private final CuentaDAO          cuentaDAO;
    private final TransaccionDAO     transaccionDAO;

    public AsistenteController(ConsoleView vista) {
        this.vista           = vista;
        this.servicioIA      = new AsistenteIAService();
        this.cuentaDAO       = new CuentaDAO();
        this.transaccionDAO  = new TransaccionDAO();
    }

    /**
     * Punto de entrada del flujo del asistente.
     * Verifica conexion con Ollama, solicita texto al usuario y procesa la intencion.
     */
    public void iniciarAsistente(Usuario usuario) {
        vista.mostrarCabecera("ASISTENTE INTELIGENTE - REGISTRO POR VOZ");

        vista.mostrarMensaje("Verificando conexion con Ollama...");
        if (!servicioIA.verificarConexion()) {
            vista.mostrarError("Ollama no esta disponible en http://localhost:11434");
            vista.mostrarMensaje("  Asegurese de que Ollama este corriendo y el modelo 'llama3.2' este descargado.");
            vista.mostrarMensaje("  Comando para iniciar el modelo: ollama run llama3.2");
            vista.esperarEnter();
            return;
        }
        vista.mostrarMensaje("Conexion con Ollama establecida.");

        List<CuentaFinanciera> cuentasUsuario = cuentaDAO.listarPorUsuario(usuario.getId());

        if (cuentasUsuario.isEmpty()) {
            vista.mostrarError("No tienes cuentas registradas. Agrega una cuenta primero.");
            vista.esperarEnter();
            return;
        }

        // Construir lista de cuentas para el System Prompt
        String listaCuentas = construirListaCuentas(cuentasUsuario);

        vista.mostrarMensaje("\nTus cuentas disponibles: " + listaCuentas);
        vista.mostrarMensaje("\nEscribe el movimiento en lenguaje natural. Ejemplos:");
        vista.mostrarMensaje("  - \"Gaste 50 soles en el cine pagando con mi Yape\"");
        vista.mostrarMensaje("  - \"Recibi mi sueldo de 2000 soles en BCP\"");
        vista.mostrarMensaje("  - \"Pague 30 soles de transporte con Plin\"");
        vista.mostrarMensaje("  (Escribe 'cancelar' para salir)");
        vista.mostrarMensaje("-".repeat(50));
        System.out.print("Tu movimiento: ");

        String textoUsuario = vista.leerLinea();

        if (textoUsuario.equalsIgnoreCase("cancelar") || textoUsuario.isBlank()) {
            vista.mostrarOperacionCancelada();
            vista.esperarEnter();
            return;
        }

        vista.mostrarMensaje("\nAnalizando con IA... (esto puede tomar unos segundos)");

        IntencionOperacionDTO dto = servicioIA.interpretarTexto(textoUsuario, listaCuentas);

        if (dto == null) {
            vista.mostrarError("No se pudo interpretar el texto. Intente de nuevo con mas detalle.");
            vista.esperarEnter();
            return;
        }

        mostrarResumenInterpretado(dto);

        System.out.print("\nConfirmar operacion? (s/n): ");
        String confirmacion = vista.leerLinea();

        if (!confirmacion.equalsIgnoreCase("s")) {
            vista.mostrarOperacionCancelada();
            vista.esperarEnter();
            return;
        }

        procesarOperacion(dto, cuentasUsuario, usuario);
    }

    /**
     * Muestra al usuario lo que la IA interpreto antes de confirmar.
     */
    private void mostrarResumenInterpretado(IntencionOperacionDTO dto) {
        vista.mostrarMensaje("\n--- Interpretacion de la IA ---");
        vista.mostrarMensaje("  Tipo       : " + (dto.getTipo()        != null ? dto.getTipo()        : "No detectado"));
        vista.mostrarMensaje("  Monto      : S/ " + (dto.getMonto()    != null ? String.format("%.2f", dto.getMonto()) : "No detectado"));
        vista.mostrarMensaje("  Categoria  : " + (dto.getCategoria()   != null ? dto.getCategoria()   : "Otros"));
        vista.mostrarMensaje("  Cuenta     : " + (dto.getCuenta()      != null ? dto.getCuenta()      : "No detectada"));
        vista.mostrarMensaje("  Descripcion: " + (dto.getDescripcion() != null ? dto.getDescripcion() : "-"));
        vista.mostrarMensaje("-".repeat(50));
    }

    /**
     * Valida el DTO, localiza la cuenta correspondiente y persiste la operacion.
     */
    private void procesarOperacion(IntencionOperacionDTO dto,
                                   List<CuentaFinanciera> cuentas,
                                   Usuario usuario) {
        if (!dto.esValido()) {
            vista.mostrarError("La IA no pudo extraer todos los datos necesarios (tipo, monto o cuenta).");
            vista.esperarEnter();
            return;
        }

        CuentaFinanciera cuentaDestino = resolverCuenta(dto.getCuenta(), cuentas);

        if (cuentaDestino == null) {
            vista.mostrarError("No se encontro una cuenta con el nombre: \"" + dto.getCuenta() + "\"");
            vista.mostrarMensaje("  Cuentas disponibles: " + construirListaCuentas(cuentas));
            vista.esperarEnter();
            return;
        }

        String tipo       = dto.getTipo();
        double monto      = dto.getMonto();
        String categoria  = (dto.getCategoria() != null && !dto.getCategoria().isBlank())
                            ? dto.getCategoria() : "Otros";
        String descripcion = (dto.getDescripcion() != null && !dto.getDescripcion().isBlank())
                            ? dto.getDescripcion() : "Registro via Asistente IA";

        MovimientoRegistro resultado = null;

        switch (tipo) {
            case "INGRESO" -> {
                resultado = transaccionDAO.registrarIngreso(cuentaDestino.getId(), monto, descripcion, categoria);
            }
            case "GASTO" -> {
                if (cuentaDestino.getSaldo() < monto) {
                    vista.mostrarError(String.format(
                        "Saldo insuficiente en %s. Disponible: S/ %.2f | Solicitado: S/ %.2f",
                        cuentaDestino.obtenerDetalleImprimible(), cuentaDestino.getSaldo(), monto));
                    vista.esperarEnter();
                    return;
                }
                resultado = transaccionDAO.registrarGasto(cuentaDestino.getId(), monto, descripcion, categoria);
            }
            default -> {
                vista.mostrarError("Tipo de operacion no soportado por el asistente: " + tipo);
                vista.esperarEnter();
                return;
            }
        }

        if (resultado != null) {
            CuentaFinanciera actualizada = cuentaDAO.buscarPorId(cuentaDestino.getId());
            double nuevoSaldo = (actualizada != null) ? actualizada.getSaldo()
                                                      : cuentaDestino.getSaldo() + (tipo.equals("INGRESO") ? monto : -monto);
            String signo = tipo.equals("INGRESO") ? "+" : "-";
            vista.mostrarExitoOperacion(
                tipo + " REGISTRADO VIA ASISTENTE IA",
                String.format("%s S/ %.2f en %s  [%s]", signo, monto,
                    cuentaDestino.obtenerDetalleImprimible(), categoria),
                String.format("Nuevo saldo: S/ %.2f", nuevoSaldo)
            );
        } else {
            vista.mostrarError("No se pudo registrar la operacion. Cambios revertidos.");
        }

        vista.esperarEnter();
    }

    /**
     * Busca la cuenta cuyo alias, proveedor o banco mas se asemeje al nombre extraido por la IA.
     * Estrategia: primero busqueda exacta (case-insensitive), luego busqueda parcial.
     */
    private CuentaFinanciera resolverCuenta(String nombreCuentaIA, List<CuentaFinanciera> cuentas) {
        if (nombreCuentaIA == null || nombreCuentaIA.isBlank()) return null;

        String nombreNorm = nombreCuentaIA.toLowerCase().trim();

        // 1. Busqueda exacta en el detalle imprimible
        for (CuentaFinanciera c : cuentas) {
            if (c.obtenerDetalleImprimible().toLowerCase().contains(nombreNorm)) {
                return c;
            }
        }

        // 2. Busqueda parcial: la IA puede retornar "Yape" y la cuenta tiene alias "Yape Personal"
        for (CuentaFinanciera c : cuentas) {
            String detalle = c.obtenerDetalleImprimible().toLowerCase();
            for (String palabra : nombreNorm.split("\\s+")) {
                if (detalle.contains(palabra)) return c;
            }
        }

        return null;
    }

    /**
     * Construye una cadena con los nombres de las cuentas separados por coma
     * para incluir en el System Prompt de la IA.
     */
    private String construirListaCuentas(List<CuentaFinanciera> cuentas) {
        return cuentas.stream()
                .map(CuentaFinanciera::obtenerDetalleImprimible)
                .collect(Collectors.joining(", "));
    }
}

