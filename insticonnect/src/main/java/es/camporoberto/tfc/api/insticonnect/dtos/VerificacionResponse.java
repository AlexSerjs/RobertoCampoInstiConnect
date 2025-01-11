package es.camporoberto.tfc.api.insticonnect.dtos;

public class VerificacionResponse {
    private String status;
    private String tipo;

    public VerificacionResponse(String status, String tipo) {
        this.status = status;
        this.tipo = tipo;
    }

    // Getters y setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
