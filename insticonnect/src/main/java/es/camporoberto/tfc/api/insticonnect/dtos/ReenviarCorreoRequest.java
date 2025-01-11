package es.camporoberto.tfc.api.insticonnect.dtos;

import lombok.Getter;

@Getter
public class ReenviarCorreoRequest {
    private String email;

    // Constructor vacío (necesario para la deserialización de JSON)

    public ReenviarCorreoRequest() {}

    public void setEmail(String email) {
        this.email = email;
    }
    // Getter
    private String token;


    public ReenviarCorreoRequest(String token) {
        this.token = token;
    }

    // Setter
    public void setToken(String token) {
        this.token = token;
    }
}
