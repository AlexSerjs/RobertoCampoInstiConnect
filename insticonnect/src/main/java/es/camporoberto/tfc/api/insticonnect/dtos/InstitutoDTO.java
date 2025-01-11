package es.camporoberto.tfc.api.insticonnect.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstitutoDTO {
    private Integer id;
    private String nombre;
    private Integer comunidadAutonomaId;
}
