# ChatFinance - Sistema de Gestión Financiera Personal 💰

## 📋 Descripción
Aplicación de consola para gestión financiera personal desarrollada en Java con arquitectura MVC estricta, persistencia con SQLite y JDBC puro. Implementa herencia, polimorfismo y el patrón Single Table Inheritance.

**🎯 Estado Actual: FASE 2 COMPLETADA**

## 🏗️ Arquitectura
- **Patrón**: MVC (Modelo-Vista-Controlador) Estricto
- **Base de Datos**: SQLite (`finanzas.db`)
- **Persistencia**: JDBC Puro con PreparedStatements (ORM Manual)
- **Gestión de Dependencias**: Maven
- **Conceptos POO**: Herencia, Polimorfismo, Abstracción, Encapsulación

## 📦 Estructura del Proyecto
```
ChatFinance/
├── pom.xml                          # Configuración Maven
├── database-schema.sql              # [NUEVO] Script SQL completo
├── FASE1-COMPLETADA.md              # Documentación Fase 1
├── FASE2-COMPLETADA.md              # [NUEVO] Documentación Fase 2
├── run.sh                           # Script de compilación/ejecución
├── src/
│   ├── Main.java                    # Punto de entrada
│   ├── controller/
│   │   ├── LoginController.java    # Autenticación y menú principal
│   │   └── CuentaController.java   # [NUEVO] Gestión de cuentas
│   ├── dao/
│   │   ├── UsuarioDAO.java         # Acceso a datos de Usuario
│   │   └── CuentaDAO.java          # [NUEVO] Acceso a datos de Cuentas
│   ├── modelo/
│   │   ├── Usuario.java            # Modelo de Usuario
│   │   ├── CuentaFinanciera.java   # [ACTUALIZADO] Clase abstracta
│   │   ├── BilleteraDigital.java   # [ACTUALIZADO] Hereda de CuentaFinanciera
│   │   ├── CuentaBancaria.java     # [ACTUALIZADO] Hereda de CuentaFinanciera
│   │   └── ...
│   ├── util/
│   │   └── DatabaseConnection.java # [ACTUALIZADO] Conexión + tabla cuentas
│   └── view/
│       └── ConsoleView.java        # [ACTUALIZADO] Interfaz de consola
└── finanzas.db                     # Base de datos SQLite (auto-generada)
```

## ✨ FASE 1: Autenticación Simple ✅

### Funcionalidades
1. **Autenticación sin contraseña**: Usa número de WhatsApp como llave única
2. **Registro automático**: Si el número no existe, se solicita nombre y se registra
3. **Persistencia**: Todos los datos se guardan en SQLite
4. **Menú principal**: Navegación entre funcionalidades

## 🆕 FASE 2: Gestión de Cuentas y Saldos ✅

### Funcionalidades Implementadas

#### 1. **Gestión de Cuentas con Herencia**
   - Jerarquía de clases: `CuentaFinanciera` → `BilleteraDigital` / `CuentaBancaria`
   - Polimorfismo en método `getDetalle()` (cada tipo muestra su información específica)
   - Single Table Inheritance para persistencia

#### 2. **Ver Mis Cuentas y Saldos** (Opción 1)
   - Lista todas las cuentas del usuario logueado
   - Muestra detalles específicos de cada tipo de cuenta:
     * 💳 **Billeteras Digitales**: Alias, Proveedor, Número
     * 🏦 **Cuentas Bancarias**: Banco, Número de cuenta, CCI
   - Calcula y muestra el **Patrimonio Total**

#### 3. **Crear Cuentas de Prueba** (Opción 99 - Oculta)
   - Modo desarrollador para generar datos de demostración
   - Crea 3 cuentas automáticamente:
     * Yape Personal (BCP) con S/ 50.00
     * Cuenta BCP con S/ 1,500.00
     * Plin Personal (Interbank) con S/ 120.50

### Ejemplo de Salida
```
═══════════════════════════════════════════════════
💰 MIS CUENTAS Y SALDOS
═══════════════════════════════════════════════════

1. 💳 Yape Personal | BCP | Número: 987654321
   💵 Saldo: S/ 50.00

2. 🏦 Banco BCP | Cuenta: 19312345678 | CCI: 00219300...
   💵 Saldo: S/ 1500.00

3. 💳 Plin Personal | Interbank | Número: 987123456
   💵 Saldo: S/ 120.50

────────────────────────────────────────────────────
🏆 PATRIMONIO TOTAL: S/ 1670.50
═══════════════════════════════════════════════════
```

### Flujo de Usuario (FASE 2)
1. La aplicación solicita: "Ingrese su número de WhatsApp"
2. Dos casos posibles:
   - **Usuario Existente**: Muestra "Bienvenido nuevamente, [Nombre]"
   - **Usuario Nuevo**: Solicita nombre, registra y da bienvenida
3. Muestra menú principal:
   - **Opción 1**: Ver Mis Cuentas y Saldos ✅ (NUEVO)
   - **Opción 2**: Agregar Cuenta (Próximamente)
   - **Opción 3**: Salir
   - **Opción 99**: Crear cuentas de prueba (oculta) 🔧

## 🛠️ Configuración y Ejecución

### Requisitos Previos
- **Java JDK**: 17 o superior (requerido para text blocks)
- **Maven**: 3.6 o superior (opcional, también puedes usar `run.sh`)

### ⚡ Inicio Rápido

#### Opción 1: Con Maven (Recomendado)
```bash
# Compilar y ejecutar en un solo comando
mvn clean compile exec:java -Dexec.mainClass="Main"
```

#### Opción 2: Con Script run.sh
```bash
# Dar permisos de ejecución (solo la primera vez)
chmod +x run.sh

# Compilar y ejecutar
./run.sh
```

### 🧪 Guía de Prueba Rápida

1. **Ejecutar la aplicación** (usa cualquiera de las opciones anteriores)

2. **Registrarse como nuevo usuario**:
   ```
   Ingrese número de WhatsApp: 987654321
   Ingrese su nombre: Juan Pérez
   ```

3. **Crear cuentas de prueba**:
   ```
   Seleccione opción: 99
   ```
   Esto creará 3 cuentas automáticamente

4. **Ver tus cuentas y saldos**:
   ```
   Seleccione opción: 1
   ```
   Verás el listado de cuentas y el patrimonio total

5. **Salir**:
   ```
   Seleccione opción: 3
   ```

### Compilar el Proyecto (Solo compilación)
```bash
mvn clean compile
```

### Ejecutar Tests
```bash
mvn test
```

### Generar JAR Ejecutable (Futuro)
```bash
mvn clean package
java -jar target/ChatFinance-1.0-SNAPSHOT.jar
```
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
