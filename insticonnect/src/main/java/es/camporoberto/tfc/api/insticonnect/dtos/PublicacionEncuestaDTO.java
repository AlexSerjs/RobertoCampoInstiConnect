package es.camporoberto.tfc.api.insticonnect.dtos;


import es.camporoberto.tfc.api.insticonnect.entidades.Publicacion;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class PublicacionEncuestaDTO extends PublicacionDTO {
    private List<OpcionEncuestaDTO> opcionesEncuesta;

    public PublicacionEncuestaDTO(Publicacion publicacion) {
        super(publicacion); // Usa el constructor de la clase base para inicializar campos comunes
        this.opcionesEncuesta = publicacion.getOpciones().stream()
                .map(OpcionEncuestaDTO::new)
                .collect(Collectors.toList());
    }

    // Constructor vacío necesario para Lombok
    public PublicacionEncuestaDTO() {
        super();
    }
}
