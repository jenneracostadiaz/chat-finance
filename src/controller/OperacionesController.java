package controller;

import dao.CuentaDAO;
import dao.TransaccionDAO;
import modelo.CuentaFinanciera;
import modelo.MovimientoRegistro;
import modelo.Usuario;
import view.ConsoleView;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OperacionesController {

    private static final int CAPACIDAD_HISTORIAL = 5;

    private final ConsoleView vista;
    private final TransaccionDAO transaccionDAO;
    private final CuentaDAO cuentaDAO;

    private final LinkedList<MovimientoRegistro> historialSesion;

    public OperacionesController(ConsoleView vista) {
        this.vista           = vista;
        this.transaccionDAO  = new TransaccionDAO();
        this.cuentaDAO       = new CuentaDAO();
        this.historialSesion = new LinkedList<>();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Menu de Operaciones
    // ─────────────────────────────────────────────────────────────────────────

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
                default -> { vista.mostrarError("Opcion invalida."); vista.esperarEnter(); }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Ingreso
    // ─────────────────────────────────────────────────────────────────────────

    private void registrarIngreso(Usuario usuario) {
        List<CuentaFinanciera> cuentas = cuentaDAO.listarPorUsuario(usuario.getId());
        if (cuentas.isEmpty()) {
            vista.mostrarError("No tienes cuentas registradas. Agrega una primero.");
            vista.esperarEnter();
            return;
        }

        vista.mostrarCabecera("REGISTRAR INGRESO");
        int cuentaIdx = vista.seleccionarCuentaDeLista(cuentas, "En que cuenta entra el dinero?");
        if (cuentaIdx == -1) { vista.mostrarOperacionCancelada(); vista.esperarEnter(); return; }

        CuentaFinanciera cuenta = cuentas.get(cuentaIdx);
        double monto = vista.solicitarMonto("Monto a ingresar");
        String categoria  = vista.seleccionarCategoria(MovimientoRegistro.CATEGORIAS_INGRESO, "Categoria del ingreso");
        String descripcion = vista.solicitarDescripcion("Descripcion breve");

        MovimientoRegistro resultado = transaccionDAO.registrarIngreso(cuenta.getId(), monto, descripcion, categoria);

        if (resultado != null) {
            agregarAlHistorial(resultado);
            CuentaFinanciera actualizada = cuentaDAO.buscarPorId(cuenta.getId());
            double nuevoSaldo = (actualizada != null) ? actualizada.getSaldo() : cuenta.getSaldo() + monto;
            vista.mostrarExitoOperacion("INGRESO REGISTRADO",
                String.format("+ S/ %.2f en %s  [%s]", monto, cuenta.obtenerDetalleImprimible(), categoria),
                String.format("Nuevo saldo: S/ %.2f", nuevoSaldo));
        } else {
            vista.mostrarError("No se pudo registrar el ingreso. Cambios revertidos.");
        }
        vista.esperarEnter();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Gasto
    // ─────────────────────────────────────────────────────────────────────────

    private void registrarGasto(Usuario usuario) {
        List<CuentaFinanciera> cuentas = cuentaDAO.listarPorUsuario(usuario.getId());
        if (cuentas.isEmpty()) {
            vista.mostrarError("No tienes cuentas registradas. Agrega una primero.");
            vista.esperarEnter();
            return;
        }

        vista.mostrarCabecera("REGISTRAR GASTO");
        int cuentaIdx = vista.seleccionarCuentaDeLista(cuentas, "De que cuenta sale el dinero?");
        if (cuentaIdx == -1) { vista.mostrarOperacionCancelada(); vista.esperarEnter(); return; }

        CuentaFinanciera cuenta = cuentas.get(cuentaIdx);
        double monto = vista.solicitarMonto("Monto del gasto");

        if (cuenta.getSaldo() < monto) {
            vista.mostrarError(String.format(
                "Saldo insuficiente. Disponible: S/ %.2f | Solicitado: S/ %.2f", cuenta.getSaldo(), monto));
            vista.esperarEnter();
            return;
        }

        String categoria  = vista.seleccionarCategoria(MovimientoRegistro.CATEGORIAS_GASTO, "Categoria del gasto");
        String descripcion = vista.solicitarDescripcion("Descripcion breve");

        MovimientoRegistro resultado = transaccionDAO.registrarGasto(cuenta.getId(), monto, descripcion, categoria);

        if (resultado != null) {
            agregarAlHistorial(resultado);
            CuentaFinanciera actualizada = cuentaDAO.buscarPorId(cuenta.getId());
            double nuevoSaldo = (actualizada != null) ? actualizada.getSaldo() : cuenta.getSaldo() - monto;
            vista.mostrarExitoOperacion("GASTO REGISTRADO",
                String.format("- S/ %.2f en %s  [%s]", monto, cuenta.obtenerDetalleImprimible(), categoria),
                String.format("Nuevo saldo: S/ %.2f", nuevoSaldo));
        } else {
            vista.mostrarError("No se pudo registrar el gasto. Cambios revertidos.");
        }
        vista.esperarEnter();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Transferencia
    // ─────────────────────────────────────────────────────────────────────────

    private void realizarTransferencia(Usuario usuario) {
        List<CuentaFinanciera> cuentas = cuentaDAO.listarPorUsuario(usuario.getId());
        if (cuentas.size() < 2) {
            vista.mostrarError("Necesitas al menos 2 cuentas para hacer una transferencia.");
            vista.esperarEnter();
            return;
        }

        vista.mostrarCabecera("TRANSFERENCIA ENTRE CUENTAS");

        int origenIdx = vista.seleccionarCuentaDeLista(cuentas, "Cuenta ORIGEN (de donde sale el dinero)");
        if (origenIdx == -1) { vista.mostrarOperacionCancelada(); vista.esperarEnter(); return; }
        CuentaFinanciera origen = cuentas.get(origenIdx);

        int destinoIdx = vista.seleccionarCuentaDeLista(cuentas, "Cuenta DESTINO (a donde llega el dinero)");
        if (destinoIdx == -1) { vista.mostrarOperacionCancelada(); vista.esperarEnter(); return; }
        CuentaFinanciera destino = cuentas.get(destinoIdx);

        if (origen.getId().equals(destino.getId())) {
            vista.mostrarError("La cuenta de origen y destino no pueden ser la misma.");
            vista.esperarEnter();
            return;
        }

        double monto = vista.solicitarMonto("Monto a transferir");

        if (origen.getSaldo() < monto) {
            vista.mostrarError(String.format(
                "Saldo insuficiente en cuenta origen. Disponible: S/ %.2f | Solicitado: S/ %.2f",
                origen.getSaldo(), monto));
            vista.esperarEnter();
            return;
        }

        String descripcion = vista.solicitarDescripcion("Descripcion (Enter para omitir)");
        if (descripcion.isEmpty()) {
            descripcion = String.format("Transferencia de %s a %s",
                origen.obtenerDetalleImprimible(), destino.obtenerDetalleImprimible());
        }

        MovimientoRegistro resultado = transaccionDAO.realizarTransferencia(
            origen.getId(), destino.getId(), monto, descripcion);

        if (resultado != null) {
            agregarAlHistorial(resultado);
            CuentaFinanciera origenAct  = cuentaDAO.buscarPorId(origen.getId());
            CuentaFinanciera destinoAct = cuentaDAO.buscarPorId(destino.getId());
            double saldoOrigen  = (origenAct  != null) ? origenAct.getSaldo()  : origen.getSaldo()  - monto;
            double saldoDestino = (destinoAct != null) ? destinoAct.getSaldo() : destino.getSaldo() + monto;
            vista.mostrarExitoOperacion("TRANSFERENCIA REALIZADA",
                String.format("S/ %.2f  %s  ->  %s", monto,
                    origen.obtenerDetalleImprimible(), destino.obtenerDetalleImprimible()),
                String.format("Saldo origen: S/ %.2f | Saldo destino: S/ %.2f", saldoOrigen, saldoDestino));
        } else {
            vista.mostrarError("No se pudo realizar la transferencia. Cambios revertidos.");
        }
        vista.esperarEnter();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Historial en memoria (LinkedList)
    // ─────────────────────────────────────────────────────────────────────────

    private void agregarAlHistorial(MovimientoRegistro movimiento) {
        historialSesion.addFirst(movimiento);
        if (historialSesion.size() > CAPACIDAD_HISTORIAL) {
            historialSesion.removeLast();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Ultimos movimientos
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Muestra los ultimos movimientos. Prioriza el historial en memoria de la sesion actual;
     * si esta vacio, consulta la base de datos como respaldo.
     */
    private void verUltimosMovimientos(Usuario usuario) {
        vista.mostrarCabecera("ULTIMOS MOVIMIENTOS");

        if (!historialSesion.isEmpty()) {
            vista.mostrarMensaje("(Historial de la sesion actual)");
            vista.mostrarListaMovimientos(historialSesion);
        } else {
            List<MovimientoRegistro> movimientos = transaccionDAO.listarUltimosMovimientos(usuario.getId(), 10);
            if (movimientos.isEmpty()) {
                vista.mostrarMensaje("Aun no tienes movimientos registrados.");
            } else {
                vista.mostrarListaMovimientos(movimientos);
            }
        }
        vista.esperarEnter();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Reporte analitico
    // ─────────────────────────────────────────────────────────────────────────

    public void verReporteAnalitico(Usuario usuario) {
        Map<String, Double> resumenGastos   = transaccionDAO.obtenerResumenGastos(usuario.getId());
        Map<String, Double> resumenIngresos = transaccionDAO.obtenerResumenIngresos(usuario.getId());
        vista.mostrarReporteAnalitico(resumenGastos, resumenIngresos);
    }
}
