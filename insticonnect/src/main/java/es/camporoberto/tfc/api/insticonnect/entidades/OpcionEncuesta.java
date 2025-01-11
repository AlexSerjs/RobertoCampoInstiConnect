package es.camporoberto.tfc.api.insticonnect.entidades;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "opciones_encuesta")
@Data
public class OpcionEncuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String texto; // El texto de la opción

    @Column(nullable = false, columnDefinition = "int default 0")
    private int votos; // Número de votos para esta opción

    @ManyToOne
    @JoinColumn(name = "encuesta_id", nullable = false)
    private Publicacion encuesta; // Relación con la encuesta

    // Constructor vacío requerido por JPA
    public OpcionEncuesta() {}

    // Constructor personalizado
    public OpcionEncuesta(String texto, int votos, Publicacion encuesta) {
        this.texto = texto;
        this.votos = votos;
        this.encuesta = encuesta;
    }
}

