package controller;

import dao.CuentaDAO;
import modelo.BilleteraDigital;
import modelo.CuentaBancaria;
import modelo.CuentaFinanciera;
import modelo.Usuario;
import view.ConsoleView;

import java.util.List;

/** Controlador para crear y listar cuentas financieras del usuario. */
public class CuentaController {

    private final CuentaDAO cuentaDAO;
    private final ConsoleView vista;

    public CuentaController(ConsoleView vista) {
        this.vista     = vista;
        this.cuentaDAO = new CuentaDAO();
    }

    public void verSaldos(Usuario usuario) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("MIS CUENTAS Y SALDOS");
        System.out.println("=".repeat(60));

        List<CuentaFinanciera> cuentas = cuentaDAO.listarPorUsuario(usuario.getId());

        if (cuentas.isEmpty()) {
            System.out.println("\nNo tienes cuentas registradas aun.");
            System.out.println("Tip: Usa la opcion 99 para agregar cuentas de prueba.");
        } else {
            System.out.println();
            int contador = 1;
            for (CuentaFinanciera cuenta : cuentas) {
                System.out.printf("%d. %s%n", contador++, cuenta.obtenerDetalleImprimible());
                System.out.printf("   Saldo: S/ %.2f%n%n", cuenta.getSaldo());
            }

            Double patrimonioTotal = cuentaDAO.calcularPatrimonioTotal(usuario.getId());
            System.out.println("-".repeat(60));
            System.out.printf("PATRIMONIO TOTAL: S/ %.2f%n", patrimonioTotal);
            System.out.println("=".repeat(60));
        }

        vista.esperarEnter();
    }

    public void crearCuentasDePrueba(Usuario usuario) {
        System.out.println("\nModo Desarrollador: Creando cuentas de prueba...\n");

        BilleteraDigital yape = new BilleteraDigital(
            usuario.getId(), "987654321", 50.00, "Yape Personal", "BCP");
        if (cuentaDAO.crear(yape) != null) System.out.println("Yape creada: S/ 50.00");

        CuentaBancaria bcp = new CuentaBancaria(
            usuario.getId(), "19312345678", 1500.00, "BCP", "00219300123456780123");
        if (cuentaDAO.crear(bcp) != null) System.out.println("Cuenta BCP creada: S/ 1500.00");

        BilleteraDigital plin = new BilleteraDigital(
            usuario.getId(), "987123456", 120.50, "Plin Personal", "Interbank");
        if (cuentaDAO.crear(plin) != null) System.out.println("Plin creada: S/ 120.50");

        System.out.println("\nCuentas de prueba creadas exitosamente.");
        vista.esperarEnter();
    }

    public void agregarCuenta(Usuario usuario) {
        int opcion = vista.mostrarMenuTipoCuenta();
        CuentaFinanciera nuevaCuenta = null;

        switch (opcion) {
            case 1 -> nuevaCuenta = crearBilleteraDigital(usuario);
            case 2 -> nuevaCuenta = crearCuentaBancaria(usuario);
            case 0 -> { vista.mostrarOperacionCancelada(); vista.esperarEnter(); return; }
            default -> { vista.mostrarError("Opcion invalida."); vista.esperarEnter(); return; }
        }

        if (nuevaCuenta != null) {
            CuentaFinanciera guardada = cuentaDAO.crear(nuevaCuenta);
            if (guardada != null) {
                vista.mostrarCuentaCreada(nuevaCuenta.getTipoCuenta(), nuevaCuenta.obtenerDetalleImprimible());
            } else {
                vista.mostrarError("No se pudo guardar la cuenta. Intente nuevamente.");
            }
        }

        vista.esperarEnter();
    }

    private BilleteraDigital crearBilleteraDigital(Usuario usuario) {
        try {
            String[] datos = vista.solicitarDatosBilletera();
            if (datos[0].isEmpty() || datos[1].isEmpty() || datos[2].isEmpty()) {
                vista.mostrarError("Todos los campos son obligatorios.");
                return null;
            }
            double saldo = Double.parseDouble(datos[3]);
            return new BilleteraDigital(usuario.getId(), datos[2], saldo, datos[0], datos[1]);
        } catch (NumberFormatException e) {
            vista.mostrarError("Error al procesar el saldo.");
            return null;
        }
    }

    private CuentaBancaria crearCuentaBancaria(Usuario usuario) {
        try {
            String[] datos = vista.solicitarDatosCuentaBancaria();
            if (datos[0].isEmpty() || datos[2].isEmpty()) {
                vista.mostrarError("El banco y numero de cuenta son obligatorios.");
                return null;
            }
            double saldo = Double.parseDouble(datos[3]);
            return new CuentaBancaria(usuario.getId(), datos[2], saldo, datos[0], datos[1]);
        } catch (NumberFormatException e) {
            vista.mostrarError("Error al procesar el saldo.");
            return null;
        }
    }
}
