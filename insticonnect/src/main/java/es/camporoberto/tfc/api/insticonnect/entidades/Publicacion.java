package es.camporoberto.tfc.api.insticonnect.entidades;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "publicaciones")
@Data
public class Publicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;  // Identificador único para la publicación

    @Column(nullable = false, length = 100)
    private String titulo;  // Título de la publicación

    @Column(nullable = true)
    private String contenido;  // El contenido de la publicación
    // El contenido de la publicación

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;  // Fecha de creación de la publicación

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Alumno usuario;  // Relación con el alumno que realizó la publicación

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contenido", nullable = false)
    private TipoContenido tipoContenido = TipoContenido.texto;
    // Tipo de contenido de la publicación

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;  // URL de la imagen (si existe)

    @Column(name = "archivo_url", length = 255)
    private String archivoUrl;  // URL del archivo (si existe)

    @Lob
    @Column(name = "encuesta")
    private String encuesta;  // Datos de la encuesta (si existe)

    @Column(name = "comentarios_count", nullable = false, columnDefinition = "int default 0")
    private int comentariosCount;  // Contador de comentarios asociados

    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;  // Relación con el grupo al que pertenece la publicación

    @OneToMany(mappedBy = "encuesta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpcionEncuesta> opciones = new ArrayList<>();

    // Enum para tipo de contenido
    public enum TipoContenido {
        texto,
        imagen,
        archivo,
        encuesta,
        mixto,
        comunicado
    }

    @OneToMany(mappedBy = "publicacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Comentarios> comentarios;



}
