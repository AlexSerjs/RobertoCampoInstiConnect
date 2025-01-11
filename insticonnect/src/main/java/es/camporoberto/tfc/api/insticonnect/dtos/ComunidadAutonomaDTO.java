package es.camporoberto.tfc.api.insticonnect.dtos;

import lombok.Data;

@Data
public class ComunidadAutonomaDTO {
    private Integer id;
    private String nombre;

    public ComunidadAutonomaDTO(Integer id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }


}
