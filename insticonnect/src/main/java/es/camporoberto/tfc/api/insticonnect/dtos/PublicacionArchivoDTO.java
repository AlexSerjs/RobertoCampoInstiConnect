package es.camporoberto.tfc.api.insticonnect.dtos;

import es.camporoberto.tfc.api.insticonnect.entidades.Publicacion;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PublicacionArchivoDTO extends PublicacionDTO {
    private String titulo;
    private String archivoUrl;
    private String autorNombre;
    private Date fechaCreacion;
    private List<ComentarioDTO> comentarios;

    public PublicacionArchivoDTO(Publicacion publicacion) {
        super(publicacion);
        this.titulo = publicacion.getTitulo(); // Título de la publicación
        this.archivoUrl = publicacion.getArchivoUrl(); // URL del archivo
        this.autorNombre = publicacion.getUsuario().getNombreCompleto(); // Nombre del autor
        this.fechaCreacion = publicacion.getFechaCreacion(); // Fecha y hora de creación

        // Convertir los comentarios a DTOs
        this.comentarios = publicacion.getComentarios().stream()
                .map(ComentarioDTO::new)
                .collect(Collectors.toList());
    }
}
