# ChatFinance - Sistema de Gestión Financiera Personal

## 📋 Descripción
Aplicación de consola para gestión financiera personal desarrollada en Java con arquitectura MVC estricta, persistencia con SQLite y JDBC puro.

## 🏗️ Arquitectura
- **Patrón**: MVC (Modelo-Vista-Controlador)
- **Base de Datos**: SQLite (`finanzas.db`)
- **Persistencia**: JDBC Puro con PreparedStatements
- **Gestión de Dependencias**: Maven

## 📦 Estructura del Proyecto
```
ChatFinance/
├── pom.xml                          # Configuración Maven
├── src/
│   ├── Main.java                    # Punto de entrada
│   ├── controller/
│   │   └── LoginController.java    # Lógica de autenticación y menú
│   ├── dao/
│   │   └── UsuarioDAO.java         # Acceso a datos de Usuario
│   ├── modelo/
│   │   ├── Usuario.java            # Modelo de Usuario
│   │   ├── CuentaFinanciera.java
│   │   ├── Transaccion.java
│   │   └── ...
│   ├── util/
│   │   └── DatabaseConnection.java # Conexión Singleton a SQLite
│   └── view/
│       └── ConsoleView.java        # Interfaz de consola
└── finanzas.db                     # Base de datos SQLite (se crea automáticamente)
```

## 🚀 FASE 1: Implementación Actual

### Funcionalidades
1. **Autenticación sin contraseña**: Usa número de WhatsApp como llave única
2. **Registro automático**: Si el número no existe, se solicita nombre y se registra
3. **Persistencia**: Todos los datos se guardan en SQLite
4. **Menú principal**: Interfaz placeholder para futuras funcionalidades

### Flujo de Usuario
1. La aplicación solicita: "Ingrese su número de WhatsApp"
2. Dos casos posibles:
   - **Usuario Existente**: Muestra "Bienvenido nuevamente, [Nombre]"
   - **Usuario Nuevo**: Solicita nombre, registra y da bienvenida
3. Muestra menú principal:
   - Opción 1: Ver Saldo (Próximamente)
   - Opción 2: Salir

## 🛠️ Configuración y Ejecución

### Requisitos Previos
- **Java JDK**: 11 o superior
- **Maven**: 3.6 o superior

### Compilar el Proyecto
```bash
# Desde la raíz del proyecto
mvn clean compile
```

### Ejecutar la Aplicación
```bash
# Opción 1: Con Maven
mvn exec:java -Dexec.mainClass="Main"

# Opción 2: Compilar y ejecutar directamente
mvn clean compile
java -cp "target/classes:$HOME/.m2/repository/org/xerial/sqlite-jdbc/3.45.0.0/sqlite-jdbc-3.45.0.0.jar" Main
```

### Generar JAR Ejecutable
```bash
mvn clean package
java -jar target/ChatFinance-1.0-SNAPSHOT.jar
```

## 💾 Base de Datos

### Tabla `usuarios`
```sql
CREATE TABLE IF NOT EXISTS usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    numero_whatsapp TEXT NOT NULL UNIQUE,
    nombre TEXT NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
```

## 🔒 Seguridad
- Uso de **PreparedStatements** para prevenir SQL Injection
- **Try-with-resources** para gestión segura de conexiones
- Validación de entradas de usuario

## 📝 Código Destacado

### Singleton Pattern (DatabaseConnection)
```java
public static synchronized DatabaseConnection getInstance() {
    if (instance == null) {
        instance = new DatabaseConnection();
    }
    return instance;
}
```

### DAO con PreparedStatement
```java
public Usuario buscarPorWhatsapp(String numeroWhatsApp) {
    String sql = "SELECT id, numero_whatsapp, nombre FROM usuarios WHERE numero_whatsapp = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, numeroWhatsApp);
        // ... resto de la implementación
    }
}
```

## 🎯 Próximas Fases
- **Fase 2**: Gestión de Cuentas Financieras
- **Fase 3**: Transacciones (Pagos/Cobros)
- **Fase 4**: Notificaciones y Recordatorios
- **Fase 5**: Reportes y Estadísticas

## 👥 Autor
ChatFinance Team - 2026

## 📄 Licencia
Proyecto educativo - Libre uso
