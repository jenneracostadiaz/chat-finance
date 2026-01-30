package modelo;

import java.util.Date;

public class Recordatorio {
    private Date fechaProxima;
    private String frecuencia;
    private INotificador notificador; // Dependencia

    public Recordatorio(Date fechaProxima, String frecuencia) {
        this.fechaProxima = fechaProxima;
        this.frecuencia = frecuencia;
    }

    public void programarEnvio(INotificador notificador, String destino, String mensaje) {
        this.notificador = notificador;
        // Lógica para programar el envío del recordatorio
        if (notificador != null) {
            notificador.enviarMensaje(destino, mensaje);
        }
    }

    // Getters y Setters
    public Date getFechaProxima() {
        return fechaProxima;
    }

    public void setFechaProxima(Date fechaProxima) {
        this.fechaProxima = fechaProxima;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(String frecuencia) {
        this.frecuencia = frecuencia;
    }
}
