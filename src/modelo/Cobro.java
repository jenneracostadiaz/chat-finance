package modelo;

import java.util.Date;
import java.util.UUID;

public class Cobro extends Transaccion {
    private String pagador;

    public Cobro(String idTransaccion, Date fecha, Double monto, Boolean esRecurrente, CuentaFinanciera cuenta, String pagador) {
        super(idTransaccion, fecha, monto, esRecurrente, cuenta);
        this.pagador = pagador;
    }

    @Override
    public void procesar() {
        // Lógica para procesar el cobro
        System.out.println("Procesando cobro de " + getMonto() + " desde " + pagador);
    }

    public String generarLinkCobro() {
        // Genera un link único para el cobro
        String uniqueId = UUID.randomUUID().toString();
        return "https://chatfinance.com/cobro/" + uniqueId + "?monto=" + getMonto();
    }

    // Getters y Setters
    public String getPagador() {
        return pagador;
    }

    public void setPagador(String pagador) {
        this.pagador = pagador;
    }
}
