package es.camporoberto.tfc.api.insticonnect.dtos;

import lombok.Data;

@Data
public class TextoPublicacionDTO extends BasePublicacionDTO {
    private String contenido; // Solo para publicaciones de tipo "texto"
}