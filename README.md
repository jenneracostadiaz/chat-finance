# ChatFinance — Sistema de Gestión Financiera Personal

Aplicación de consola desarrollada en Java con arquitectura MVC estricta,
persistencia SQLite mediante JDBC puro, y estructuras de datos concretas
(ArrayList, LinkedList, HashMap) aplicadas según el contexto de cada capa.

---

## Estado del proyecto

| Fase | Descripción | Estado |
|------|-------------|--------|
| 1 | Autenticación y estructura base | Completado |
| 2 | Gestión de cuentas (STI) | Completado |
| 3 | Motor de transacciones ACID | Completado |
| 4 | Categorías y reportes analíticos | Completado |
| — | Refactorización POO + Scope Trim | Completado |

---

## Arquitectura

```
src/
  Main.java
  controller/
    LoginController.java       — Autenticación y menú principal
    CuentaController.java      — CRUD de cuentas financieras
    OperacionesController.java — Transacciones + historial en sesión
  dao/
    CrudRepository.java        — Interfaz genérica <T, ID>
    UsuarioDAO.java
    CuentaDAO.java             — Implementa CrudRepository<CuentaFinanciera, Integer>
    TransaccionDAO.java        — Implementa CrudRepository<MovimientoRegistro, Integer>
  modelo/
    Usuario.java
    CuentaFinanciera.java      — Clase abstracta (polimorfismo)
    BilleteraDigital.java      — Subclase concreta
    CuentaBancaria.java        — Subclase concreta
    MovimientoRegistro.java    — Entidad de persistencia de transacciones
    INotificador.java          — Interfaz para sistema de notificaciones
    WhatsAppService.java       — Implementación de INotificador (mock consola)
  util/
    DatabaseConnection.java    — Singleton JDBC
  view/
    ConsoleView.java           — Toda la E/S de consola
```

**Patrón:** MVC estricto
**Base de datos:** SQLite (`finanzas.db`)
**Persistencia:** JDBC puro con `PreparedStatement`
**Gestión de dependencias:** Maven (`pom.xml`)

---

## Funcionalidades implementadas

### 1. Autenticación simple por WhatsApp

- El número de WhatsApp es la clave de acceso — sin contraseñas.
- Si el número existe en la BD: bienvenida de usuario recurrente.
- Si es nuevo: flujo de registro (nombre) y creación inmediata.

### 2. Gestión de cuentas — Single Table Inheritance

Una sola tabla `cuentas` almacena ambos tipos mediante la columna
discriminadora `tipo_cuenta` (`BILLETERA` | `BANCO`).

```
CuentaFinanciera  (abstracta)
  ├── BilleteraDigital   alias, proveedor  → tipo = BILLETERA
  └── CuentaBancaria     banco, cci        → tipo = BANCO
```

El método abstracto `obtenerDetalleImprimible()` es implementado
polimórficamente por cada subclase. El mapeo ORM manual se realiza
en `CuentaDAO.mapearFila()` con un `switch` sobre el discriminador.

### 3. Motor de transacciones ACID

Cada operación que toca múltiples tablas usa transacciones SQL explícitas:

```java
conn.setAutoCommit(false);
// 1. INSERT en transacciones
// 2. UPDATE saldo en cuentas
conn.commit();          // ambas operaciones o ninguna
// catch → conn.rollback()
```

| Operación | Tablas afectadas | SQL atómicos |
|-----------|-----------------|--------------|
| Ingreso | transacciones + cuentas | 2 |
| Gasto | transacciones + cuentas | 2 |
| Transferencia | transacciones + cuentas (x2) | 3 |

### 4. Categorías y reportes analíticos

Al registrar un ingreso o gasto, el usuario elige una categoría predefinida
que se persiste en la columna `categoria` de la tabla `transacciones`.

El reporte analítico usa `GROUP BY + SUM()` en SQL y devuelve un
`HashMap<String, Double>` (categoría → total). La vista calcula el
porcentaje de cada categoría respecto al total y lo muestra con una
barra de progreso ASCII.

### 5. Sistema de notificaciones (arquitectónico)

```java
public interface INotificador {
    void enviarMensaje(String destino, String texto);
}
```

`WhatsAppService implements INotificador` provee una implementación
que actualmente imprime en consola. La interfaz está diseñada para
ser sustituida por una integración real con la Meta Cloud API sin
modificar ninguna otra capa del sistema.

---

## Estructuras de datos aplicadas

| Colección | Dónde se usa | Justificación |
|-----------|-------------|---------------|
| `ArrayList` | `CuentaDAO`, `TransaccionDAO` — retorno de listas | Acceso aleatorio O(1), uso estándar de DAOs |
| `LinkedList` | `OperacionesController.historialSesion` | Cola FIFO de cap. 5: `addFirst()` + `removeLast()` ambos O(1) |
| `HashMap` | `TransaccionDAO.obtenerResumenGastos/Ingresos()` | Agrupación por clave (categoría) para reportes analíticos |

---

## Interfaz genérica CrudRepository

```java
public interface CrudRepository<T, ID> {
    T guardar(T entidad);
    T buscarPorId(ID id);
    List<T> listarTodos();
}
```

Implementada por `CuentaDAO` y `TransaccionDAO`, establece el contrato
estándar de persistencia y permite extender el sistema con nuevos
repositorios sin duplicar firmas.

---

## Base de datos

### Esquema

```sql
usuarios (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    numero_whatsapp  TEXT NOT NULL UNIQUE,
    nombre           TEXT NOT NULL,
    fecha_registro   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)

cuentas (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id     INTEGER NOT NULL REFERENCES usuarios(id),
    numero_cuenta  TEXT NOT NULL,
    saldo          REAL NOT NULL DEFAULT 0.0,
    tipo_cuenta    TEXT NOT NULL CHECK(tipo_cuenta IN ('BILLETERA','BANCO')),
    alias          TEXT,          -- BilleteraDigital
    proveedor      TEXT,          -- BilleteraDigital
    banco          TEXT,          -- CuentaBancaria
    cci            TEXT,          -- CuentaBancaria
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)

transacciones (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    cuenta_origen_id  INTEGER NOT NULL REFERENCES cuentas(id),
    cuenta_destino_id INTEGER REFERENCES cuentas(id),
    tipo              TEXT NOT NULL CHECK(tipo IN ('INGRESO','GASTO','TRANSFERENCIA')),
    monto             REAL NOT NULL CHECK(monto > 0),
    descripcion       TEXT,
    categoria         TEXT,
    fecha             TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
```

### Migraciones automáticas al arrancar

`DatabaseConnection.inicializarTablas()` aplica migraciones idempotentes:

- Elimina el constraint `UNIQUE(usuario_id, numero_cuenta)` de `cuentas`
  si existe (permite tener Yape y Plin con el mismo número).
- Añade la columna `categoria` a `transacciones` si no existe.

---

## Requisitos y ejecución

**Requisitos:** JDK 17+, Maven 3.6+

```bash
# Compilar
mvn compile

# Ejecutar
mvn exec:java -Dexec.mainClass="Main"

# O directamente
java -cp "target/classes:$(find ~/.m2/repository/org/xerial -name '*.jar' | head -1)" Main
```

El archivo `finanzas.db` se crea automáticamente en el directorio
de ejecución al primer arranque.

---

## Dependencias (`pom.xml`)

| Artefacto | Versión | Uso |
|-----------|---------|-----|
| `org.xerial:sqlite-jdbc` | 3.45.0.0 | Driver SQLite |
| `org.slf4j:slf4j-api` | 1.7.36 | API de logging (transitivo) |
| `org.slf4j:slf4j-nop` | 1.7.36 | Suprime warnings de SLF4J en consola |

---

## Decisiones de diseño relevantes

**¿Por qué Single Table Inheritance y no una tabla por subclase?**
Con solo dos subclases y un número pequeño de columnas específicas,
STI evita JOINs en cada consulta y simplifica el ORM manual.
Los campos nulos por fila son un intercambio aceptable.

**¿Por qué JDBC puro y no un ORM?**
El proyecto es educativo. JDBC puro fuerza a entender el ciclo
completo: SQL → `ResultSet` → objeto. Un ORM ocultaría ese aprendizaje.

**¿Por qué `LinkedList` para el historial en sesión?**
El historial actúa como una cola de capacidad fija: siempre se inserta
al frente (`addFirst`) y se descarta el elemento más antiguo desde el
final (`removeLast`). Ambas operaciones son O(1) en `LinkedList`,
mientras que en `ArrayList` `add(0, e)` es O(n).

**¿Por qué `HashMap` para reportes y no `LinkedHashMap`?**
El orden de iteración del reporte no es relevante para el cálculo
de totales y porcentajes. `HashMap` es la estructura semánticamente
correcta para una agrupación por clave sin orden implícito.
