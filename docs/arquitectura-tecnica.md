# ChatFinance — Documentación Técnica Oficial

**Proyecto:** ChatFinance — Sistema de Gestión Financiera Personal  
**Lenguaje:** Java 17  
**Persistencia:** SQLite · JDBC puro · Transacciones ACID  
**Arquitectura:** MVC estricto (Modelo – Vista – Controlador)  
**IA:** LangChain4j 0.36.2 · Ollama · llama3.2 (local)  
**Fecha:** Marzo 2026

---

## 1. Resumen Ejecutivo

ChatFinance es una aplicación de consola en Java 17 que permite a un usuario gestionar sus finanzas personales —billeteras digitales como Yape o Plin, y cuentas bancarias tradicionales— identificándose únicamente con su número de WhatsApp, sin contraseñas. El sistema registra ingresos, gastos y transferencias de forma atómica sobre SQLite usando JDBC puro con `setAutoCommit(false)` y `rollback` explícito; clasifica cada movimiento por categoría y genera reportes analíticos con distribución porcentual del gasto. Toda la lógica está organizada bajo el patrón MVC con paquetes `modelo`, `view`, `controller`, `dao`, `util` y `service`, con una capa de persistencia basada en el patrón Single Table Inheritance. En la Fase 5, el sistema integra un **Router de Intenciones con Inteligencia Artificial** implementado sobre LangChain4j y el modelo de lenguaje llama3.2 ejecutado localmente con Ollama: el usuario escribe en lenguaje natural y la IA clasifica la intención (`REGISTRAR_TRANSACCION`, `CREAR_CUENTA`, `VER_REPORTE`, `VER_SALDOS`) extrayendo los parámetros necesarios, que el controlador persiste automáticamente usando los DAOs existentes.

---

## 2. Defensa Técnica: POO y Estructuras de Datos

---

### 2.1 Interfaces — Contratos de comportamiento y principio Open/Closed

#### Dónde se aplicó

| Interfaz | Paquete | Implementaciones concretas |
|---|---|---|
| `INotificador` | `modelo` | `WhatsAppService` |
| `CrudRepository<T, ID>` | `dao` | `CuentaDAO`, `TransaccionDAO` |

#### Cómo se implementó

`INotificador` declara el contrato `enviarMensaje(String destino, String texto)`. La clase `WhatsAppService` lo implementa con la lógica actual de consola, simulando el envío mientras se conecta la API real. El controlador de autenticación depende de `INotificador`, no de `WhatsAppService` directamente.

`CrudRepository<T, ID>` declara tres firmas genéricas de persistencia: `guardar(T)`, `buscarPorId(ID)` y `listarTodos()`. Tanto `CuentaDAO` como `TransaccionDAO` la implementan, fijando sus propios tipos concretos. Esta interfaz se desarrolla en detalle en la sección de Genéricos.

#### Por qué se tomó esta decisión

Una interfaz establece un **contrato sin revelar implementación**: desacopla al consumidor del proveedor. Si mañana se integra la Meta Cloud API de WhatsApp, solo cambia `WhatsAppService`; ningún controlador que dependa de `INotificador` requiere modificación. Este es el **Principio Open/Closed**: el sistema está abierto a extensión y cerrado a modificación. La misma lógica aplica a `CrudRepository`: cualquier nuevo DAO puede unirse al ecosistema implementando el contrato sin alterar el código existente.

---

### 2.2 Clases Genéricas — `CrudRepository<T, ID>` y el principio DRY

#### Dónde se aplicó

```
dao/CrudRepository<T, ID>        ← interfaz genérica
        ↑                   ↑
   CuentaDAO           TransaccionDAO
   <CuentaFinanciera,  <MovimientoRegistro,
    Integer>            Integer>
```

#### Cómo se implementó

La interfaz `CrudRepository` en `dao/CrudRepository.java` declara tres operaciones parametrizadas. El tipo `T` representa la entidad a gestionar; `ID`, el tipo de su clave primaria. `CuentaDAO` implementa `CrudRepository<CuentaFinanciera, Integer>`: en su clase, `T` queda fijado a `CuentaFinanciera` e `ID` a `Integer`. `TransaccionDAO` hace lo mismo con `MovimientoRegistro`. Cada implementación aporta únicamente la lógica de mapeo específica en su método privado `mapearFila(ResultSet rs)`.

#### Por qué se tomó esta decisión

Sin genéricos, las firmas `guardar`, `buscarPorId` y `listarTodos` se repetirían con tipos distintos en cada DAO, violando el principio **DRY** (Don't Repeat Yourself). Con `CrudRepository<T, ID>`, el contrato se escribe una sola vez y el compilador garantiza en tiempo de compilación que cada implementación respeta sus tipos. Un futuro `UsuarioDAO` solo necesita declarar `implements CrudRepository<Usuario, Integer>`: las firmas ya existen, solo hay que rellenar la lógica concreta.

---

### 2.3 Clases Abstractas, Herencia y Polimorfismo — Dominio de cuentas y Single Table Inheritance

#### Dónde se aplicó

```
modelo/
  CuentaFinanciera  (abstract)
    ├── BilleteraDigital   → alias, proveedor
    └── CuentaBancaria     → banco, cci
```

#### Cómo se implementó

`CuentaFinanciera` en `modelo/CuentaFinanciera.java` es una clase `abstract` que encapsula los atributos comunes a toda cuenta (`id`, `usuarioId`, `numeroCuenta`, `saldo`) y declara tres métodos abstractos que cada subclase debe implementar obligatoriamente:

| Método abstracto | Rol |
|---|---|
| `obtenerDetalleImprimible()` | Representación textual legible, específica al tipo |
| `getTipoCuenta()` | Discriminador `"BILLETERA"` / `"BANCO"` para la base de datos |
| `validarCuenta()` | Reglas de validación propias de cada tipo |

`BilleteraDigital` implementa `obtenerDetalleImprimible()` retornando `"Billetera Yape | BCP | N. 987..."`. `CuentaBancaria` retorna `"Banco BCP | Cuenta: 193... | CCI: 002..."`. El polimorfismo se materializa en `CuentaDAO.mapearFila()`: lee la columna discriminadora `tipo_cuenta` y con un `switch` instancia la subclase correcta, retornando siempre una referencia `CuentaFinanciera`. La vista (`ConsoleView`) y los controladores trabajan con `List<CuentaFinanciera>` sin conocer qué tipo concreto contienen.

Este mismo patrón alimenta la estrategia **Single Table Inheritance** en SQLite: una única tabla `cuentas` almacena billeteras y cuentas bancarias, diferenciadas por la columna `tipo_cuenta`. El mapeo objeto-relacional es completamente manual, sin frameworks, demostrando comprensión real del mecanismo.

#### Por qué se tomó esta decisión

La clase abstracta convierte al compilador en un guardián de contratos de dominio: **es imposible crear una subclase que compile sin implementar los tres métodos abstractos**. Si se añade `CriptoWallet`, el error de compilación indica exactamente qué contratos faltan, previniendo errores en tiempo de ejecución. La herencia también elimina la duplicación de los cuatro atributos comunes, y el polimorfismo permite que la vista y los controladores operen sobre cualquier tipo de cuenta sin condiciones explícitas, manteniendo el código abierto a extensión.

---

### 2.4 Colecciones — `ArrayList`: retorno estándar de resultados desde los DAOs

#### Dónde se aplicó

| Clase | Método |
|---|---|
| `CuentaDAO` | `listarTodos()`, `listarPorUsuario(int)` |
| `TransaccionDAO` | `listarTodos()`, `listarUltimosMovimientos(int, int)` |

#### Cómo se implementó

Cada método de consulta declara `List<T> lista = new ArrayList<>()`, itera el `ResultSet` con `lista.add(mapearFila(rs))` y retorna la lista completa. El tipo de retorno declarado es la interfaz `List<T>`, no la implementación `ArrayList`, respetando el principio de programación hacia interfaces.

#### Por qué se tomó esta decisión

`ArrayList` almacena sus elementos en un arreglo interno con acceso directo por índice en **O(1)**. Los patrones de uso de la capa vista son exactamente dos: iteración secuencial `for-each` para mostrar todas las cuentas, y acceso por índice para recuperar la cuenta elegida por el usuario. La inserción secuencial desde el `ResultSet` es O(1) amortizado. No existe en este flujo ninguna necesidad de inserción o eliminación en posiciones arbitrarias, por lo que `ArrayList` es la elección técnicamente correcta y predecible para el rol de contenedor de resultados de consulta.

---

### 2.5 Colecciones — `LinkedList`: historial de sesión con eficiencia O(1) en extremos

#### Dónde se aplicó

`OperacionesController` — campo `historialSesion` declarado en la línea 22 como `LinkedList<MovimientoRegistro>`.

| Operación | Método | Descripción |
|---|---|---|
| Insertar al frente | `historialSesion.addFirst(movimiento)` | Nuevo movimiento como más reciente |
| Eliminar por el final | `historialSesion.removeLast()` | Descarta el más antiguo al superar capacidad 5 |

#### Cómo se implementó

El método privado `agregarAlHistorial(MovimientoRegistro)` se invoca tras cada transacción exitosa. Inserta el nuevo movimiento al frente con `addFirst()` y, si el tamaño supera `CAPACIDAD_HISTORIAL = 5`, elimina el último con `removeLast()`. Al mostrar "Ver Últimos Movimientos", el controlador prioriza este historial en memoria antes de consultar la base de datos.

#### Por qué se tomó esta decisión

Este diseño implementa una **cola de capacidad fija** (bounded queue). El argumento es de eficiencia computacional:

| Operación | `ArrayList` | `LinkedList` |
|---|---|---|
| Insertar al frente | **O(n)** — desplaza todos los elementos | **O(1)** — reasigna puntero de cabeza |
| Eliminar por el final | O(1) | **O(1)** — reasigna puntero de cola |

Con `ArrayList`, cada `add(0, elemento)` obliga internamente a desplazar todos los elementos una posición. Con `LinkedList`, la misma operación es una reasignación de puntero. El uso de `LinkedList` refleja una **decisión de diseño consciente**: se elige la estructura cuya semántica se ajusta al problema — inserción y eliminación eficiente en los extremos, sin acceso aleatorio por índice.

---

### 2.6 Colecciones — `HashMap`: motor de reportes analíticos por categoría

#### Dónde se aplicó

| Clase | Método | Tipo de retorno |
|---|---|---|
| `TransaccionDAO` | `obtenerResumenGastos(int usuarioId)` | `Map<String, Double>` |
| `TransaccionDAO` | `obtenerResumenIngresos(int usuarioId)` | `Map<String, Double>` |

Ambos métodos instancian internamente `new HashMap<>()`.

#### Cómo se implementó

Ambos métodos ejecutan una consulta SQL con `GROUP BY categoria` y `SUM(monto)` sobre las transacciones del usuario. A medida que se itera el `ResultSet`, cada par `(categoria → total)` se inserta en el mapa con `resumen.put(rs.getString("categoria"), rs.getDouble("total"))`. El mapa retornado viaja al `OperacionesController` y de ahí a `ConsoleView.mostrarReporteAnalitico()`, donde se itera con `Map.Entry<String, Double>` para calcular porcentajes y renderizar barras de progreso ASCII.

#### Por qué se tomó esta decisión

El problema es de naturaleza **asociativa**: para cada categoría de texto se necesita recuperar su total acumulado en tiempo constante. `HashMap` ofrece acceso, inserción y búsqueda en **O(1) promedio** gracias a su función hash. Las alternativas habrían sido:

- **Arreglos paralelos** de nombres y montos: frágil, sin relación semántica explícita.
- **`List` de pares**: búsqueda lineal O(n) para localizar una categoría.
- **`LinkedHashMap`**: preservaría orden de inserción, pero el SQL ya provee el orden con `ORDER BY total DESC`, haciendo ese overhead innecesario.

`HashMap` es la estructura canónica para agrupación por clave cuando el orden no es requisito del cliente: semánticamente precisa, sin overhead innecesario.

---

## 3. Guion para Diapositivas — Pitch de Defensa

---

### Diapositiva 1 — ¿Qué es ChatFinance?

- Sistema de gestión financiera personal en **Java 17** con **SQLite** como base de datos embebida
- Autenticación sin contraseña: el **número de WhatsApp** es la llave de identidad del usuario
- Operaciones financieras completas: registrar ingresos, gastos y transferencias entre cuentas propias con **garantía ACID** (`commit` / `rollback`)
- **Fase 5 — IA integrada**: el usuario escribe en lenguaje natural y el sistema registra la operación automáticamente usando **LangChain4j + Ollama + llama3.2** ejecutado localmente
- Arquitectura **MVC estricta** · JDBC puro · sin Hibernate ni JPA · sin frameworks web

---

### Diapositiva 2 — Interfaces y Clases Genéricas

**Interfaces como contratos desacoplados**

- `INotificador` → contrato `enviarMensaje()`: `WhatsAppService` lo implementa hoy en consola, mañana con la Meta Cloud API — **cero cambios** en ningún controlador
- Principio **Open/Closed**: el sistema está abierto a extensión y cerrado a modificación; se añade una nueva implementación sin tocar el código existente

**Genéricos para eliminar duplicación (DRY)**

- `CrudRepository<T, ID>` declara `guardar`, `buscarPorId` y `listarTodos` **exactamente una vez**
- `CuentaDAO` lo instancia como `<CuentaFinanciera, Integer>`; `TransaccionDAO` como `<MovimientoRegistro, Integer>`
- El compilador verifica los tipos en **tiempo de compilación**, no en ejecución — los errores se detectan antes de ejecutar una sola línea

---

### Diapositiva 3 — Herencia Abstracta, Polimorfismo y Single Table Inheritance

**Jerarquía del dominio**

```
CuentaFinanciera (abstract)
  ├── BilleteraDigital  →  alias, proveedor
  └── CuentaBancaria    →  banco, cci
```

- Tres métodos abstractos **garantizan por compilador** que toda subclase defina su propio comportamiento: ninguna cuenta puede existir sin implementar `obtenerDetalleImprimible()`, `getTipoCuenta()` y `validarCuenta()`
- La vista y los controladores trabajan con `List<CuentaFinanciera>` — **no saben ni necesitan saber** qué tipo concreto tienen
- `CuentaDAO.mapearFila()` aplica **Single Table Inheritance manual**: lee el discriminador `tipo_cuenta` y construye la subclase correcta sin ningún framework ORM
- Añadir `CriptoWallet` requiere únicamente crear la subclase — la vista, el DAO y todos los controladores existentes **no se modifican**

---

### Diapositiva 4 — Estructuras de Datos: la elección correcta importa

| Estructura | Dónde se usa | Operación crítica | Complejidad |
|---|---|---|---|
| `ArrayList` | DAOs — retorno de consultas | Iteración secuencial + acceso por índice | **O(1)** lectura |
| `LinkedList` | `OperacionesController` — historial de sesión | `addFirst()` + `removeLast()` · cola FIFO cap. 5 | **O(1)** en ambos extremos |
| `HashMap` | `TransaccionDAO` — motor de reportes | Agrupación `categoría → total acumulado` | **O(1)** promedio por clave |

- **`ArrayList` en DAOs**: el acceso por índice es O(1) y la vista itera una sola vez de forma secuencial — es exactamente para esto para lo que fue diseñado
- **`LinkedList` en historial**: insertar al frente con `ArrayList` es **O(n)** porque desplaza todos los elementos; con `LinkedList` es **O(1)** porque solo reasigna un puntero
- **`HashMap` en reportes**: localizar una categoría en una lista es O(n); en un mapa hash es **O(1)** — crítico cuando el reporte consolida múltiples categorías

---

### Diapositiva 5 — Integración con IA y Conclusión Arquitectónica

**Router de Intenciones con IA (Fase 5)**

- `AsistenteIAService` conecta con **Ollama** (servidor local) usando **LangChain4j** y el modelo `llama3.2`
- El **System Prompt** actúa como clasificador analítico: instruye al modelo a identificar la intención en una de 4 categorías y extraer los parámetros relevantes
- `IntencionOperacionDTO` es el contrato entre la IA y el sistema: campos `intencion`, `tipoTransaccion`, `monto`, `categoria`, `nombreCuenta`, `tipoCuentaNueva`
- `AsistenteController` implementa el **Router** con un `switch` sobre la intención: despacha a `registrarTransaccion()`, `crearCuenta()`, `verReporte()` o `verSaldos()` reutilizando los DAOs y controladores existentes — **sin duplicar lógica**

**Decisiones que escalan**

- `INotificador` es el punto de extensión listo para la Meta Cloud API sin tocar ningún controlador
- `CrudRepository<T, ID>` garantiza que todo DAO futuro siga el mismo contrato CRUD
- La clase abstracta `CuentaFinanciera` hace imposible, por diseño del compilador, tener una subclase incompleta
- Cada decisión técnica — qué colección, qué patrón, qué estructura — tiene una **justificación medible de rendimiento, mantenimiento o escalabilidad**, no de conveniencia
