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
 * Servicio de Inteligencia Artificial que se comunica con el modelo Ollama local
 * para interpretar texto en lenguaje natural y extraer una intencion financiera.
 *
 * Requiere Ollama corriendo en http://localhost:11434 con el modelo "llama3.2" disponible.
 */
public class AsistenteIAService {

    private static final String URL_OLLAMA  = "http://localhost:11434";
    private static final String MODELO      = "llama3.2";
    private static final int    TIMEOUT_SEG = 60;

    private final ChatLanguageModel modeloChat;
    private final Gson              gson;

    private static final String SYSTEM_PROMPT_PLANTILLA =
            "Eres un extractor de datos financieros. El usuario te dara un texto en espanol " +
            "describiendo un movimiento de dinero. " +
            "Debes extraer la intencion y devolver UNICAMENTE un objeto JSON valido con exactamente " +
            "estas claves: \"tipo\", \"monto\", \"categoria\", \"cuenta\", \"descripcion\". " +
            "Reglas estrictas: " +
            "1. \"tipo\" debe ser exactamente \"INGRESO\" o \"GASTO\" en mayusculas. " +
            "2. \"monto\" debe ser un numero decimal positivo sin simbolo de moneda. " +
            "3. \"categoria\" para GASTO debe ser una de: Alimentacion, Transporte, Servicios, " +
            "   Entretenimiento, Otros. Para INGRESO: Sueldo, Freelance, Otros. " +
            "4. \"cuenta\" debe ser el alias o nombre del proveedor mencionado. " +
            "   Cuentas disponibles del usuario: %s. Elige la que mas se parezca al texto. " +
            "   Si no se menciona ninguna, elige la primera de la lista. " +
            "5. \"descripcion\" debe ser un resumen breve en espanol de maximo 50 caracteres. " +
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
     * Interpreta un texto libre del usuario y extrae la intencion financiera.
     *
     * @param textoUsuario            El texto libre escrito por el usuario.
     * @param listaCuentasDisponibles Nombres/alias de las cuentas del usuario separados por coma.
     * @return Un {@link IntencionOperacionDTO} con los datos extraidos, o null si falla el parseo.
     */
    public IntencionOperacionDTO interpretarTexto(String textoUsuario, String listaCuentasDisponibles) {
        String systemPrompt = String.format(SYSTEM_PROMPT_PLANTILLA, listaCuentasDisponibles);

        List<ChatMessage> mensajes = List.of(
                SystemMessage.from(systemPrompt),
                UserMessage.from(textoUsuario)
        );

        try {
            Response<AiMessage> respuesta = modeloChat.generate(mensajes);
            String jsonRespuesta = respuesta.content().text().trim();
            jsonRespuesta = extraerJsonLimpio(jsonRespuesta);

            IntencionOperacionDTO dto = gson.fromJson(jsonRespuesta, IntencionOperacionDTO.class);

            if (dto.getTipo() != null) {
                dto.setTipo(dto.getTipo().toUpperCase().trim());
            }
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
     * Proteccion ante respuestas con markdown o texto adicional.
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

