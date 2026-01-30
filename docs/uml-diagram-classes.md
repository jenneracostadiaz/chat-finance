classDiagram
%% --- RELACIONES DE HERENCIA (GENERALIZACIÓN) ---
%% Una BilleteraDigital ES UNA CuentaFinanciera
CuentaFinanciera <|-- BilleteraDigital : Herencia
%% Una CuentaBancaria ES UNA CuentaFinanciera
CuentaFinanciera <|-- CuentaBancaria : Herencia

    %% Un Pago ES UNA Transacción
    Transaccion <|-- Pago : Herencia
    %% Un Cobro ES UNA Transacción
    Transaccion <|-- Cobro : Herencia

    %% --- RELACIONES DE IMPLEMENTACIÓN (REALIZACIÓN) ---
    %% WhatsAppService cumple el contrato de INotificador
    INotificador <|.. WhatsAppService : Implementación

    %% --- RELACIONES DE COMPOSICIÓN (TIENE UN - FUERTE) ---
    %% Si borras la Transacción, la Comisión desaparece
    Transaccion "1" *-- "1" Comision : Composición
    %% Si borras la Transacción recurrente, el Recordatorio desaparece
    Transaccion "1" *-- "0..1" Recordatorio : Composición

    %% --- RELACIONES DE AGREGACIÓN (TIENE UN - DÉBIL) ---
    %% El Usuario tiene Cuentas, pero las cuentas existen sin el usuario (en el banco)
    Usuario "1" o-- "1..*" CuentaFinanciera : Agregación

    %% --- RELACIONES DE ASOCIACIÓN (USA / CONOCE) ---
    %% El Usuario registra Transacciones
    Usuario "1" --> "*" Transaccion : Asociación (Registra)
    %% La Transacción usa una Cuenta para mover el dinero
    Transaccion "*" --> "1" CuentaFinanciera : Asociación (Usa)
    %% El Recordatorio usa el Notificador para enviar el mensaje
    Recordatorio ..> INotificador : Dependencia (Usa)


    %% --- DEFINICIÓN DE CLASES ---
    class Usuario {
        -String idUsuario
        -String numeroWhatsApp
        -String pinSeguridad
        +autenticar(pin)
        +registrarTransaccion()
    }

    class CuentaFinanciera {
        <<Abstract>>
        -String idCuenta
        -String numeroCuenta
        -Double saldo
        +validarCuenta()
    }

    class BilleteraDigital {
        -String alias
        -String proveedor
        +generarQR()
    }

    class CuentaBancaria {
        -String banco
        -String cci
        +validarInterbancario()
    }

    class Transaccion {
        <<Abstract>>
        -String idTransaccion
        -Date fecha
        -Double monto
        -Boolean esRecurrente
        +procesar()
    }

    class Pago {
        -String destinatario
        +autorizarPago()
    }

    class Cobro {
        -String pagador
        +generarLinkCobro()
    }

    class Comision {
        -Double porcentaje
        -Double totalComision
        +calcular()
    }

    class Recordatorio {
        -Date fechaProxima
        -String frecuencia
        +programarEnvio()
    }

    class INotificador {
        <<Interface>>
        +enviarMensaje(destino, texto)
    }

    class WhatsAppService {
        -String apiKey
        +enviarMensaje(destino, texto)
        +conectarAPI()
    }