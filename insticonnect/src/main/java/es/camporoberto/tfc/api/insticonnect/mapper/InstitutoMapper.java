package es.camporoberto.tfc.api.insticonnect.mapper;

import es.camporoberto.tfc.api.insticonnect.dtos.InstitutoDTO;
import es.camporoberto.tfc.api.insticonnect.entidades.Instituto;

public class InstitutoMapper {

    // Metodo para convertir desde la entidad Instituto al DTO
    public static InstitutoDTO fromEntity(Instituto instituto) {
        return new InstitutoDTO(
                instituto.getId(),
                instituto.getNombre(),
                instituto.getComunidadAutonoma().getId() // Representar comunidad autónoma por ID
        );
    }

    // Metodo para convertir desde el DTO a la entidad Instituto
    public static Instituto toEntity(InstitutoDTO dto) {
        Instituto instituto = new Instituto();
        instituto.setId(dto.getId());
        instituto.setNombre(dto.getNombre());
        return instituto;
    }
}
