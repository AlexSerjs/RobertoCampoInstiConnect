package es.camporoberto.tfc.api.insticonnect.dtos;

import es.camporoberto.tfc.api.insticonnect.entidades.Publicacion;

public class PublicacionMixtoDTO extends PublicacionDTO {
    private String contenido;
    private String imagenUrl;
    private String archivoUrl;

    public PublicacionMixtoDTO(Publicacion publicacion) {
        super(publicacion);
        this.contenido = publicacion.getContenido();
        this.imagenUrl = publicacion.getImagenUrl();
        this.archivoUrl = publicacion.getArchivoUrl();
    }

    // Getters y setters
}
