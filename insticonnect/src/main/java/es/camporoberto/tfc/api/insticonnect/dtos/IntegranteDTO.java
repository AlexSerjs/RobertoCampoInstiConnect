package es.camporoberto.tfc.api.insticonnect.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntegranteDTO {

    private Integer id;
    private String nombreCompleto;
    private String email;
    private Boolean verificado;
    private Boolean puedePublicar;
    private String status;

}
