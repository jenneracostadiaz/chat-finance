package modelo;

import java.util.Date;

public class Pago extends Transaccion {
    private String destinatario;

    public Pago(String idTransaccion, Date fecha, Double monto, Boolean esRecurrente, CuentaFinanciera cuenta, String destinatario) {
        super(idTransaccion, fecha, monto, esRecurrente, cuenta);
        this.destinatario = destinatario;
    }

    @Override
    public void procesar() {
        // Lógica para procesar el pago
        Double comisionCalculada = getComision().calcular(getMonto());
        Double montoTotal = getMonto() + comisionCalculada;
        System.out.println("Procesando pago de " + montoTotal + " hacia " + destinatario);
    }

    public boolean autorizarPago() {
        // Lógica para autorizar el pago
        return getCuenta().getSaldo() >= getMonto();
    }

    // Getters y Setters
    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }
}
