-- =========================================
-- ChatFinance - Script SQL
-- FASE 2: Gestión de Cuentas y Saldos
-- =========================================

-- Tabla de Usuarios (FASE 1)
CREATE TABLE IF NOT EXISTS usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    numero_whatsapp TEXT NOT NULL UNIQUE,
    nombre TEXT NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Cuentas (FASE 2)
-- Implementa el patrón "Single Table Inheritance"
-- El campo tipo_cuenta actúa como discriminador para diferenciar
-- entre BilleteraDigital y CuentaBancaria
CREATE TABLE IF NOT EXISTS cuentas (
    -- Campos comunes a todas las cuentas
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id INTEGER NOT NULL,
    numero_cuenta TEXT NOT NULL,
    saldo REAL NOT NULL DEFAULT 0.0,

    -- Discriminador: indica el tipo de cuenta
    tipo_cuenta TEXT NOT NULL CHECK(tipo_cuenta IN ('BILLETERA', 'BANCO')),

    -- Campos específicos de BilleteraDigital
    -- Solo se usan cuando tipo_cuenta = 'BILLETERA'
    alias TEXT,              -- Ej: "Yape Personal", "Plin Trabajo"
    proveedor TEXT,          -- Ej: "BCP", "Interbank"

    -- Campos específicos de CuentaBancaria
    -- Solo se usan cuando tipo_cuenta = 'BANCO'
    banco TEXT,              -- Ej: "BCP", "Interbank", "BBVA"
    cci TEXT,                -- Código de Cuenta Interbancario (20 dígitos)

    -- Metadata
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    UNIQUE(usuario_id, numero_cuenta)  -- Un usuario no puede tener dos cuentas con el mismo número
);

-- =========================================
-- Índices para optimizar consultas
-- =========================================

-- Índice para búsquedas por usuario
CREATE INDEX IF NOT EXISTS idx_cuentas_usuario_id
ON cuentas(usuario_id);

-- Índice para búsquedas por tipo de cuenta
CREATE INDEX IF NOT EXISTS idx_cuentas_tipo
ON cuentas(tipo_cuenta);

-- =========================================
-- Datos de prueba (opcional)
-- =========================================

-- Insertar un usuario de prueba
-- INSERT INTO usuarios (numero_whatsapp, nombre)
-- VALUES ('987654321', 'Juan Pérez');

-- Insertar una billetera digital de prueba
-- INSERT INTO cuentas (usuario_id, numero_cuenta, saldo, tipo_cuenta, alias, proveedor, banco, cci)
-- VALUES (1, '987654321', 50.00, 'BILLETERA', 'Yape Personal', 'BCP', NULL, NULL);

-- Insertar una cuenta bancaria de prueba
-- INSERT INTO cuentas (usuario_id, numero_cuenta, saldo, tipo_cuenta, alias, proveedor, banco, cci)
-- VALUES (1, '19312345678', 1500.00, 'BANCO', NULL, NULL, 'BCP', '00219300123456780123');

-- =========================================
-- Consultas útiles
-- =========================================

-- Ver todas las cuentas de un usuario
-- SELECT * FROM cuentas WHERE usuario_id = 1;

-- Calcular patrimonio total de un usuario
-- SELECT SUM(saldo) as patrimonio_total FROM cuentas WHERE usuario_id = 1;

-- Ver solo billeteras digitales
-- SELECT * FROM cuentas WHERE tipo_cuenta = 'BILLETERA';

-- Ver solo cuentas bancarias
-- SELECT * FROM cuentas WHERE tipo_cuenta = 'BANCO';
