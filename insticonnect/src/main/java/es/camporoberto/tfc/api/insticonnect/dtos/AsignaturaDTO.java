package es.camporoberto.tfc.api.insticonnect.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignaturaDTO {
    private Integer id;
    private String nombre;
    private String profesor;
    private String email;

    public AsignaturaDTO(String nombre, String profesor, String email) {
        this.nombre = nombre;
        this.profesor = profesor;
        this.email = email;
    }
}
