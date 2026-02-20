# ChatFinance — Documentación Técnica de Arquitectura y Patrones de Diseño

**Proyecto:** ChatFinance — Sistema de Gestión Financiera Personal  
**Lenguaje:** Java 17  
**Persistencia:** SQLite · JDBC puro  
**Arquitectura:** MVC (Modelo – Vista – Controlador)  
**Fecha:** Febrero 2026

---

## 1. Resumen Ejecutivo

ChatFinance es una aplicación de consola que permite a un usuario gestionar
sus cuentas financieras personales —billeteras digitales como Yape o Plin,
y cuentas bancarias tradicionales— identificándose únicamente con su número
de WhatsApp. El sistema registra ingresos, gastos y transferencias de forma
segura y atómica en una base de datos SQLite, clasifica cada movimiento por
categoría y genera reportes analíticos que muestran en qué rubros se
distribuye el dinero del usuario. Toda la lógica está organizada bajo el
patrón MVC, con una capa de persistencia basada en JDBC puro y estructuras
de datos seleccionadas deliberadamente según las necesidades de rendimiento
de cada operación.

---

## 2. Sustentación de Conceptos POO y Estructuras de Datos

---

### 2.1 Interfaces — Contratos de comportamiento

#### Dónde se aplicó

| Interfaz | Paquete | Implementaciones |
|----------|---------|-----------------|
| `INotificador` | `modelo` | `WhatsAppService` |
| `CrudRepository<T, ID>` | `dao` | `CuentaDAO`, `TransaccionDAO` |

#### Cómo se implementó

`INotificador` declara un único método `enviarMensaje(String destino, String texto)`.
`WhatsAppService` proporciona la implementación concreta que actualmente opera
sobre consola, pero que puede ser sustituida por una llamada real a la
Meta Cloud API sin modificar ninguna otra clase del sistema.

`CrudRepository<T, ID>` es la segunda interfaz del proyecto y se describe
en detalle en la sección de Genéricos (§2.2), ya que ambos conceptos
están entrelazados por diseño.

#### Por qué se tomó esta decisión

Una interfaz establece un **contrato**: define qué puede hacer un componente
sin revelar cómo lo hace. Esto desacopla al consumidor del proveedor.
Si mañana se integra la API real de WhatsApp, solo cambia la clase
`WhatsAppService`; los controladores que dependan de `INotificador` no
requieren ninguna modificación. Este principio se conoce como
**Abierto/Cerrado** (Open/Closed Principle): el sistema está abierto a
extensión, cerrado a modificación.

---

### 2.2 Clases Genéricas — Eliminación de código duplicado

#### Dónde se aplicó

`CrudRepository<T, ID>` — archivo `dao/CrudRepository.java`

```
dao/
  CrudRepository<T, ID>          ← interfaz genérica
        ↑                   ↑
   CuentaDAO           TransaccionDAO
   implements          implements
   CrudRepository      CrudRepository
   <CuentaFinanciera,  <MovimientoRegistro,
    Integer>            Integer>
```

#### Cómo se implementó

La interfaz declara tres firmas parametrizadas:

- `T guardar(T entidad)` — persiste una entidad y retorna la instancia con su ID asignado.
- `T buscarPorId(ID id)` — recupera una entidad por su clave primaria.
- `List<T> listarTodos()` — devuelve todas las entidades del repositorio.

`CuentaDAO` implementa `CrudRepository<CuentaFinanciera, Integer>`: los
parámetros de tipo `T` e `ID` quedan fijados en `CuentaFinanciera` e `Integer`
respectivamente. `TransaccionDAO` hace lo mismo con `MovimientoRegistro` e
`Integer`. Cada clase proporciona su lógica de mapeo específica en el método
privado `mapearFila(ResultSet rs)`.

#### Por qué se tomó esta decisión

Sin genéricos, las tres firmas CRUD deberían repetirse en cada DAO con
tipos distintos, generando código duplicado difícil de mantener.
Con `CrudRepository<T, ID>`, el contrato se escribe **una sola vez**
y el compilador verifica en tiempo de compilación que cada implementación
respeta el contrato para sus tipos concretos. Añadir un futuro
`UsuarioDAO` al mismo contrato requiere únicamente declarar
`implements CrudRepository<Usuario, Integer>`: la firma ya existe,
solo hay que implementarla.

---

### 2.3 Clases Abstractas y Herencia — Modelo de dominio polimórfico

#### Dónde se aplicó

```
modelo/
  CuentaFinanciera  (abstract)
    ├── BilleteraDigital   extends CuentaFinanciera
    └── CuentaBancaria     extends CuentaFinanciera
```

#### Cómo se implementó

`CuentaFinanciera` es una clase `abstract` que concentra los atributos
comunes a cualquier cuenta (`id`, `usuarioId`, `numeroCuenta`, `saldo`)
y declara tres métodos abstractos que cada subclase debe implementar
obligatoriamente:

| Método abstracto | Propósito |
|-----------------|-----------|
| `obtenerDetalleImprimible()` | Descripción legible específica al tipo |
| `getTipoCuenta()` | Discriminador `BILLETERA` / `BANCO` para la BD |
| `validarCuenta()` | Reglas de validación propias de cada tipo |

`BilleteraDigital` agrega los atributos `alias` y `proveedor`, e implementa
`obtenerDetalleImprimible()` retornando, por ejemplo,
`"Billetera Yape | BCP | N. 987654321"`.
`CuentaBancaria` agrega `banco` y `cci`, e implementa el mismo método
retornando `"Banco BCP | Cuenta: 193... | CCI: 00219..."`.

El polimorfismo se materializa en `CuentaDAO.mapearFila()`: el método
recibe un `ResultSet`, lee la columna discriminadora `tipo_cuenta` y,
mediante un `switch`, instancia `BilleteraDigital` o `CuentaBancaria`.
Quien llama al método recibe una referencia del tipo padre
`CuentaFinanciera` y puede invocar `obtenerDetalleImprimible()` sin
saber qué subclase concreta tiene en sus manos.

Lo mismo ocurre en `ConsoleView.seleccionarCuentaDeLista()` y en
`CuentaController.verSaldos()`: ambos trabajan con
`List<CuentaFinanciera>` y llaman a `obtenerDetalleImprimible()` de
forma polimórfica.

#### Por qué se tomó esta decisión

La herencia sobre una clase abstracta garantiza que **ninguna subclase
futura pueda compilar** si no implementa los tres métodos abstractos.
Esto convierte al compilador en un guardián de contratos de dominio.
Si se añade un tercer tipo de cuenta (por ejemplo, `CriptoWallet`), el
desarrollador recibe un error de compilación que le indica exactamente
qué debe implementar, eliminando errores de comportamiento en tiempo
de ejecución. La clase abstracta también evita instanciar directamente
el tipo genérico `CuentaFinanciera`, lo que no tendría sentido de negocio.

---

### 2.4 Colecciones — `ArrayList`: retorno estándar de listas desde la BD

#### Dónde se aplicó

| Clase | Método | Línea |
|-------|--------|-------|
| `CuentaDAO` | `listarTodos()` | 99 |
| `CuentaDAO` | `listarPorUsuario(int)` | 120 |
| `TransaccionDAO` | `listarTodos()` | 76 |
| `TransaccionDAO` | `listarUltimosMovimientos(...)` | 243 |

#### Cómo se implementó

Cada método de consulta del DAO declara una variable local
`List<T> lista = new ArrayList<>()`, itera el `ResultSet` con
`lista.add(mapearFila(rs))` y devuelve la lista completa al
controlador. El tipo de retorno declarado es la interfaz `List<T>`,
no la implementación concreta `ArrayList<T>`, respetando el
principio de programación hacia interfaces.

#### Por qué se tomó esta decisión

`ArrayList` almacena sus elementos en un arreglo interno de acceso
directo. Su complejidad de acceso por índice es **O(1)**, lo que lo
hace óptimo para los patrones de uso de esta capa: la vista itera la
lista una vez (`for-each`) o accede a un índice específico elegido por
el usuario. La inserción secuencial desde el `ResultSet` es amortizada
O(1). No se requieren inserciones ni eliminaciones en posiciones
arbitrarias, por lo que `ArrayList` es la elección correcta y
predecible para el rol de "contenedor de resultados de consulta".

---

### 2.5 Colecciones — `LinkedList`: historial de sesión con eficiencia O(1) en extremos

#### Dónde se aplicó

`OperacionesController` — campo `historialSesion`

| Línea | Instrucción | Rol |
|-------|-------------|-----|
| 22 | `private final LinkedList<MovimientoRegistro> historialSesion` | Declaración del campo |
| 28 | `this.historialSesion = new LinkedList<>()` | Inicialización |
| 199 | `historialSesion.addFirst(movimiento)` | Inserción al frente (más reciente primero) |
| 201 | `historialSesion.removeLast()` | Descarte del movimiento más antiguo |

#### Cómo se implementó

Cada vez que el usuario registra exitosamente un ingreso, un gasto
o una transferencia, el controlador invoca el método privado
`agregarAlHistorial(MovimientoRegistro movimiento)`. Este método:

1. Inserta el nuevo movimiento al **frente** de la lista con `addFirst()`.
2. Si el tamaño supera `CAPACIDAD_HISTORIAL` (5 elementos), elimina el
   elemento más antiguo desde el **final** con `removeLast()`.

Cuando el usuario selecciona "Ver Últimos Movimientos", si
`historialSesion` no está vacío se muestra directamente desde memoria
sin tocar la base de datos; de lo contrario, se realiza una consulta
como respaldo.

#### Por qué se tomó esta decisión

Este patrón implementa una **cola de capacidad fija** (bounded FIFO queue).
La eficiencia es el argumento central:

| Operación | `ArrayList` | `LinkedList` |
|-----------|-------------|--------------|
| Insertar al frente (`add(0, e)`) | **O(n)** — desplaza todos los elementos | **O(1)** — reasigna puntero de cabeza |
| Eliminar por el final | O(1) | **O(1)** — reasigna puntero de cola |

Con `ArrayList`, cada llamada a `add(0, movimiento)` obliga a Java a
desplazar internamente todos los elementos una posición hacia la derecha.
Con una lista de 5 elementos el impacto es mínimo, pero el uso de
`LinkedList` refleja una **decisión de diseño consciente**: se elige la
estructura cuya semántica se ajusta exactamente al problema — inserción
y eliminación en los extremos, sin acceso aleatorio por índice.

---

### 2.6 Colecciones — `HashMap`: agrupación clave-valor para reportes analíticos

#### Dónde se aplicó

| Clase | Método | Tipo de retorno |
|-------|--------|----------------|
| `TransaccionDAO` | `obtenerResumenGastos(int usuarioId)` | `Map<String, Double>` |
| `TransaccionDAO` | `obtenerResumenIngresos(int usuarioId)` | `Map<String, Double>` |

Ambos métodos instancian internamente `new HashMap<>()` (líneas 272 y 300).

#### Cómo se implementó

Ambos métodos ejecutan una consulta SQL con `GROUP BY categoria` y
`SUM(monto)`. A medida que se itera el `ResultSet`, cada par
`(categoria, total)` se inserta en el `HashMap` mediante
`resumen.put(rs.getString("categoria"), rs.getDouble("total"))`.
El mapa retornado viaja al `OperacionesController` y de ahí a
`ConsoleView.mostrarReporteAnalitico()`, donde se itera con
`Map.Entry<String, Double>` para calcular el porcentaje de cada
categoría sobre el total y renderizar la barra de progreso ASCII.

#### Por qué se tomó esta decisión

El problema que resuelve este mapa es de naturaleza **asociativa**:
para cada cadena de texto (categoría) se necesita recuperar y
actualizar un valor numérico acumulado (total). `HashMap` ofrece
acceso y escritura en **O(1)** promedio gracias a su función hash
interna, independientemente del número de categorías. Las alternativas
habrían sido:

- Un arreglo paralelo de nombres y montos: frágil, sin relación semántica explícita.
- Una `List` de pares: requeriría búsqueda lineal O(n) para encontrar la categoría.
- `LinkedHashMap`: preservaría el orden de inserción, pero aquí el orden
  lo provee directamente el SQL (`ORDER BY total DESC`), haciendo innecesaria
  esa garantía adicional.

`HashMap` es la estructura canónica para agrupación por clave cuando el
orden no es un requisito: semánticamente precisa, sin overhead innecesario.

---

## 3. Guion para Diapositivas

---

### Diapositiva 1 — ¿Qué es ChatFinance?

- Sistema de gestión financiera personal en consola — Java 17 + SQLite
- Autenticación sin contraseña: el número de WhatsApp es la llave de acceso
- Operaciones: registrar ingresos, gastos y transferencias entre cuentas propias
- Reportes: clasificación por categoría con porcentajes y balance neto
- Arquitectura MVC estricta · JDBC puro · sin frameworks externos

---

### Diapositiva 2 — Interfaces y Genéricos

**Interfaces como contratos desacoplados**

- `INotificador` define `enviarMensaje()` — `WhatsAppService` la implementa hoy en consola, mañana con la Meta API: **cero cambios** en el resto del sistema
- Principio Open/Closed: el sistema está abierto a extensión, cerrado a modificación

**Genéricos para eliminar duplicación**

- `CrudRepository<T, ID>` declara `guardar`, `buscarPorId` y `listarTodos` **una sola vez**
- `CuentaDAO` lo instancia con `<CuentaFinanciera, Integer>`; `TransaccionDAO` con `<MovimientoRegistro, Integer>`
- El compilador verifica los tipos en tiempo de compilación, no en ejecución

---

### Diapositiva 3 — Herencia Abstracta y Polimorfismo

**Jerarquía de cuentas**

```
CuentaFinanciera (abstract)
  ├── BilleteraDigital  →  alias, proveedor
  └── CuentaBancaria    →  banco, cci
```

- Tres métodos abstractos **obligan** a cada subclase a definir su propio comportamiento
- `obtenerDetalleImprimible()` se llama de forma polimórfica: el sistema no sabe ni le importa qué tipo concreto tiene
- `CuentaDAO.mapearFila()` aplica Single Table Inheritance: lee el discriminador `tipo_cuenta` y construye la subclase correcta
- Añadir `CriptoWallet` solo requiere crear una subclase — la vista, el DAO y los controladores existentes **no cambian**

---

### Diapositiva 4 — Estructuras de Datos: la elección correcta importa

| Estructura | Dónde | Operación crítica | Complejidad |
|-----------|-------|-------------------|-------------|
| `ArrayList` | DAOs — retorno de consultas | Iteración secuencial, acceso por índice | O(1) lectura |
| `LinkedList` | `OperacionesController` — historial de sesión | `addFirst()` + `removeLast()` · cola FIFO cap. 5 | O(1) en ambos extremos |
| `HashMap` | `TransaccionDAO` — reportes analíticos | Agrupación categoría → total acumulado | O(1) promedio por clave |

- `ArrayList` en los DAOs: el acceso por índice es O(1) y la vista itera una sola vez
- `LinkedList` en el historial: insertar al frente con `ArrayList` es O(n) — desplaza todos los elementos
- `HashMap` en el reporte: la búsqueda por clave de texto es O(1) — imposible de lograr con listas paralelas

---

### Diapositiva 5 — Conclusión Arquitectónica

**Decisiones que escalan**

- `INotificador` + `WhatsAppService` es el punto de extensión listo para la Meta Cloud API
- `CrudRepository<T, ID>` garantiza que todos los DAOs futuros sigan el mismo contrato sin reescribir firmas
- La clase abstracta `CuentaFinanciera` hace imposible, por diseño del compilador, tener una subclase sin sus contratos de dominio

**Cohesión por capa**

- `modelo/` — entidades puras, sin dependencias externas
- `dao/` — acceso a datos, JDBC, transacciones ACID con `rollback`
- `controller/` — lógica de aplicación, orquestación de flujos
- `view/` — toda la E/S en un único punto, nunca dispersa en otra capa

**Resultado:** cada decisión técnica tiene una justificación de rendimiento, mantenimiento o escalabilidad — no de conveniencia.
