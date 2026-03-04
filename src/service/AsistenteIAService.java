package service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.output.Response;

import java.time.Duration;
import java.util.List;

/**
 * Servicio de Inteligencia Artificial — Router de Intenciones.
 * Clasifica el texto libre del usuario en una de 4 intenciones y extrae
 * los parametros relevantes en un {@link IntencionOperacionDTO}.
 *
 * Requiere Ollama corriendo en http://localhost:11434 con el modelo "llama3.2".
 */
public class AsistenteIAService {

    private static final String URL_OLLAMA  = "http://localhost:11434";
    private static final String MODELO      = "llama3.2";
    private static final int    TIMEOUT_SEG = 60;

    private final ChatLanguageModel modeloChat;
    private final Gson              gson;

    private static final String SYSTEM_PROMPT_PLANTILLA =
        "Eres el cerebro de ChatFinance, una app de finanzas personales. " +
        "Analiza el texto del usuario y devuelve UNICAMENTE un JSON con estos campos: " +
        "\"intencion\", \"tipoTransaccion\", \"monto\", \"categoria\", \"nombreCuenta\", \"tipoCuentaNueva\", \"descripcion\". " +

        "PASO 1 — Clasifica la intencion en exactamente una de estas 4 opciones (en mayusculas): " +
        "REGISTRAR_TRANSACCION, CREAR_CUENTA, VER_REPORTE, VER_SALDOS. " +

        "PASO 2 — Extrae los parametros segun la intencion. Usa null para los que no apliquen: " +
        "- REGISTRAR_TRANSACCION: tipoTransaccion (INGRESO|GASTO), monto, categoria, nombreCuenta, descripcion. " +
        "  Categorias GASTO: Alimentacion, Transporte, Servicios, Entretenimiento, Otros. " +
        "  Categorias INGRESO: Sueldo, Freelance, Otros. " +
        "  Cuentas existentes del usuario: %s. Elige la que mas se parezca; si no se menciona, elige la primera. " +
        "- CREAR_CUENTA: nombreCuenta (nombre del banco o billetera), tipoCuentaNueva (BANCO|BILLETERA), monto (saldo inicial si se menciona). " +
        "- VER_REPORTE: todos los parametros en null. " +
        "- VER_SALDOS: todos los parametros en null. " +

        "Ejemplos: " +
        "  'Gaste 20 en taxi con Yape' -> {\"intencion\":\"REGISTRAR_TRANSACCION\",\"tipoTransaccion\":\"GASTO\",\"monto\":20.0,\"categoria\":\"Transporte\",\"nombreCuenta\":\"Yape\",\"tipoCuentaNueva\":null,\"descripcion\":\"Taxi\"} " +
        "  'Crea una cuenta BCP con 500 soles' -> {\"intencion\":\"CREAR_CUENTA\",\"tipoTransaccion\":null,\"monto\":500.0,\"categoria\":null,\"nombreCuenta\":\"BCP\",\"tipoCuentaNueva\":\"BANCO\",\"descripcion\":null} " +
        "  'Muestrame mis gastos del mes' -> {\"intencion\":\"VER_REPORTE\",\"tipoTransaccion\":null,\"monto\":null,\"categoria\":null,\"nombreCuenta\":null,\"tipoCuentaNueva\":null,\"descripcion\":null} " +
        "  'Cuanto tengo en mis cuentas' -> {\"intencion\":\"VER_SALDOS\",\"tipoTransaccion\":null,\"monto\":null,\"categoria\":null,\"nombreCuenta\":null,\"tipoCuentaNueva\":null,\"descripcion\":null} " +

        "No agregues texto fuera del JSON. No uses markdown. Solo el JSON puro.";

    public AsistenteIAService() {
        this.modeloChat = OllamaChatModel.builder()
                .baseUrl(URL_OLLAMA)
                .modelName(MODELO)
                .format("json")
                .temperature(0.0)
                .timeout(Duration.ofSeconds(TIMEOUT_SEG))
                .build();
        this.gson = new Gson();
    }

    /**
     * Interpreta un texto libre del usuario y clasifica su intencion con los parametros relevantes.
     *
     * @param textoUsuario            Texto libre escrito por el usuario.
     * @param listaCuentasDisponibles Nombres/alias de las cuentas del usuario, separados por coma.
     * @return {@link IntencionOperacionDTO} con la intencion y parametros extraidos, o null si falla.
     */
    public IntencionOperacionDTO interpretarTexto(String textoUsuario, String listaCuentasDisponibles) {
        String systemPrompt = String.format(SYSTEM_PROMPT_PLANTILLA, listaCuentasDisponibles);

        List<ChatMessage> mensajes = List.of(
                SystemMessage.from(systemPrompt),
                UserMessage.from(textoUsuario)
        );

        try {
            Response<AiMessage> respuesta = modeloChat.generate(mensajes);
            String jsonRespuesta = extraerJsonLimpio(respuesta.content().text().trim());

            IntencionOperacionDTO dto = gson.fromJson(jsonRespuesta, IntencionOperacionDTO.class);

            // Normalizar a mayusculas (el setter ya lo hace, pero por si Gson saltea el setter)
            if (dto.getIntencion() != null)
                dto.setIntencion(dto.getIntencion().toUpperCase().trim());
            if (dto.getTipoTransaccion() != null)
                dto.setTipoTransaccion(dto.getTipoTransaccion().toUpperCase().trim());
            if (dto.getTipoCuentaNueva() != null)
                dto.setTipoCuentaNueva(dto.getTipoCuentaNueva().toUpperCase().trim());

            return dto;

        } catch (JsonSyntaxException e) {
            System.err.println("Error: La IA devolvio JSON invalido: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Error al comunicarse con Ollama: " + e.getMessage());
            System.err.println("Verifique que Ollama este ejecutandose: ollama run " + MODELO);
            return null;
        }
    }

    /**
     * Verifica si Ollama esta disponible realizando una llamada de prueba minima.
     */
    public boolean verificarConexion() {
        try {
            modeloChat.generate(List.of(UserMessage.from("ping")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrae el primer bloque JSON de una cadena de texto.
     * Proteccion ante respuestas con markdown o texto adicional de la IA.
     */
    private String extraerJsonLimpio(String texto) {
        int inicio = texto.indexOf('{');
        int fin    = texto.lastIndexOf('}');
        if (inicio != -1 && fin != -1 && fin > inicio) {
            return texto.substring(inicio, fin + 1);
        }
        return texto;
    }
}
