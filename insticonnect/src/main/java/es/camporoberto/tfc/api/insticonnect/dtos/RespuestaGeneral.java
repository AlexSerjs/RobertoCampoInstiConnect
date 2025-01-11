package es.camporoberto.tfc.api.insticonnect.dtos;

public class RespuestaGeneral {
    private String status;
    private String message;
    private String token;

    public RespuestaGeneral(String status, String message) {
        this.status = status;
        this.message = message;
    }

    // Getters y setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
