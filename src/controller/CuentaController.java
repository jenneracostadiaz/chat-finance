package controller;

import dao.CuentaDAO;
import modelo.BilleteraDigital;
import modelo.CuentaBancaria;
import modelo.CuentaFinanciera;
import modelo.Usuario;
import view.ConsoleView;

import java.util.List;

/**
 * Controlador para gestionar las operaciones relacionadas con cuentas financieras.
 * FASE 2: Gestión de Cuentas y Saldos
 */
public class CuentaController {
    private CuentaDAO cuentaDAO;
    private ConsoleView vista;

    public CuentaController(ConsoleView vista) {
        this.vista = vista;
        this.cuentaDAO = new CuentaDAO();
    }

    /**
     * Muestra todas las cuentas del usuario con sus saldos y el patrimonio total.
     * Implementa polimorfismo: cada cuenta muestra su detalle según su tipo.
     *
     * @param usuario Usuario logueado
     */
    public void verSaldos(Usuario usuario) {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("💰 MIS CUENTAS Y SALDOS");
        System.out.println("═".repeat(60));

        List<CuentaFinanciera> cuentas = cuentaDAO.listarPorUsuario(usuario.getId());

        if (cuentas.isEmpty()) {
            System.out.println("\n⚠️  No tienes cuentas registradas aún.");
            System.out.println("💡 Usa la opción 99 (menú oculto) para agregar cuentas de prueba.");
        } else {
            System.out.println();
            int contador = 1;
            for (CuentaFinanciera cuenta : cuentas) {
                // Polimorfismo: cada cuenta muestra su detalle específico
                System.out.printf("%d. %s\n", contador++, cuenta.getDetalle());
                System.out.printf("   💵 Saldo: S/ %.2f\n\n", cuenta.getSaldo());
            }

            // Calcular y mostrar patrimonio total
            Double patrimonioTotal = cuentaDAO.calcularPatrimonioTotal(usuario.getId());
            System.out.println("─".repeat(60));
            System.out.printf("🏆 PATRIMONIO TOTAL: S/ %.2f\n", patrimonioTotal);
            System.out.println("═".repeat(60));
        }

        vista.esperarEnter();
    }

    /**
     * Crea cuentas de prueba (Seed Data) para demostración.
     * Método Mock para la Fase 2.
     *
     * @param usuario Usuario logueado
     */
    public void crearCuentasDePrueba(Usuario usuario) {
        System.out.println("\n🔧 Modo Desarrollador: Creando cuentas de prueba...\n");

        // Verificar si ya tiene cuentas
        List<CuentaFinanciera> cuentasExistentes = cuentaDAO.listarPorUsuario(usuario.getId());
        if (!cuentasExistentes.isEmpty()) {
            System.out.println("⚠️  Ya tienes cuentas registradas. Eliminando primero...");
            // En una implementación real, aquí habría un método para eliminar
        }

        // Crear Billetera Digital (Yape)
        BilleteraDigital yape = new BilleteraDigital(
            usuario.getId(),
            "987654321",  // número de celular
            50.00,
            "Yape Personal",
            "BCP"
        );

        CuentaFinanciera yapeCreada = cuentaDAO.crear(yape);
        if (yapeCreada != null) {
            System.out.println("✓ Yape creada: S/ 50.00");
        }

        // Crear Cuenta Bancaria (BCP)
        CuentaBancaria cuentaBcp = new CuentaBancaria(
            usuario.getId(),
            "19312345678",  // número de cuenta
            1500.00,
            "BCP",
            "00219300123456780123"  // CCI de 20 dígitos
        );

        CuentaFinanciera bcpCreada = cuentaDAO.crear(cuentaBcp);
        if (bcpCreada != null) {
            System.out.println("✓ Cuenta BCP creada: S/ 1500.00");
        }

        // Crear otra Billetera Digital (Plin)
        BilleteraDigital plin = new BilleteraDigital(
            usuario.getId(),
            "987123456",  // Número diferente
            120.50,
            "Plin Personal",
            "Interbank"
        );

        CuentaFinanciera plinCreada = cuentaDAO.crear(plin);
        if (plinCreada != null) {
            System.out.println("✓ Plin creada: S/ 120.50");
        }

        System.out.println("\n✓ Cuentas de prueba creadas exitosamente!");
        System.out.println("💡 Usa la opción 1 para ver tus saldos.\n");

        vista.esperarEnter();
    }

    /**
     * Agrega una nueva cuenta de forma interactiva.
     * Permite al usuario elegir el tipo de cuenta y llenar el formulario.
     *
     * @param usuario Usuario logueado
     */
    public void agregarCuenta(Usuario usuario) {
        // Mostrar menú de selección de tipo de cuenta
        int opcion = vista.mostrarMenuTipoCuenta();

        CuentaFinanciera nuevaCuenta = null;

        switch (opcion) {
            case 1:
                // Crear Billetera Digital
                nuevaCuenta = crearBilleteraDigital(usuario);
                break;

            case 2:
                // Crear Cuenta Bancaria
                nuevaCuenta = crearCuentaBancaria(usuario);
                break;

            case 0:
                // Cancelar operación
                vista.mostrarOperacionCancelada();
                vista.esperarEnter();
                return;

            default:
                vista.mostrarError("Opción inválida.");
                vista.esperarEnter();
                return;
        }

        // Si se creó una cuenta, guardarla en la BD
        if (nuevaCuenta != null) {
            CuentaFinanciera cuentaGuardada = cuentaDAO.crear(nuevaCuenta);

            if (cuentaGuardada != null) {
                // Mostrar confirmación con el detalle polimórfico
                vista.mostrarCuentaCreada(
                    nuevaCuenta.getTipoCuenta(),
                    nuevaCuenta.getDetalle()
                );
            } else {
                vista.mostrarError("No se pudo guardar la cuenta. Intente nuevamente.");
            }
        }

        vista.esperarEnter();
    }

    /**
     * Crea una Billetera Digital solicitando los datos por consola.
     *
     * @param usuario Usuario logueado
     * @return BilleteraDigital creada o null si hay error
     */
    private BilleteraDigital crearBilleteraDigital(Usuario usuario) {
        try {
            // Solicitar datos del formulario
            String[] datos = vista.solicitarDatosBilletera();

            // datos[0] = alias
            // datos[1] = proveedor
            // datos[2] = numeroCuenta
            // datos[3] = saldo

            // Validar que no estén vacíos
            if (datos[0].isEmpty() || datos[1].isEmpty() || datos[2].isEmpty()) {
                vista.mostrarError("Todos los campos son obligatorios.");
                return null;
            }

            // Parsear saldo
            double saldo = Double.parseDouble(datos[3]);

            // Crear la billetera
            return new BilleteraDigital(
                usuario.getId(),
                datos[2],  // numeroCuenta
                saldo,
                datos[0],  // alias
                datos[1]   // proveedor
            );

        } catch (NumberFormatException e) {
            vista.mostrarError("Error al procesar el saldo.");
            return null;
        } catch (Exception e) {
            vista.mostrarError("Error al crear la billetera: " + e.getMessage());
            return null;
        }
    }

    /**
     * Crea una Cuenta Bancaria solicitando los datos por consola.
     *
     * @param usuario Usuario logueado
     * @return CuentaBancaria creada o null si hay error
     */
    private CuentaBancaria crearCuentaBancaria(Usuario usuario) {
        try {
            // Solicitar datos del formulario
            String[] datos = vista.solicitarDatosCuentaBancaria();

            // datos[0] = banco
            // datos[1] = cci (puede ser null)
            // datos[2] = numeroCuenta
            // datos[3] = saldo

            // Validar que no estén vacíos (excepto CCI que es opcional)
            if (datos[0].isEmpty() || datos[2].isEmpty()) {
                vista.mostrarError("El banco y número de cuenta son obligatorios.");
                return null;
            }

            // Parsear saldo
            double saldo = Double.parseDouble(datos[3]);

            // Crear la cuenta bancaria
            return new CuentaBancaria(
                usuario.getId(),
                datos[2],  // numeroCuenta
                saldo,
                datos[0],  // banco
                datos[1]   // cci (puede ser null)
            );

        } catch (NumberFormatException e) {
            vista.mostrarError("Error al procesar el saldo.");
            return null;
        } catch (Exception e) {
            vista.mostrarError("Error al crear la cuenta bancaria: " + e.getMessage());
            return null;
        }
    }
}
