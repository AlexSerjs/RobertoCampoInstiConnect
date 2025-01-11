package es.camporoberto.tfc.api.insticonnect.dtos;


import es.camporoberto.tfc.api.insticonnect.entidades.Publicacion;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PublicacionImagenDTO extends PublicacionDTO {
    private String titulo;
    private String imagenUrl;
    private String autorNombre;
    private Date fechaCreacion;
    private List<ComentarioDTO> comentarios; // Lista de comentarios asociados

    public PublicacionImagenDTO(Publicacion publicacion) {
        super(publicacion); // Llama al constructor de la clase base
        this.titulo = publicacion.getTitulo();
        this.imagenUrl = publicacion.getImagenUrl();
        this.autorNombre = publicacion.getUsuario().getNombreCompleto();
        this.fechaCreacion = publicacion.getFechaCreacion();

        // Convertir los comentarios a DTO
        this.comentarios = publicacion.getComentarios().stream()
                .map(ComentarioDTO::new)
                .collect(Collectors.toList());
    }
}
