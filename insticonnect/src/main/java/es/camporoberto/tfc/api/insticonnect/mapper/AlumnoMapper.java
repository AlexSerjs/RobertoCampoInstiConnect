package es.camporoberto.tfc.api.insticonnect.mapper;

import es.camporoberto.tfc.api.insticonnect.dtos.AlumnoDTO;
import es.camporoberto.tfc.api.insticonnect.entidades.Alumno;

public class AlumnoMapper {

    // Método para convertir desde la entidad Alumno al DTO
    public static AlumnoDTO fromEntity(Alumno alumno) {
        return new AlumnoDTO(
                alumno.getId(),
                alumno.getNombreCompleto(),
                alumno.getEmail(),
                alumno.getCodigoGrupo()
        );
    }

    // Metodo para convertir desde el DTO a la entidad Alumno
    public static Alumno toEntity(AlumnoDTO dto) {
        Alumno alumno = new Alumno();
        alumno.setId(dto.getId());
        alumno.setNombreCompleto(dto.getNombreCompleto());
        alumno.setEmail(dto.getEmail());
        alumno.setCodigoGrupo(dto.getCodigoGrupo());
        return alumno;
    }
}
