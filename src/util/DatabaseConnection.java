package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton que gestiona la conexion JDBC a SQLite.
 * Una sola instancia de {@link Connection} es reutilizada durante toda la sesion.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:finanzas.db";

    private DatabaseConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(DB_URL);
            System.out.println("Conexion a base de datos establecida.");
            inicializarTablas();
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Driver de SQLite no encontrado.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos.");
            e.printStackTrace();
        }
    }

    /** Retorna la instancia unica (patron Singleton, thread-safe). */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) instance = new DatabaseConnection();
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener la conexion.");
            e.printStackTrace();
        }
        return connection;
    }

    private void inicializarTablas() {
        String sqlUsuarios =
            "CREATE TABLE IF NOT EXISTS usuarios (" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    numero_whatsapp TEXT NOT NULL UNIQUE," +
            "    nombre TEXT NOT NULL," +
            "    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")";

        String sqlCuentas =
            "CREATE TABLE IF NOT EXISTS cuentas (" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    usuario_id INTEGER NOT NULL," +
            "    numero_cuenta TEXT NOT NULL," +
            "    saldo REAL NOT NULL DEFAULT 0.0," +
            "    tipo_cuenta TEXT NOT NULL CHECK(tipo_cuenta IN ('BILLETERA', 'BANCO'))," +
            "    alias TEXT," +
            "    proveedor TEXT," +
            "    banco TEXT," +
            "    cci TEXT," +
            "    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)" +
            ")";

        String sqlTransacciones =
            "CREATE TABLE IF NOT EXISTS transacciones (" +
            "    id                 INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    cuenta_origen_id   INTEGER NOT NULL," +
            "    cuenta_destino_id  INTEGER," +
            "    tipo               TEXT NOT NULL CHECK(tipo IN ('INGRESO', 'GASTO', 'TRANSFERENCIA'))," +
            "    monto              REAL NOT NULL CHECK(monto > 0)," +
            "    descripcion        TEXT," +
            "    fecha              TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    FOREIGN KEY (cuenta_origen_id)  REFERENCES cuentas(id)," +
            "    FOREIGN KEY (cuenta_destino_id) REFERENCES cuentas(id)" +
            ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sqlUsuarios);

            if (necesitaMigracion()) {
                migrarTablaCuentas(stmt);
            } else {
                stmt.execute(sqlCuentas);
            }

            stmt.execute(sqlTransacciones);

            if (!columnaExiste("transacciones", "categoria")) {
                stmt.execute("ALTER TABLE transacciones ADD COLUMN categoria TEXT");
                System.out.println("Migracion aplicada: columna 'categoria' añadida.");
            }

            System.out.println("Tablas verificadas/creadas correctamente.");

        } catch (SQLException e) {
            System.err.println("Error al inicializar las tablas.");
            e.printStackTrace();
        }
    }

    private boolean necesitaMigracion() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT sql FROM sqlite_master WHERE type='table' AND name='cuentas'")) {
            if (rs.next()) {
                String sql = rs.getString("sql");
                return sql != null && sql.contains("UNIQUE(usuario_id, numero_cuenta)");
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }

    private void migrarTablaCuentas(Statement stmt) throws SQLException {
        System.out.println("Migrando tabla cuentas (eliminando constraint UNIQUE obsoleto)...");

        stmt.execute(
            "CREATE TABLE cuentas_temp (" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    usuario_id INTEGER NOT NULL," +
            "    numero_cuenta TEXT NOT NULL," +
            "    saldo REAL NOT NULL DEFAULT 0.0," +
            "    tipo_cuenta TEXT NOT NULL CHECK(tipo_cuenta IN ('BILLETERA', 'BANCO'))," +
            "    alias TEXT, proveedor TEXT, banco TEXT, cci TEXT," +
            "    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)" +
            ")"
        );
        stmt.execute(
            "INSERT INTO cuentas_temp " +
            "SELECT id, usuario_id, numero_cuenta, saldo, tipo_cuenta, " +
            "       alias, proveedor, banco, cci, fecha_creacion FROM cuentas"
        );
        stmt.execute("DROP TABLE cuentas");
        stmt.execute("ALTER TABLE cuentas_temp RENAME TO cuentas");
        System.out.println("Migracion de tabla cuentas completada.");
    }

    private boolean columnaExiste(String tabla, String columna) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tabla + ")")) {
            while (rs.next()) {
                if (columna.equalsIgnoreCase(rs.getString("name"))) return true;
            }
        } catch (SQLException e) {
            // tabla aun no existe
        }
        return false;
    }

    public void cerrarConexion() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Conexion a base de datos cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexion.");
            e.printStackTrace();
        }
    }
}
