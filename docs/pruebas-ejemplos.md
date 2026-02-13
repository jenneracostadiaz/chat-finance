# 🧪 Guía de Pruebas y Ejemplos de Uso - ChatFinance

## 📝 Escenarios de Prueba

### Escenario 1: Usuario Nuevo (Primera Vez)

**Entrada del Usuario:**
```
Ingrese su número de WhatsApp: +56912345678
Número no reconocido. Ingrese su Nombre para registrarse.
Nombre: Juan Pérez
```

**Salida Esperada:**
```
═══════════════════════════════════════════════════════════
✓ ¡REGISTRO EXITOSO! BIENVENIDO, JUAN PÉREZ! 🎉
═══════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────
💰 MENÚ PRINCIPAL - CHATFINANCE
──────────────────────────────────────────────────────────
1. Ver Saldo (Próximamente)
2. Salir
──────────────────────────────────────────────────────────
➤ Seleccione una opción:
```

**Verificación en Base de Datos:**
```sql
SELECT * FROM usuarios WHERE numero_whatsapp = '+56912345678';
```
**Resultado Esperado:**
```
id | numero_whatsapp | nombre      | fecha_registro
1  | +56912345678    | Juan Pérez  | 2026-02-13 10:30:00
```

---

### Escenario 2: Usuario Existente (Login)

**Entrada del Usuario:**
```
Ingrese su número de WhatsApp: +56912345678
```

**Salida Esperada:**
```
═══════════════════════════════════════════════════════════
✓ BIENVENIDO NUEVAMENTE, JUAN PÉREZ! 👋
═══════════════════════════════════════════════════════════

──────────────────────────────────────────────────────────
💰 MENÚ PRINCIPAL - CHATFINANCE
──────────────────────────────────────────────────────────
1. Ver Saldo (Próximamente)
2. Salir
──────────────────────────────────────────────────────────
➤ Seleccione una opción:
```

---

### Escenario 3: Navegación del Menú

**Entrada del Usuario:**
```
➤ Seleccione una opción: 1
```

**Salida Esperada:**
```
⏳ Esta funcionalidad estará disponible próximamente...

──────────────────────────────────────────────────────────
💰 MENÚ PRINCIPAL - CHATFINANCE
──────────────────────────────────────────────────────────
1. Ver Saldo (Próximamente)
2. Salir
──────────────────────────────────────────────────────────
➤ Seleccione una opción:
```

---

### Escenario 4: Salir de la Aplicación

**Entrada del Usuario:**
```
➤ Seleccione una opción: 2
```

**Salida Esperada:**
```
═══════════════════════════════════════════════════════════
👋 ¡HASTA PRONTO! Gracias por usar ChatFinance.
═══════════════════════════════════════════════════════════

✓ Conexión a base de datos cerrada.
```

---

### Escenario 5: Validación de Entrada Inválida

**Entrada del Usuario:**
```
➤ Seleccione una opción: abc
Por favor, ingrese un número válido.
➤ Seleccione una opción: 5
✗ ERROR: Opción inválida. Por favor, seleccione 1 o 2.
```

---

## 🔍 Pruebas Técnicas

### 1. Verificar Creación de Base de Datos

**Comando:**
```bash
cd /Users/jenner/IdeaProjects/ChatFinance
ls -la finanzas.db
```

**Resultado Esperado:**
```
-rw-r--r--  1 user  staff  12288 Feb 13 10:30 finanzas.db
```

---

### 2. Inspeccionar Estructura de la Tabla

**Usando SQLite CLI:**
```bash
sqlite3 finanzas.db

sqlite> .schema usuarios
CREATE TABLE usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    numero_whatsapp TEXT NOT NULL UNIQUE,
    nombre TEXT NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

sqlite> .quit
```

---

### 3. Consultar Usuarios Registrados

```bash
sqlite3 finanzas.db "SELECT * FROM usuarios;"
```

**Resultado Esperado:**
```
1|+56912345678|Juan Pérez|2026-02-13 10:30:00
2|+56987654321|María García|2026-02-13 11:15:00
```

---

### 4. Verificar Unicidad del Número de WhatsApp

**Intento de Registro Duplicado:**

Si intentas registrar un número ya existente, el DAO debería manejar la excepción:

```java
// En UsuarioDAO.crearUsuario()
catch (SQLException e) {
    if (e.getMessage().contains("UNIQUE constraint failed")) {
        System.err.println("✗ El número de WhatsApp ya está registrado.");
    }
}
```

---

## 🐛 Casos Límite y Manejo de Errores

### Caso 1: Número de WhatsApp Vacío

**Entrada:**
```
Ingrese su número de WhatsApp: [Enter]
```

**Salida Esperada:**
```
✗ ERROR: El número de WhatsApp no puede estar vacío.
✓ Conexión a base de datos cerrada.
```

---

### Caso 2: Nombre Vacío durante Registro

**Entrada:**
```
Ingrese su número de WhatsApp: +56999999999
Nombre: [Enter]
```

**Salida Esperada:**
```
✗ ERROR: El nombre no puede estar vacío.
✓ Conexión a base de datos cerrada.
```

---

### Caso 3: Base de Datos Corrupta

Si `finanzas.db` está corrupta, el sistema debe:
1. Imprimir error en consola
2. No crashear abruptamente
3. Cerrar recursos correctamente

**Simulación:**
```bash
echo "corrupt data" > finanzas.db
./run.sh
```

**Salida Esperada:**
```
✗ Error al conectar con la base de datos.
java.sql.SQLException: file is not a database
✗ Error crítico en la aplicación:
```

---

## 📊 Pruebas de Carga Básicas

### Insertar Múltiples Usuarios

**Script de Prueba:**
```java
public class TestMultipleUsers {
    public static void main(String[] args) {
        UsuarioDAO dao = new UsuarioDAO();
        
        for (int i = 1; i <= 100; i++) {
            Usuario usuario = new Usuario(
                "+5691234567" + String.format("%02d", i),
                "Usuario " + i
            );
            dao.crearUsuario(usuario);
        }
        
        System.out.println("✓ 100 usuarios creados exitosamente");
    }
}
```

**Verificación:**
```bash
sqlite3 finanzas.db "SELECT COUNT(*) FROM usuarios;"
# Resultado esperado: 100
```

---

## 🧹 Limpieza y Reset de Base de Datos

### Eliminar Base de Datos
```bash
rm finanzas.db
```

### Resetear Tabla de Usuarios
```bash
sqlite3 finanzas.db "DELETE FROM usuarios;"
sqlite3 finanzas.db "DELETE FROM sqlite_sequence WHERE name='usuarios';"
```

---

## 🔧 Debugging Tips

### Habilitar Logging de SQL (Futuro)

En `DatabaseConnection.java`:
```java
DriverManager.setLogWriter(new PrintWriter(System.out));
```

### Imprimir Estado del Usuario Actual

En `LoginController.java`:
```java
System.out.println("[DEBUG] Usuario actual: " + usuarioActual.getNombre());
System.out.println("[DEBUG] ID: " + usuarioActual.getId());
```

### Verificar Conexión a BD

```java
Connection conn = DatabaseConnection.getInstance().getConnection();
if (conn != null && !conn.isClosed()) {
    System.out.println("✓ Conexión activa");
} else {
    System.out.println("✗ Conexión cerrada");
}
```

---

## 📈 Métricas de Calidad

### Checklist de Implementación

- ✅ Arquitectura MVC implementada correctamente
- ✅ Singleton para DatabaseConnection
- ✅ PreparedStatements en todos los DAOs
- ✅ Try-with-resources en todas las conexiones
- ✅ Validación de inputs del usuario
- ✅ Manejo de excepciones SQL
- ✅ Código comentado y documentado
- ✅ Separación clara de responsabilidades
- ✅ Base de datos se crea automáticamente
- ✅ Cierre correcto de recursos

### Checklist de Funcionalidad

- ✅ Login por número de WhatsApp
- ✅ Registro de nuevos usuarios
- ✅ Persistencia en SQLite
- ✅ Menú principal funcional
- ✅ Opción de salir
- ✅ Mensajes de bienvenida diferenciados
- ✅ Validación de entradas

---

## 🎯 Pruebas Recomendadas

1. **Prueba Funcional Básica**: Registrar usuario → Cerrar app → Abrir app → Login exitoso
2. **Prueba de Unicidad**: Intentar registrar el mismo número dos veces
3. **Prueba de Validación**: Ingresar datos vacíos y caracteres especiales
4. **Prueba de Persistencia**: Crear 10 usuarios, cerrar app, verificar que todos persisten
5. **Prueba de Menú**: Navegar todas las opciones del menú

---

## 🚀 Ejecución de Pruebas Completas

### Script de Prueba Automática (Bash)

```bash
#!/bin/bash

echo "🧪 INICIANDO SUITE DE PRUEBAS"

# Limpiar base de datos
rm -f finanzas.db

# Prueba 1: Primera ejecución
echo -e "+56911111111\nUsuario Test 1\n2\n" | java -cp "out:lib/*" Main

# Prueba 2: Login existente
echo -e "+56911111111\n2\n" | java -cp "out:lib/*" Main

# Prueba 3: Verificar DB
COUNT=$(sqlite3 finanzas.db "SELECT COUNT(*) FROM usuarios;")
if [ "$COUNT" -eq 1 ]; then
    echo "✓ Prueba de persistencia exitosa"
else
    echo "✗ Error en prueba de persistencia"
fi

echo "✓ SUITE DE PRUEBAS COMPLETADA"
```

---

**Última actualización:** Febrero 2026  
**Estado:** Fase 1 Completa ✅
