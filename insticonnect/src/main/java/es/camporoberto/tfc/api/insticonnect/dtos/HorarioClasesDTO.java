package es.camporoberto.tfc.api.insticonnect.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioClasesDTO {

    private String bloque;
    private String horaInicio;
    private String horaFin;
    private String lunes;
    private String martes;
    private String miercoles;
    private String jueves;
    private String viernes;

}
