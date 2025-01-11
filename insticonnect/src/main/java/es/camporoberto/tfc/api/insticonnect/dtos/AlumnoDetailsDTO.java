package es.camporoberto.tfc.api.insticonnect.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AlumnoDetailsDTO {
    private Integer id;                  // ID del alumno
    private String nombreCompleto;       // Nombre completo del alumno
    private String codigoGrupo;          // Código del grupo al que pertenece el alumno
    private String email;                // Email del alumno
    private String tipo;                 // Tipo de alumno (delegado o alumno)
    private Boolean isVerified;          // Estado de verificación del alumno
    private Boolean puedePublicar;       // Si el alumno puede publicar contenido
    private LocalDateTime fechaCreacion; // Fecha de creación del perfil del alumno
    private String nombreCiclo;          // Nombre del ciclo formativo
    private String nivelCiclo;           // Nivel del ciclo (Grado Medio, Superior, etc.)
    private String anioCiclo;            // Año del ciclo (primero o segundo)
    private String anioLectivo;          // Año lectivo del curso
    private String instituto;            // Nombre del instituto
    private String comunidadAutonoma;    // Nombre de la comunidad autónoma
}
