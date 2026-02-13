package util;

import java.sql.Connection;
import java.sql.DriverManager;
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
                
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
                UNIQUE(usuario_id, numero_cuenta)
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sqlCrearTablaUsuarios);
            stmt.execute(sqlCrearTablaCuentas);
            System.out.println("✓ Tablas de base de datos verificadas/creadas.");
        } catch (SQLException e) {
            System.err.println("✗ Error al inicializar las tablas de la base de datos.");
            e.printStackTrace();
        }
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
