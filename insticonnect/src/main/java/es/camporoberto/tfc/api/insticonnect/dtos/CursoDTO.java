package es.camporoberto.tfc.api.insticonnect.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CursoDTO {
    private Integer id;          // ID del ciclo formativo
    private String nombre;       // Nombre del ciclo formativo
    private String nivel;        // Nivel del ciclo formativo
    private String anioLectivo;  // Año lectivo del curso
    private String anio;         // Año del curso (primero/segundo)
}
