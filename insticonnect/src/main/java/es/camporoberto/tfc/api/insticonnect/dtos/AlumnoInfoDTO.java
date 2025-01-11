package es.camporoberto.tfc.api.insticonnect.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AlumnoInfoDTO {
    private String nombreCompleto;
    private String grado;
    private String nombreComunidadAutonoma;
    private String nombreInstituto;
    private String anioLectivo;
    private String correo;
    private String foto;
}
