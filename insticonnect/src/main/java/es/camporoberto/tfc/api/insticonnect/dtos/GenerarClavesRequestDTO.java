package es.camporoberto.tfc.api.insticonnect.dtos;

import lombok.Data;

@Data
public class GenerarClavesRequestDTO {

    private Integer comunidadId;
    private Integer institutoId;
    private Integer administradorId;
    private String comunidadPrefijo; // Ejemplo: "MAD"
    private Integer cantidad;

    public GenerarClavesRequestDTO() {
    }
}
