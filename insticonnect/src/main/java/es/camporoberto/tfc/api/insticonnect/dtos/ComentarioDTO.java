package es.camporoberto.tfc.api.insticonnect.dtos;

import es.camporoberto.tfc.api.insticonnect.entidades.Comentarios;
import lombok.Data;

import java.util.Date;

@Data
public class ComentarioDTO {
    private int id;
    private String contenido;
    private Date fechaCreacion;
    private String autorNombre;
    private Integer publicacionId;

    public ComentarioDTO(Comentarios comentario) {
        this.id = comentario.getId();
        this.contenido = comentario.getContenido();
        this.fechaCreacion = comentario.getFechaCreacion();
        this.autorNombre = comentario.getAlumno().getNombreCompleto();
        this.publicacionId = comentario.getPublicacion().getId();
    }

    public ComentarioDTO() {
    }
}
