package view;

import modelo.CuentaFinanciera;
import modelo.MovimientoRegistro;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/** Vista de consola. Centraliza toda la entrada/salida del usuario. */
public class ConsoleView {

    private final Scanner scanner;

    public ConsoleView() {
        this.scanner = new Scanner(System.in);
    }

    public String leerLinea() {
        return scanner.nextLine().trim();
    }

    public int leerEntero() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                mostrarError("Por favor, ingrese un numero valido.");
            }
        }
    }

    public String solicitarNumeroWhatsApp() {
        mostrarMensaje("\n" + "=".repeat(50));
        mostrarMensaje("INGRESE SU NUMERO DE WHATSAPP");
        mostrarMensaje("=".repeat(50));
        System.out.print("Numero: ");
        return leerLinea();
    }

    public String solicitarNombre() {
        mostrarMensaje("\n" + "-".repeat(50));
        mostrarMensaje("REGISTRO DE NUEVO USUARIO");
        mostrarMensaje("-".repeat(50));
        mostrarMensaje("Numero no reconocido. Ingrese su nombre para registrarse.");
        System.out.print("Nombre: ");
        return leerLinea();
    }

    public void mostrarBienvenidaUsuarioExistente(String nombre) {
        mostrarMensaje("\n" + "=".repeat(50));
        mostrarMensaje("Bienvenido nuevamente, " + nombre.toUpperCase() + ".");
        mostrarMensaje("=".repeat(50));
    }

    public void mostrarBienvenidaNuevoUsuario(String nombre) {
        mostrarMensaje("\n" + "=".repeat(50));
        mostrarMensaje("Registro exitoso. Bienvenido, " + nombre.toUpperCase() + ".");
        mostrarMensaje("=".repeat(50));
    }

    public void mostrarMenuPrincipal() {
        mostrarMensaje("\n" + "-".repeat(50));
        mostrarMensaje("MENU PRINCIPAL - CHATFINANCE");
        mostrarMensaje("-".repeat(50));
        mostrarMensaje("1. Ver Mis Cuentas y Saldos");
        mostrarMensaje("2. Agregar Nueva Cuenta");
        mostrarMensaje("3. Operaciones (Ingresos / Gastos / Transferencias)");
        mostrarMensaje("4. Reportes y Analitica");
        mostrarMensaje("5. Salir");
        mostrarMensaje("\nTip: Opcion 99 para datos de prueba");
        mostrarMensaje("-".repeat(50));
        System.out.print("Seleccione una opcion: ");
    }

    public void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    public void mostrarError(String error) {
        System.err.println("ERROR: " + error);
    }

    public void mostrarDespedida() {
        mostrarMensaje("\n" + "=".repeat(50));
        mostrarMensaje("Hasta pronto. Gracias por usar ChatFinance.");
        mostrarMensaje("=".repeat(50) + "\n");
    }

    public int mostrarMenuTipoCuenta() {
        mostrarMensaje("\n" + "=".repeat(50));
        mostrarMensaje("AGREGAR NUEVA CUENTA");
        mostrarMensaje("=".repeat(50));
        mostrarMensaje("1. Billetera Digital (Yape, Plin, etc.)");
        mostrarMensaje("2. Cuenta Bancaria");
        mostrarMensaje("0. Cancelar");
        mostrarMensaje("-".repeat(50));
        System.out.print("Seleccione una opcion: ");
        return leerEntero();
    }

    public String[] solicitarDatosBilletera() {
        mostrarMensaje("\n" + "-".repeat(50));
        mostrarMensaje("NUEVA BILLETERA DIGITAL");
        mostrarMensaje("-".repeat(50));

        System.out.print("Alias (ej: Yape Personal): ");
        String alias = leerLinea();

        System.out.print("Proveedor (ej: BCP, Interbank): ");
        String proveedor = leerLinea();

        System.out.print("Numero de celular asociado: ");
        String numeroCuenta = leerLinea();

        String saldo = solicitarSaldoInicial();
        return new String[]{alias, proveedor, numeroCuenta, saldo};
    }

    public String[] solicitarDatosCuentaBancaria() {
        mostrarMensaje("\n" + "-".repeat(50));
        mostrarMensaje("NUEVA CUENTA BANCARIA");
        mostrarMensaje("-".repeat(50));

        System.out.print("Nombre del Banco (ej: BCP, Interbank): ");
        String banco = leerLinea();

        System.out.print("Numero de cuenta: ");
        String numeroCuenta = leerLinea();

        System.out.print("CCI (20 digitos, opcional - Enter para omitir): ");
        String cci = leerLinea();
        if (cci.isEmpty()) cci = null;

        String saldo = solicitarSaldoInicial();
        return new String[]{banco, cci, numeroCuenta, saldo};
    }

    private String solicitarSaldoInicial() {
        while (true) {
            System.out.print("Saldo inicial (S/): ");
            try {
                String input = leerLinea();
                double saldo = Double.parseDouble(input);
                if (saldo < 0) { mostrarError("El saldo no puede ser negativo."); continue; }
                return String.valueOf(saldo);
            } catch (NumberFormatException e) {
                mostrarError("Ingrese un numero valido (ej: 100.50)");
            }
        }
    }

    public void mostrarCuentaCreada(String tipoCuenta, String detalle) {
        mostrarMensaje("\n" + "=".repeat(50));
        mostrarMensaje("Cuenta creada exitosamente.");
        mostrarMensaje("=".repeat(50));
        mostrarMensaje("Tipo: " + tipoCuenta);
        mostrarMensaje("Detalle: " + detalle);
        mostrarMensaje("=".repeat(50));
    }

    public void mostrarOperacionCancelada() {
        mostrarMensaje("\nOperacion cancelada.");
    }

    public void esperarEnter() {
        System.out.print("\nPresione Enter para continuar...");
        scanner.nextLine();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Operaciones financieras
    // ─────────────────────────────────────────────────────────────────────────

    public void mostrarMenuOperaciones() {
        mostrarMensaje("\n" + "=".repeat(50));
        mostrarMensaje("OPERACIONES FINANCIERAS");
        mostrarMensaje("=".repeat(50));
        mostrarMensaje("1. Registrar Ingreso");
        mostrarMensaje("2. Registrar Gasto");
        mostrarMensaje("3. Transferir entre mis cuentas");
        mostrarMensaje("4. Ver Ultimos Movimientos");
        mostrarMensaje("0. Volver al Menu Principal");
        mostrarMensaje("-".repeat(50));
        System.out.print("Seleccione una opcion: ");
    }

    public void mostrarCabecera(String titulo) {
        mostrarMensaje("\n" + "=".repeat(50));
        mostrarMensaje(titulo);
        mostrarMensaje("=".repeat(50));
    }

    /**
     * Muestra la lista numerada de cuentas y retorna el indice (base 0) elegido por el usuario,
     * o -1 si cancela.
     */
    public int seleccionarCuentaDeLista(List<CuentaFinanciera> cuentas, String pregunta) {
        mostrarMensaje("\n" + pregunta);
        mostrarMensaje("-".repeat(50));

        int i = 1;
        for (CuentaFinanciera c : cuentas) {
            System.out.printf("%d. %s  |  S/ %.2f%n", i++, c.obtenerDetalleImprimible(), c.getSaldo());
        }
        mostrarMensaje("0. Cancelar");
        mostrarMensaje("-".repeat(50));
        System.out.print("Seleccione una cuenta: ");

        int opcion = leerEntero();
        if (opcion <= 0 || opcion > cuentas.size()) return -1;
        return opcion - 1;
    }

    public double solicitarMonto(String etiqueta) {
        while (true) {
            System.out.print(etiqueta + " (S/): ");
            try {
                double monto = Double.parseDouble(leerLinea());
                if (monto > 0) return monto;
                mostrarError("El monto debe ser mayor a cero.");
            } catch (NumberFormatException e) {
                mostrarError("Ingrese un numero valido (ej: 100.50).");
            }
        }
    }

    public String solicitarDescripcion(String etiqueta) {
        System.out.print(etiqueta + ": ");
        return leerLinea();
    }

    public void mostrarExitoOperacion(String operacion, String detalle, String resumen) {
        mostrarMensaje("\n" + "=".repeat(50));
        mostrarMensaje(operacion);
        mostrarMensaje("=".repeat(50));
        mostrarMensaje("   " + detalle);
        mostrarMensaje("   " + resumen);
        mostrarMensaje("=".repeat(50));
    }

    public void mostrarListaMovimientos(List<? extends MovimientoRegistro> movimientos) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

        mostrarMensaje(String.format("%-13s %-15s %-10s %-22s %-20s",
                "FECHA", "TIPO", "MONTO(S/)", "CATEGORIA", "DESCRIPCION"));
        mostrarMensaje("-".repeat(82));

        for (MovimientoRegistro m : movimientos) {
            String fecha      = (m.getFecha()       != null) ? m.getFecha().format(fmt) : "-";
            String tipo       = m.getTipo().toString();
            String signo      = switch (m.getTipo()) {
                case INGRESO       -> "+";
                case GASTO         -> "-";
                case TRANSFERENCIA -> "=";
            };
            String categoria  = (m.getCategoria()   != null) ? m.getCategoria()   : "-";
            String descripcion= (m.getDescripcion() != null) ? m.getDescripcion() : "-";

            System.out.printf("%-13s %-15s %s%-9.2f %-22s %-20s%n",
                    fecha, tipo, signo, m.getMonto(), categoria, descripcion);
        }
        mostrarMensaje("-".repeat(82));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Categorias y Reportes
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Muestra un menu numerado de categorias y retorna la elegida.
     * Si la entrada es invalida retorna la ultima opcion del array (convencion: "Otros").
     */
    public String seleccionarCategoria(String[] categorias, String titulo) {
        mostrarMensaje("\n-- " + titulo + " --");
        for (int i = 0; i < categorias.length; i++) {
            System.out.printf("%d. %s%n", i + 1, categorias[i]);
        }
        System.out.print("Seleccione categoria: ");

        try {
            int opcion = leerEntero();
            if (opcion >= 1 && opcion <= categorias.length) return categorias[opcion - 1];
        } catch (Exception ignorada) { }

        return categorias[categorias.length - 1];
    }

    /**
     * Imprime el reporte analitico con barras de progreso ASCII proporcionales
     * y el balance neto ingreso - gasto.
     *
     * @param gastos    Map categoria -> total gastado (instanciado como HashMap en el DAO)
     * @param ingresos  Map categoria -> total ingresado
     */
    public void mostrarReporteAnalitico(Map<String, Double> gastos, Map<String, Double> ingresos) {
        mostrarMensaje("\n" + "=".repeat(60));
        mostrarMensaje("REPORTE ANALITICO DE FINANZAS PERSONALES");
        mostrarMensaje("=".repeat(60));

        mostrarMensaje("\nRESUMEN DE GASTOS POR CATEGORIA");
        mostrarMensaje("-".repeat(60));
        if (gastos.isEmpty()) {
            mostrarMensaje("  Sin gastos registrados.");
        } else {
            double totalGastos = gastos.values().stream().mapToDouble(Double::doubleValue).sum();
            for (Map.Entry<String, Double> entrada : gastos.entrySet()) {
                double pct    = (totalGastos > 0) ? (entrada.getValue() / totalGastos * 100) : 0;
                int    barLen = (int) (pct / 5);
                String barra  = "#".repeat(barLen) + ".".repeat(20 - barLen);
                System.out.printf("  %-22s S/ %8.2f  %5.1f%%  [%s]%n",
                        entrada.getKey(), entrada.getValue(), pct, barra);
            }
            mostrarMensaje("-".repeat(60));
            System.out.printf("  %-22s S/ %8.2f%n", "TOTAL GASTADO", totalGastos);
        }

        mostrarMensaje("\nRESUMEN DE INGRESOS POR CATEGORIA");
        mostrarMensaje("-".repeat(60));
        if (ingresos.isEmpty()) {
            mostrarMensaje("  Sin ingresos registrados.");
        } else {
            double totalIngresos = ingresos.values().stream().mapToDouble(Double::doubleValue).sum();
            for (Map.Entry<String, Double> entrada : ingresos.entrySet()) {
                double pct    = (totalIngresos > 0) ? (entrada.getValue() / totalIngresos * 100) : 0;
                int    barLen = (int) (pct / 5);
                String barra  = "#".repeat(barLen) + ".".repeat(20 - barLen);
                System.out.printf("  %-22s S/ %8.2f  %5.1f%%  [%s]%n",
                        entrada.getKey(), entrada.getValue(), pct, barra);
            }
            mostrarMensaje("-".repeat(60));
            System.out.printf("  %-22s S/ %8.2f%n", "TOTAL INGRESADO", totalIngresos);
        }

        if (!gastos.isEmpty() || !ingresos.isEmpty()) {
            double totalG  = gastos.values().stream().mapToDouble(Double::doubleValue).sum();
            double totalI  = ingresos.values().stream().mapToDouble(Double::doubleValue).sum();
            double balance = totalI - totalG;
            mostrarMensaje("\n" + "=".repeat(60));
            System.out.printf("  %-22s S/ %8.2f%n",
                    balance >= 0 ? "BALANCE NETO (positivo)" : "BALANCE NETO (negativo)", balance);
            mostrarMensaje("=".repeat(60));
        }
    }

    public void cerrar() {
        if (scanner != null) scanner.close();
    }
}
