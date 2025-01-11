package es.camporoberto.tfc.api.insticonnect.dtos;

import es.camporoberto.tfc.api.insticonnect.entidades.Publicacion;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class PublicacionDTO {
    private int id;
    private String titulo;
    private String contenido;
    private String tipoContenido;
    private String imagenUrl;
    private String archivoUrl;
    private Date fechaCreacion;
    private String autor;
    private String grupo;
    private List<ComentarioDTO> comentarios = List.of(); // Lista vacía por defecto
    private List<OpcionEncuestaDTO> opcionesEncuesta = List.of(); // Lista vacía por defecto
    private boolean yaVotado = false; // Indica si el alumno ya votó en esta encuesta
    private OpcionEncuestaDTO opcionSeleccionada; // La opción votada por el alumno, si ya votó

    public PublicacionDTO(Publicacion publicacion) {
        this.id = publicacion.getId();
        this.titulo = publicacion.getTitulo();
        this.contenido = publicacion.getContenido();
        this.tipoContenido = publicacion.getTipoContenido().name();
        this.imagenUrl = publicacion.getImagenUrl();
        this.archivoUrl = publicacion.getArchivoUrl();
        this.fechaCreacion = publicacion.getFechaCreacion();
        this.autor = publicacion.getUsuario().getNombreCompleto();
        this.grupo = publicacion.getGrupo() != null ? publicacion.getGrupo().getCodigoGrupo() : null;

        if (publicacion.getComentarios() != null) {
            this.comentarios = publicacion.getComentarios().stream()
                    .map(ComentarioDTO::new)
                    .collect(Collectors.toList());
        }

        if ("encuesta".equalsIgnoreCase(publicacion.getTipoContenido().name()) && publicacion.getOpciones() != null) {
            this.opcionesEncuesta = publicacion.getOpciones().stream()
                    .map(OpcionEncuestaDTO::new)
                    .collect(Collectors.toList());
        }
    }

    public PublicacionDTO() {
    }
}
