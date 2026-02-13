package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Clase Singleton para gestionar la conexión a la base de datos SQLite.
 * Garantiza que solo exista una instancia de conexión en toda la aplicación.
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:finanzas.db";

    /**
     * Constructor privado para implementar el patrón Singleton.
     * Establece la conexión con la base de datos.
     */
    private DatabaseConnection() {
        try {
            // Cargar el driver de SQLite
            Class.forName("org.sqlite.JDBC");
            // Establecer la conexión
            this.connection = DriverManager.getConnection(DB_URL);
            System.out.println("✓ Conexión a base de datos establecida correctamente.");
            // Inicializar las tablas
            inicializarTablas();
        } catch (ClassNotFoundException e) {
            System.err.println("✗ Error: Driver de SQLite no encontrado.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("✗ Error al conectar con la base de datos.");
            e.printStackTrace();
        }
    }

    /**
     * Obtiene la instancia única de DatabaseConnection (Singleton).
     * @return la instancia única de DatabaseConnection
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Obtiene la conexión a la base de datos.
     * @return Connection objeto de conexión JDBC
     */
    public Connection getConnection() {
        try {
            // Verificar si la conexión está cerrada y reabrirla si es necesario
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            System.err.println("✗ Error al obtener la conexión.");
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Crea las tablas necesarias si no existen.
     */
    private void inicializarTablas() {
        // Tabla de usuarios
        String sqlCrearTablaUsuarios = """
            CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                numero_whatsapp TEXT NOT NULL UNIQUE,
                nombre TEXT NOT NULL,
                fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        // Tabla de cuentas (Single Table Inheritance)
        // FASE 2: Gestión de Cuentas y Saldos
        // NOTA: Permite mismo número en diferentes proveedores (ej: Yape y Plin con mismo celular)
        String sqlCrearTablaCuentas = """
            CREATE TABLE IF NOT EXISTS cuentas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario_id INTEGER NOT NULL,
                numero_cuenta TEXT NOT NULL,
                saldo REAL NOT NULL DEFAULT 0.0,
                tipo_cuenta TEXT NOT NULL CHECK(tipo_cuenta IN ('BILLETERA', 'BANCO')),
                
                -- Campos específicos de BilleteraDigital
                alias TEXT,
                proveedor TEXT,
                
                -- Campos específicos de CuentaBancaria
                banco TEXT,
                cci TEXT,
                
                fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sqlCrearTablaUsuarios);

            // Verificar si necesitamos migrar la tabla cuentas
            if (necesitaMigracion()) {
                migrarTablaCuentas(stmt);
            } else {
                stmt.execute(sqlCrearTablaCuentas);
            }

            System.out.println("✓ Tablas de base de datos verificadas/creadas.");
        } catch (SQLException e) {
            System.err.println("✗ Error al inicializar las tablas de la base de datos.");
            e.printStackTrace();
        }
    }

    /**
     * Verifica si la tabla cuentas necesita migración (tiene el constraint antiguo).
     */
    private boolean necesitaMigracion() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT sql FROM sqlite_master WHERE type='table' AND name='cuentas'")) {

            if (rs.next()) {
                String tableSql = rs.getString("sql");
                // Si contiene el constraint antiguo, necesita migración
                return tableSql != null && tableSql.contains("UNIQUE(usuario_id, numero_cuenta)");
            }
        } catch (SQLException e) {
            // Si hay error, asumimos que no existe la tabla
            return false;
        }
        return false;
    }

    /**
     * Migra la tabla cuentas eliminando el constraint restrictivo.
     */
    private void migrarTablaCuentas(Statement stmt) throws SQLException {
        System.out.println("⚙️  Migrando tabla cuentas para permitir mismo número en diferentes proveedores...");

        // Paso 1: Crear tabla temporal con la nueva estructura
        String sqlTempTable = """
            CREATE TABLE cuentas_temp (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario_id INTEGER NOT NULL,
                numero_cuenta TEXT NOT NULL,
                saldo REAL NOT NULL DEFAULT 0.0,
                tipo_cuenta TEXT NOT NULL CHECK(tipo_cuenta IN ('BILLETERA', 'BANCO')),
                alias TEXT,
                proveedor TEXT,
                banco TEXT,
                cci TEXT,
                fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
            )
        """;
        stmt.execute(sqlTempTable);

        // Paso 2: Copiar datos existentes
        String sqlCopyData = """
            INSERT INTO cuentas_temp 
            SELECT id, usuario_id, numero_cuenta, saldo, tipo_cuenta, 
                   alias, proveedor, banco, cci, fecha_creacion
            FROM cuentas
        """;
        stmt.execute(sqlCopyData);

        // Paso 3: Eliminar tabla antigua
        stmt.execute("DROP TABLE cuentas");

        // Paso 4: Renombrar tabla temporal
        stmt.execute("ALTER TABLE cuentas_temp RENAME TO cuentas");

        System.out.println("✓ Migración completada. Ahora puedes tener Yape y Plin con el mismo número.");
    }

    /**
     * Cierra la conexión a la base de datos.
     */
    public void cerrarConexion() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Conexión a base de datos cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("✗ Error al cerrar la conexión.");
            e.printStackTrace();
        }
    }
}
