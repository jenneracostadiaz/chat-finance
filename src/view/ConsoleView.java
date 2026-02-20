package view;

import modelo.CuentaFinanciera;
import modelo.MovimientoRegistro;

import java.time.format.DateTimeFormatter;
import java.util.List;
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
        mostrarMensaje("2. Agregar Nueva Cuenta");
        mostrarMensaje("3. 💳 Operaciones (Ingresos / Gastos / Transferencias)");
        mostrarMensaje("4. 📊 Reportes y Analítica");
        mostrarMensaje("5. Salir");
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
     * Muestra el menú de selección de tipo de cuenta.
     * @return int con la opción seleccionada (1: Billetera, 2: Banco, 0: Cancelar)
     */
    public int mostrarMenuTipoCuenta() {
        mostrarMensaje("\n" + "═".repeat(50));
        mostrarMensaje("➕ AGREGAR NUEVA CUENTA");
        mostrarMensaje("═".repeat(50));
        mostrarMensaje("¿Qué tipo de cuenta deseas agregar?");
        mostrarMensaje("\n1. 💳 Billetera Digital (Yape, Plin, etc.)");
        mostrarMensaje("2. 🏦 Cuenta Bancaria");
        mostrarMensaje("0. ❌ Cancelar");
        mostrarMensaje("─".repeat(50));
        System.out.print("➤ Seleccione una opción: ");
        return leerEntero();
    }

    /**
     * Solicita los datos para crear una Billetera Digital.
     * @return String[] con [alias, proveedor, numeroCuenta, saldo]
     */
    public String[] solicitarDatosBilletera() {
        mostrarMensaje("\n" + "─".repeat(50));
        mostrarMensaje("💳 NUEVA BILLETERA DIGITAL");
        mostrarMensaje("─".repeat(50));

        // Alias
        System.out.print("➤ Alias (ej: Yape Personal, Plin Trabajo): ");
        String alias = leerLinea();

        // Proveedor
        System.out.print("➤ Proveedor (ej: BCP, Interbank, BBVA): ");
        String proveedor = leerLinea();

        // Número de cuenta (celular)
        System.out.print("➤ Número de celular asociado: ");
        String numeroCuenta = leerLinea();

        // Saldo inicial
        String saldo = solicitarSaldoInicial();

        return new String[]{alias, proveedor, numeroCuenta, saldo};
    }

    /**
     * Solicita los datos para crear una Cuenta Bancaria.
     * @return String[] con [banco, cci, numeroCuenta, saldo]
     */
    public String[] solicitarDatosCuentaBancaria() {
        mostrarMensaje("\n" + "─".repeat(50));
        mostrarMensaje("🏦 NUEVA CUENTA BANCARIA");
        mostrarMensaje("─".repeat(50));

        // Banco
        System.out.print("➤ Nombre del Banco (ej: BCP, Interbank, BBVA): ");
        String banco = leerLinea();

        // Número de cuenta
        System.out.print("➤ Número de cuenta: ");
        String numeroCuenta = leerLinea();

        // CCI
        System.out.print("➤ CCI (20 dígitos, opcional - Enter para omitir): ");
        String cci = leerLinea();
        if (cci.isEmpty()) {
            cci = null;
        }

        // Saldo inicial
        String saldo = solicitarSaldoInicial();

        return new String[]{banco, cci, numeroCuenta, saldo};
    }

    /**
     * Solicita el saldo inicial con validación (no negativo).
     * @return String con el saldo validado
     */
    private String solicitarSaldoInicial() {
        while (true) {
            System.out.print("➤ Saldo inicial (S/): ");
            try {
                String input = leerLinea();
                double saldo = Double.parseDouble(input);

                if (saldo < 0) {
                    mostrarError("El saldo no puede ser negativo. Intente nuevamente.");
                    continue;
                }

                return String.valueOf(saldo);
            } catch (NumberFormatException e) {
                mostrarError("Por favor, ingrese un número válido (ej: 100.50)");
            }
        }
    }

    /**
     * Muestra mensaje de confirmación de cuenta creada.
     * @param tipoCuenta Tipo de cuenta creada
     * @param detalle Detalle de la cuenta
     */
    public void mostrarCuentaCreada(String tipoCuenta, String detalle) {
        mostrarMensaje("\n" + "═".repeat(50));
        mostrarMensaje("✓ ¡CUENTA CREADA EXITOSAMENTE! 🎉");
        mostrarMensaje("═".repeat(50));
        mostrarMensaje("Tipo: " + tipoCuenta);
        mostrarMensaje("Detalle: " + detalle);
        mostrarMensaje("═".repeat(50));
    }

    /**
     * Muestra mensaje de operación cancelada.
     */
    public void mostrarOperacionCancelada() {
        mostrarMensaje("\n⚠️  Operación cancelada.");
    }

    /**
     * Espera a que el usuario presione Enter para continuar.
     */
    public void esperarEnter() {
        System.out.print("\n➤ Presione Enter para continuar...");
        scanner.nextLine();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FASE 3: Motor de Transacciones - Métodos de Vista
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Muestra el submenú de operaciones financieras.
     */
    public void mostrarMenuOperaciones() {
        mostrarMensaje("\n" + "═".repeat(50));
        mostrarMensaje("💳 OPERACIONES FINANCIERAS");
        mostrarMensaje("═".repeat(50));
        mostrarMensaje("1. 💵 Registrar Ingreso");
        mostrarMensaje("2. 💸 Registrar Gasto");
        mostrarMensaje("3. 🔄 Transferir entre mis cuentas");
        mostrarMensaje("4. 📋 Ver Últimos Movimientos");
        mostrarMensaje("0. ◀  Volver al Menú Principal");
        mostrarMensaje("─".repeat(50));
        System.out.print("➤ Seleccione una opción: ");
    }

    /**
     * Muestra una cabecera de sección con formato.
     * @param titulo Título de la sección
     */
    public void mostrarCabecera(String titulo) {
        mostrarMensaje("\n" + "═".repeat(50));
        mostrarMensaje(titulo);
        mostrarMensaje("═".repeat(50));
    }

    /**
     * Muestra la lista de cuentas numerada y pide al usuario seleccionar una.
     * @param cuentas  Lista de cuentas a mostrar
     * @param pregunta Texto descriptivo de la selección
     * @return Índice (base 0) de la cuenta seleccionada, o -1 si cancela
     */
    public int seleccionarCuentaDeLista(List<CuentaFinanciera> cuentas, String pregunta) {
        mostrarMensaje("\n" + pregunta);
        mostrarMensaje("─".repeat(50));

        int i = 1;
        for (CuentaFinanciera c : cuentas) {
            System.out.printf("%d. %s  │  S/ %.2f%n", i++, c.getDetalle(), c.getSaldo());
        }
        mostrarMensaje("0. ❌ Cancelar");
        mostrarMensaje("─".repeat(50));
        System.out.print("➤ Seleccione una cuenta: ");

        int opcion = leerEntero();
        if (opcion == 0 || opcion < 0 || opcion > cuentas.size()) {
            return -1;
        }
        return opcion - 1; // convertir a índice base 0
    }

    /**
     * Solicita un monto al usuario con validación básica (> 0).
     * @param etiqueta Texto del prompt
     * @return double con el monto ingresado (siempre positivo)
     */
    public double solicitarMonto(String etiqueta) {
        while (true) {
            System.out.print("➤ " + etiqueta + " (S/): ");
            try {
                double monto = Double.parseDouble(leerLinea());
                if (monto > 0) return monto;
                mostrarError("El monto debe ser mayor a cero.");
            } catch (NumberFormatException e) {
                mostrarError("Ingrese un número válido (ej: 100.50).");
            }
        }
    }

    /**
     * Solicita una descripción de texto al usuario.
     * @param etiqueta Texto del prompt
     * @return String con la descripción (puede estar vacío si el usuario presiona Enter)
     */
    public String solicitarDescripcion(String etiqueta) {
        System.out.print("➤ " + etiqueta + ": ");
        return leerLinea();
    }

    /**
     * Muestra un mensaje de éxito para una operación financiera.
     * @param operacion  Nombre de la operación (ej: "INGRESO REGISTRADO")
     * @param detalle    Línea de detalle (ej: "+ S/ 100 en Yape")
     * @param resumen    Línea de resumen (ej: "Nuevo saldo: S/ 500.00")
     */
    public void mostrarExitoOperacion(String operacion, String detalle, String resumen) {
        mostrarMensaje("\n" + "═".repeat(50));
        mostrarMensaje("✅ " + operacion);
        mostrarMensaje("═".repeat(50));
        mostrarMensaje("   " + detalle);
        mostrarMensaje("   " + resumen);
        mostrarMensaje("═".repeat(50));
    }

    /**
     * Muestra la lista de últimos movimientos con formato de tabla.
     * FASE 4: incluye columna Categoría.
     * @param movimientos Lista de movimientos a mostrar
     */
    public void mostrarListaMovimientos(List<MovimientoRegistro> movimientos) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

        mostrarMensaje(String.format("%-13s %-15s %-10s %-20s %-20s",
                "FECHA", "TIPO", "MONTO(S/)", "CATEGORÍA", "DESCRIPCIÓN"));
        mostrarMensaje("─".repeat(80));

        for (MovimientoRegistro m : movimientos) {
            String fecha      = (m.getFecha()     != null) ? m.getFecha().format(fmt) : "-";
            String tipo       = m.getTipo().toString();
            String signo      = switch (m.getTipo()) {
                case INGRESO       -> "+";
                case GASTO         -> "-";
                case TRANSFERENCIA -> "→";
            };
            String categoria  = (m.getCategoria()    != null) ? m.getCategoria()    : "-";
            String descripcion= (m.getDescripcion()  != null) ? m.getDescripcion()  : "-";

            System.out.printf("%-13s %-15s %s%-9.2f %-20s %-20s%n",
                    fecha, tipo, signo, m.getMonto(), categoria, descripcion);
        }
        mostrarMensaje("─".repeat(80));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FASE 4: Categorías y Reportes Analíticos
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Muestra un menú numerado de categorías y devuelve la seleccionada.
     * Si el usuario elige 0 o un índice inválido, devuelve la última categoría ("Otros").
     *
     * @param categorias Array de nombres de categorías (de MovimientoRegistro)
     * @param titulo     Texto del encabezado del menú
     * @return String con la categoría elegida
     */
    public String seleccionarCategoria(String[] categorias, String titulo) {
        mostrarMensaje("\n── " + titulo + " ──");
        for (int i = 0; i < categorias.length; i++) {
            System.out.printf("%d. %s%n", i + 1, categorias[i]);
        }
        System.out.print("➤ Seleccione categoría: ");

        try {
            int opcion = leerEntero();
            if (opcion >= 1 && opcion <= categorias.length) {
                return categorias[opcion - 1];
            }
        } catch (Exception ignored) { }

        // Fallback: última categoría = "Otros"
        return categorias[categorias.length - 1];
    }

    /**
     * Imprime el reporte analítico de gastos e ingresos por categoría con porcentajes.
     * Usa una barra ASCII proporcional para visualizar la distribución.
     *
     * @param gastos    Map categoría → total (gastos)
     * @param ingresos  Map categoría → total (ingresos)
     */
    public void mostrarReporteAnalitico(java.util.Map<String, Double> gastos,
                                        java.util.Map<String, Double> ingresos) {
        mostrarMensaje("\n" + "═".repeat(60));
        mostrarMensaje("📊 REPORTE ANALÍTICO DE FINANZAS PERSONALES");
        mostrarMensaje("═".repeat(60));

        // ── Sección GASTOS ──────────────────────────────────────────────
        mostrarMensaje("\n💸 RESUMEN DE GASTOS POR CATEGORÍA");
        mostrarMensaje("─".repeat(60));

        if (gastos.isEmpty()) {
            mostrarMensaje("  ⚠️  Aún no tienes gastos registrados.");
        } else {
            double totalGastos = gastos.values().stream().mapToDouble(Double::doubleValue).sum();
            for (java.util.Map.Entry<String, Double> entry : gastos.entrySet()) {
                double pct      = (totalGastos > 0) ? (entry.getValue() / totalGastos * 100) : 0;
                int    barLen   = (int) (pct / 5);          // cada █ = 5 %
                String barra    = "█".repeat(barLen) + "░".repeat(20 - barLen);
                System.out.printf("  %-22s S/ %8.2f  %5.1f%%  %s%n",
                        entry.getKey(), entry.getValue(), pct, barra);
            }
            mostrarMensaje("─".repeat(60));
            System.out.printf("  %-22s S/ %8.2f%n", "TOTAL GASTADO", totalGastos);
        }

        // ── Sección INGRESOS ─────────────────────────────────────────────
        mostrarMensaje("\n💵 RESUMEN DE INGRESOS POR CATEGORÍA");
        mostrarMensaje("─".repeat(60));

        if (ingresos.isEmpty()) {
            mostrarMensaje("  ⚠️  Aún no tienes ingresos registrados.");
        } else {
            double totalIngresos = ingresos.values().stream().mapToDouble(Double::doubleValue).sum();
            for (java.util.Map.Entry<String, Double> entry : ingresos.entrySet()) {
                double pct    = (totalIngresos > 0) ? (entry.getValue() / totalIngresos * 100) : 0;
                int    barLen = (int) (pct / 5);
                String barra  = "█".repeat(barLen) + "░".repeat(20 - barLen);
                System.out.printf("  %-22s S/ %8.2f  %5.1f%%  %s%n",
                        entry.getKey(), entry.getValue(), pct, barra);
            }
            mostrarMensaje("─".repeat(60));
            System.out.printf("  %-22s S/ %8.2f%n", "TOTAL INGRESADO", totalIngresos);
        }

        // ── Balance neto ─────────────────────────────────────────────────
        if (!gastos.isEmpty() || !ingresos.isEmpty()) {
            double totalG = gastos.values().stream().mapToDouble(Double::doubleValue).sum();
            double totalI = ingresos.values().stream().mapToDouble(Double::doubleValue).sum();
            double balance = totalI - totalG;
            mostrarMensaje("\n" + "═".repeat(60));
            System.out.printf("  %-22s S/ %8.2f%n",
                    balance >= 0 ? "✅ BALANCE NETO  +" : "⚠️  BALANCE NETO ", balance);
            mostrarMensaje("═".repeat(60));
        }
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
