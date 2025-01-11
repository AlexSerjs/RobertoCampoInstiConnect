package es.camporoberto.tfc.api.insticonnect.dtos;

import es.camporoberto.tfc.api.insticonnect.entidades.Publicacion;
import lombok.Data;

@Data
public class PublicacionTextoDTO extends PublicacionDTO {
    private String contenido;

    public PublicacionTextoDTO(Publicacion publicacion) {
        super(publicacion);
        this.contenido = publicacion.getContenido();
    }

}
