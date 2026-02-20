package controller;

import dao.CuentaDAO;
import dao.TransaccionDAO;
import modelo.CuentaFinanciera;
import modelo.MovimientoRegistro;
import modelo.Usuario;
import view.ConsoleView;

import java.util.List;

/**
 * Controlador para el Motor de Transacciones.
 * Coordina la Vista, el CuentaDAO y el TransaccionDAO para registrar
 * ingresos, gastos y transferencias con validaciones previas.
 *
 * FASE 3: Motor de Transacciones
 */
public class OperacionesController {

    private final ConsoleView vista;
    private final TransaccionDAO transaccionDAO;
    private final CuentaDAO cuentaDAO;

    public OperacionesController(ConsoleView vista) {
        this.vista           = vista;
        this.transaccionDAO  = new TransaccionDAO();
        this.cuentaDAO       = new CuentaDAO();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Menú principal de Operaciones
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Muestra el submenú de operaciones y gestiona las opciones.
     * Se llama desde LoginController (opción 3 del menú principal).
     *
     * @param usuario Usuario actualmente logueado
     */
    public void mostrarMenuOperaciones(Usuario usuario) {
        boolean continuar = true;

        while (continuar) {
            vista.mostrarMenuOperaciones();
            int opcion = vista.leerEntero();

            switch (opcion) {
                case 1 -> registrarIngreso(usuario);
                case 2 -> registrarGasto(usuario);
                case 3 -> realizarTransferencia(usuario);
                case 4 -> verUltimosMovimientos(usuario);
                case 0 -> continuar = false;
                default -> {
                    vista.mostrarError("Opción inválida.");
                    vista.esperarEnter();
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INGRESO
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Flujo para registrar un ingreso.
     * Pide al usuario elegir una cuenta y el monto/descripción del ingreso.
     */
    private void registrarIngreso(Usuario usuario) {
        List<CuentaFinanciera> cuentas = cuentaDAO.listarPorUsuario(usuario.getId());

        if (cuentas.isEmpty()) {
            vista.mostrarError("No tienes cuentas registradas. Agrega una primero.");
            vista.esperarEnter();
            return;
        }

        vista.mostrarCabecera("💵 REGISTRAR INGRESO");
        int cuentaIdx = vista.seleccionarCuentaDeLista(cuentas, "¿En qué cuenta entra el dinero?");
        if (cuentaIdx == -1) {
            vista.mostrarOperacionCancelada();
            vista.esperarEnter();
            return;
        }

        CuentaFinanciera cuentaSeleccionada = cuentas.get(cuentaIdx);

        double monto = vista.solicitarMonto("Monto a ingresar");
        if (monto <= 0) {
            vista.mostrarError("El monto debe ser mayor a cero.");
            vista.esperarEnter();
            return;
        }

        String descripcion = vista.solicitarDescripcion("Descripción del ingreso (ej: Sueldo, Freelance)");

        // Ejecutar en el DAO con transacción SQL atómica
        MovimientoRegistro resultado = transaccionDAO.registrarIngreso(
            cuentaSeleccionada.getId(), monto, descripcion
        );

        if (resultado != null) {
            // Recargar saldo actualizado
            CuentaFinanciera actualizada = cuentaDAO.buscarPorId(cuentaSeleccionada.getId());
            double nuevoSaldo = (actualizada != null) ? actualizada.getSaldo() : cuentaSeleccionada.getSaldo() + monto;

            vista.mostrarExitoOperacion("INGRESO REGISTRADO",
                String.format("+ S/ %.2f en %s", monto, cuentaSeleccionada.getDetalle()),
                String.format("Nuevo saldo: S/ %.2f", nuevoSaldo));
        } else {
            vista.mostrarError("No se pudo registrar el ingreso. Se revirtieron todos los cambios.");
        }

        vista.esperarEnter();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GASTO
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Flujo para registrar un gasto.
     * Valida que la cuenta tenga saldo suficiente antes de llamar al DAO.
     */
    private void registrarGasto(Usuario usuario) {
        List<CuentaFinanciera> cuentas = cuentaDAO.listarPorUsuario(usuario.getId());

        if (cuentas.isEmpty()) {
            vista.mostrarError("No tienes cuentas registradas. Agrega una primero.");
            vista.esperarEnter();
            return;
        }

        vista.mostrarCabecera("💸 REGISTRAR GASTO");
        int cuentaIdx = vista.seleccionarCuentaDeLista(cuentas, "¿De qué cuenta sale el dinero?");
        if (cuentaIdx == -1) {
            vista.mostrarOperacionCancelada();
            vista.esperarEnter();
            return;
        }

        CuentaFinanciera cuentaSeleccionada = cuentas.get(cuentaIdx);

        double monto = vista.solicitarMonto("Monto del gasto");
        if (monto <= 0) {
            vista.mostrarError("El monto debe ser mayor a cero.");
            vista.esperarEnter();
            return;
        }

        // ── Validación de saldo suficiente (en el Controlador) ────────────
        if (cuentaSeleccionada.getSaldo() < monto) {
            vista.mostrarError(String.format(
                "Saldo insuficiente. Disponible: S/ %.2f | Solicitado: S/ %.2f",
                cuentaSeleccionada.getSaldo(), monto
            ));
            vista.esperarEnter();
            return;
        }

        String descripcion = vista.solicitarDescripcion("Descripción del gasto (ej: Almuerzo, Taxi)");

        MovimientoRegistro resultado = transaccionDAO.registrarGasto(
            cuentaSeleccionada.getId(), monto, descripcion
        );

        if (resultado != null) {
            CuentaFinanciera actualizada = cuentaDAO.buscarPorId(cuentaSeleccionada.getId());
            double nuevoSaldo = (actualizada != null) ? actualizada.getSaldo() : cuentaSeleccionada.getSaldo() - monto;

            vista.mostrarExitoOperacion("GASTO REGISTRADO",
                String.format("- S/ %.2f en %s", monto, cuentaSeleccionada.getDetalle()),
                String.format("Nuevo saldo: S/ %.2f", nuevoSaldo));
        } else {
            vista.mostrarError("No se pudo registrar el gasto. Se revirtieron todos los cambios.");
        }

        vista.esperarEnter();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TRANSFERENCIA
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Flujo para realizar una transferencia entre dos cuentas del usuario.
     * Valida: saldo suficiente, origen ≠ destino.
     */
    private void realizarTransferencia(Usuario usuario) {
        List<CuentaFinanciera> cuentas = cuentaDAO.listarPorUsuario(usuario.getId());

        if (cuentas.size() < 2) {
            vista.mostrarError("Necesitas al menos 2 cuentas para hacer una transferencia.");
            vista.esperarEnter();
            return;
        }

        vista.mostrarCabecera("🔄 TRANSFERENCIA ENTRE CUENTAS");

        // Seleccionar cuenta origen
        int origenIdx = vista.seleccionarCuentaDeLista(cuentas, "Cuenta ORIGEN (de donde sale el dinero)");
        if (origenIdx == -1) {
            vista.mostrarOperacionCancelada();
            vista.esperarEnter();
            return;
        }
        CuentaFinanciera origen = cuentas.get(origenIdx);

        // Seleccionar cuenta destino (filtrando el origen de la lista visual)
        int destinoIdx = vista.seleccionarCuentaDeLista(cuentas, "Cuenta DESTINO (a donde llega el dinero)");
        if (destinoIdx == -1) {
            vista.mostrarOperacionCancelada();
            vista.esperarEnter();
            return;
        }
        CuentaFinanciera destino = cuentas.get(destinoIdx);

        // Validar que no sean la misma cuenta
        if (origen.getId().equals(destino.getId())) {
            vista.mostrarError("La cuenta de origen y destino no pueden ser la misma.");
            vista.esperarEnter();
            return;
        }

        double monto = vista.solicitarMonto("Monto a transferir");
        if (monto <= 0) {
            vista.mostrarError("El monto debe ser mayor a cero.");
            vista.esperarEnter();
            return;
        }

        // ── Validación de saldo suficiente ────────────────────────────────
        if (origen.getSaldo() < monto) {
            vista.mostrarError(String.format(
                "Saldo insuficiente en cuenta origen. Disponible: S/ %.2f | Solicitado: S/ %.2f",
                origen.getSaldo(), monto
            ));
            vista.esperarEnter();
            return;
        }

        String descripcion = vista.solicitarDescripcion(
            "Descripción (ej: Ahorro, Préstamo) [Enter para omitir]"
        );
        if (descripcion.isEmpty()) {
            descripcion = String.format("Transferencia de %s a %s", origen.getDetalle(), destino.getDetalle());
        }

        MovimientoRegistro resultado = transaccionDAO.realizarTransferencia(
            origen.getId(), destino.getId(), monto, descripcion
        );

        if (resultado != null) {
            CuentaFinanciera origenActualizado  = cuentaDAO.buscarPorId(origen.getId());
            CuentaFinanciera destinoActualizado = cuentaDAO.buscarPorId(destino.getId());

            double nuevoSaldoOrigen  = (origenActualizado  != null) ? origenActualizado.getSaldo()  : origen.getSaldo()  - monto;
            double nuevoSaldoDestino = (destinoActualizado != null) ? destinoActualizado.getSaldo() : destino.getSaldo() + monto;

            vista.mostrarExitoOperacion("TRANSFERENCIA REALIZADA",
                String.format("S/ %.2f  %s  →  %s", monto, origen.getDetalle(), destino.getDetalle()),
                String.format("Saldo origen: S/ %.2f | Saldo destino: S/ %.2f",
                              nuevoSaldoOrigen, nuevoSaldoDestino));
        } else {
            vista.mostrarError("No se pudo realizar la transferencia. Se revirtieron todos los cambios.");
        }

        vista.esperarEnter();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VER ÚLTIMOS MOVIMIENTOS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Muestra los últimos 10 movimientos del usuario, de todas sus cuentas.
     */
    private void verUltimosMovimientos(Usuario usuario) {
        List<MovimientoRegistro> movimientos = transaccionDAO.listarUltimosMovimientos(usuario.getId(), 10);

        vista.mostrarCabecera("📋 ÚLTIMOS MOVIMIENTOS");

        if (movimientos.isEmpty()) {
            vista.mostrarMensaje("⚠️  Aún no tienes movimientos registrados.");
        } else {
            vista.mostrarListaMovimientos(movimientos);
        }

        vista.esperarEnter();
    }
}

