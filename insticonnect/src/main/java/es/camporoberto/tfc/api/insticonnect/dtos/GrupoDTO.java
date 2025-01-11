package es.camporoberto.tfc.api.insticonnect.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GrupoDTO {
    private Integer id;
    private String codigoGrupo;
    private String nombreCurso; // Nombre del curso relacionado
    private String nombreDelegado; // Nombre del delegado, si existe
}
