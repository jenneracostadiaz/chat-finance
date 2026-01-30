package modelo;

public class WhatsAppService implements INotificador {
    private String apiKey;

    public WhatsAppService(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public void enviarMensaje(String destino, String texto) {
        // Lógica para enviar mensaje por WhatsApp
        if (conectarAPI()) {
            System.out.println("Enviando mensaje a " + destino + ": " + texto);
        } else {
            System.out.println("Error: No se pudo conectar a la API de WhatsApp");
        }
    }

    public boolean conectarAPI() {
        // Lógica para conectar con la API de WhatsApp
        return apiKey != null && !apiKey.isEmpty();
    }

    // Getters y Setters
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
