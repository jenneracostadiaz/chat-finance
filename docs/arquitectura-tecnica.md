# 📐 Arquitectura Técnica - ChatFinance Fase 1

## 🎯 Objetivo
Implementar la estructura base de una aplicación de gestión financiera personal usando arquitectura MVC estricta, SQLite como base de datos y JDBC puro para la persistencia.

---

## 🏛️ Arquitectura MVC

### Capas del Sistema

#### 1. **Modelo (model)**
Representa los datos y la lógica de negocio.

**Clase Principal: `Usuario.java`**
```java
public class Usuario {
    private Integer id;
    private String numeroWhatsApp;  // Llave única de autenticación
    private String nombre;
    // ... constructores, getters, setters
}
```

**Responsabilidades:**
- Definir la estructura de datos
- Encapsular atributos con getters/setters
- Contener lógica de negocio básica (futuras validaciones)

---

#### 2. **Vista (view)**
Maneja toda la interacción con el usuario.

**Clase Principal: `ConsoleView.java`**
```java
public class ConsoleView {
    private Scanner scanner;
    
    public String solicitarNumeroWhatsApp() { ... }
    public String solicitarNombre() { ... }
    public void mostrarBienvenida(String nombre) { ... }
    public void mostrarMenuPrincipal() { ... }
}
```

**Responsabilidades:**
- Capturar inputs del usuario
- Mostrar mensajes en consola
- Formatear salidas (banners, menús)
- **NO** contiene lógica de negocio

**Principio:** La vista es "tonta", solo muestra y captura.

---

#### 3. **Controlador (controller)**
Orquesta la lógica entre Modelo y Vista.

**Clase Principal: `LoginController.java`**
```java
public class LoginController {
    private ConsoleView vista;
    private UsuarioDAO usuarioDAO;
    private Usuario usuarioActual;
    
    public void iniciar() {
        if (autenticarUsuario()) {
            mostrarMenuPrincipal();
        }
    }
}
```

**Responsabilidades:**
- Recibir inputs de la Vista
- Invocar métodos del DAO para persistencia
- Tomar decisiones de flujo (autenticar vs registrar)
- Actualizar el Modelo
- Indicar a la Vista qué mostrar

**Flujo de Control:**
```
Vista → Controlador → DAO → Base de Datos
                    ↓
                  Modelo
                    ↓
         Controlador → Vista
```

---

#### 4. **DAO (Data Access Object)**
Abstrae el acceso a la base de datos.

**Clase Principal: `UsuarioDAO.java`**
```java
public class UsuarioDAO {
    public Usuario buscarPorWhatsapp(String numero) { ... }
    public Usuario crearUsuario(Usuario usuario) { ... }
}
```

**Responsabilidades:**
- CRUD operations (Create, Read, Update, Delete)
- Ejecutar consultas SQL con PreparedStatements
- Convertir ResultSet → Modelo
- Manejar excepciones SQL

**Ventajas:**
- Separación total entre lógica de negocio y persistencia
- Facilita cambiar de base de datos en el futuro
- Mejora testabilidad

---

#### 5. **Utilidades (util)**
Servicios transversales reutilizables.

**Clase Principal: `DatabaseConnection.java`**
```java
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    
    public static synchronized DatabaseConnection getInstance() { ... }
    public Connection getConnection() { ... }
}
```

**Patrón Singleton:**
- Una única instancia de conexión en toda la aplicación
- Evita múltiples conexiones concurrentes
- Centraliza la configuración de la BD

**Inicialización Lazy:**
```java
if (instance == null) {
    instance = new DatabaseConnection();
}
```

---

## 🗄️ Persistencia con JDBC Puro

### ¿Por qué JDBC Puro?
1. **Control total** sobre consultas SQL
2. **Sin overhead** de frameworks ORM
3. **Aprendizaje** de fundamentos de persistencia
4. **Ligereza** para aplicaciones pequeñas

### Try-With-Resources
```java
try (Connection conn = DatabaseConnection.getInstance().getConnection();
     PreparedStatement pstmt = conn.prepareStatement(sql)) {
    // Código
} // Cierre automático de recursos
```

**Ventajas:**
- Cierre automático incluso si hay excepciones
- Previene fugas de memoria
- Código más limpio

### PreparedStatement vs Statement
```java
// ❌ MALO - Vulnerable a SQL Injection
String sql = "SELECT * FROM usuarios WHERE numero = '" + input + "'";
Statement stmt = conn.createStatement();

// ✅ BUENO - Seguro con PreparedStatement
String sql = "SELECT * FROM usuarios WHERE numero = ?";
PreparedStatement pstmt = conn.prepareStatement(sql);
pstmt.setString(1, input);
```

---

## 🔐 Seguridad

### 1. Prevención de SQL Injection
```java
// Parámetros escapados automáticamente
pstmt.setString(1, numeroWhatsApp);
```

### 2. Validación de Inputs
```java
if (numeroWhatsApp == null || numeroWhatsApp.isEmpty()) {
    vista.mostrarError("El número no puede estar vacío.");
    return false;
}
```

### 3. Manejo de Excepciones
```java
try {
    // Operación de base de datos
} catch (SQLException e) {
    System.err.println("✗ Error: " + e.getMessage());
    e.printStackTrace();
}
```

---

## 🔄 Flujo Completo de Autenticación

```
┌─────────────┐
│    Main     │ Inicializa DatabaseConnection y LoginController
└──────┬──────┘
       │
       v
┌─────────────────────┐
│ LoginController     │
│  .iniciar()         │
└──────┬──────────────┘
       │
       v
┌─────────────────────┐
│ ConsoleView         │
│  .solicitarWhatsApp()│ ← Usuario ingresa: "+56912345678"
└──────┬──────────────┘
       │
       v
┌─────────────────────┐
│ LoginController     │
│  .autenticarUsuario()│
└──────┬──────────────┘
       │
       v
┌─────────────────────┐
│ UsuarioDAO          │
│  .buscarPorWhatsapp()│ ← SELECT * FROM usuarios WHERE numero = ?
└──────┬──────────────┘
       │
       v
    ¿Usuario existe?
       │
       ├─ SÍ ──────────────┐
       │                   v
       │            ┌──────────────┐
       │            │ Retorna      │
       │            │ Usuario      │
       │            └──────┬───────┘
       │                   │
       │                   v
       │            ┌──────────────────┐
       │            │ ConsoleView      │
       │            │ .mostrarBienvenida│
       │            └──────────────────┘
       │
       └─ NO ─────────────┐
                          v
                   ┌──────────────┐
                   │ ConsoleView  │
                   │ .solicitarNombre│ ← Usuario ingresa: "Juan"
                   └──────┬───────┘
                          │
                          v
                   ┌──────────────┐
                   │ UsuarioDAO   │
                   │ .crearUsuario()│ ← INSERT INTO usuarios ...
                   └──────┬───────┘
                          │
                          v
                   ┌──────────────┐
                   │ ConsoleView  │
                   │ .mostrarBienvenida│
                   └──────────────┘
```

---

## 📊 Diagrama de Clases Simplificado

```
┌─────────────────┐
│     Main        │
└────────┬────────┘
         │
         │ inicializa
         v
┌─────────────────────────┐
│  LoginController        │
├─────────────────────────┤
│ - vista: ConsoleView    │
│ - usuarioDAO: UsuarioDAO│
│ - usuarioActual: Usuario│
├─────────────────────────┤
│ + iniciar()             │
│ - autenticarUsuario()   │
│ - registrarUsuario()    │
│ - mostrarMenuPrincipal()│
└──────┬──────────────┬───┘
       │              │
       │ usa          │ usa
       v              v
┌──────────────┐  ┌─────────────┐
│ ConsoleView  │  │ UsuarioDAO  │
├──────────────┤  ├─────────────┤
│ - scanner    │  │             │
├──────────────┤  ├─────────────┤
│ + solicitar..│  │ + buscar..  │
│ + mostrar..  │  │ + crear..   │
└──────────────┘  └──────┬──────┘
                         │
                         │ maneja
                         v
                  ┌─────────────┐
                  │   Usuario   │
                  ├─────────────┤
                  │ - id        │
                  │ - whatsapp  │
                  │ - nombre    │
                  └─────────────┘
```

---

## 🚀 Ventajas de Esta Arquitectura

### 1. Separación de Responsabilidades
Cada clase tiene un propósito único y claro.

### 2. Mantenibilidad
Cambios en la interfaz no afectan la lógica de negocio.

### 3. Escalabilidad
Fácil agregar nuevos controladores, DAOs o vistas.

### 4. Testabilidad
Cada capa puede ser testeada independientemente.

### 5. Reutilización
ConsoleView y DatabaseConnection pueden usarse en todo el proyecto.

---

## 📈 Próximas Extensiones

### Fase 2: Cuentas Financieras
- **Modelo**: `CuentaBancaria.java`, `BilleteraDigital.java`
- **DAO**: `CuentaDAO.java`
- **Controlador**: `CuentaController.java`
- **Vista**: Nuevos métodos en `ConsoleView.java`

### Fase 3: Transacciones
- **Modelo**: `Transaccion.java`, `Pago.java`, `Cobro.java`
- **DAO**: `TransaccionDAO.java`
- **Controlador**: `TransaccionController.java`

### Fase 4: Reportes
- **Servicio**: `ReportService.java`
- **Controlador**: `ReportController.java`

---

## 🎓 Conceptos Aplicados

- ✅ Patrón MVC
- ✅ Patrón DAO
- ✅ Patrón Singleton
- ✅ JDBC con PreparedStatements
- ✅ Try-with-resources
- ✅ Encapsulamiento
- ✅ Separación de Capas
- ✅ Gestión de Excepciones

---

**Versión:** 1.0 - Fase 1  
**Fecha:** Febrero 2026  
**Autor:** ChatFinance Team
