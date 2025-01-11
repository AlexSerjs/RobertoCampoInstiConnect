package es.camporoberto.tfc.api.insticonnect.entidades;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "alumnos")
@Data
public class Alumno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombreCompleto;

    @Column(length = 255)
    private String foto;

    @Lob
    @Column(name = "foto_almacenada")
    private byte[] fotoAlmacenada;  // Imagen en formato binario

    @Column(length = 10)
    private String codigoGrupo;

    @Column(name = "puede_publicar", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean puedePublicar = false;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column(name = "clave", nullable = false, length = 255)
    private String clave;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo = Tipo.alumno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "grupo_id")
    private Grupo grupo;

    public void setGrupo(Grupo grupo) {
        this.grupo = grupo;
        if (grupo != null) {
            this.codigoGrupo = grupo.getCodigoGrupo();
        }
    }


    public enum Tipo {
        delegado,
        alumno
    }

    @Getter
    @Setter
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }


    @Setter
    @Getter
    @Column(name = "intentos_verificacion", columnDefinition = "int default 0")
    private int intentosVerificacion; // Nueva variable para contar los intentos


    @OneToMany(mappedBy = "alumno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comentarios> comentarios;

}
