package modelo;

public class Comision {
    private Double porcentaje;
    private Double totalComision;

    public Comision(Double porcentaje) {
        this.porcentaje = porcentaje;
        this.totalComision = 0.0;
    }

    public Double calcular(Double monto) {
        this.totalComision = monto * (porcentaje / 100);
        return this.totalComision;
    }

    // Getters y Setters
    public Double getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(Double porcentaje) {
        this.porcentaje = porcentaje;
    }

    public Double getTotalComision() {
        return totalComision;
    }

    public void setTotalComision(Double totalComision) {
        this.totalComision = totalComision;
    }
}
