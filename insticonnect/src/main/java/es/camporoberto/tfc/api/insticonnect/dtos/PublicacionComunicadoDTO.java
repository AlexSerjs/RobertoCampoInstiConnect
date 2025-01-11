package es.camporoberto.tfc.api.insticonnect.dtos;

import es.camporoberto.tfc.api.insticonnect.entidades.Publicacion;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class PublicacionComunicadoDTO extends PublicacionDTO {
    private String titulo;
    private String contenido;
    private String autorNombre;
    private Date fechaCreacion;
    private List<ComentarioDTO> comentarios;

    @JsonCreator
    public PublicacionComunicadoDTO(
            @JsonProperty("titulo") String titulo,
            @JsonProperty("autorNombre") String autorNombre,
            @JsonProperty("fechaCreacion") Date fechaCreacion,
            @JsonProperty("comentarios") List<ComentarioDTO> comentarios) {
        this.titulo = titulo;
        this.autorNombre = autorNombre;
        this.fechaCreacion = fechaCreacion;
        this.comentarios = comentarios;
    }

    public PublicacionComunicadoDTO(Publicacion publicacion) {
        super(publicacion);
        this.titulo = publicacion.getTitulo(); // El título también será la descripción
        this.autorNombre = publicacion.getUsuario().getNombreCompleto(); // Nombre del autor
        this.fechaCreacion = publicacion.getFechaCreacion(); // Fecha y hora de creación

        // Convertir los comentarios a DTOs, manejando el caso de lista null
        this.comentarios = publicacion.getComentarios() != null
                ? publicacion.getComentarios().stream().map(ComentarioDTO::new).collect(Collectors.toList())
                : new ArrayList<>();
    }

}

