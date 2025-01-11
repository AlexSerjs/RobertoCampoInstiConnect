package es.camporoberto.tfc.api.insticonnect.entidades;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cursos")
@Data
public class Curso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "ciclo_id", nullable = false)
    private CicloFormativo ciclo;

    @Column(name = "anio_lectivo", nullable = false, length = 9)
    private String anioLectivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CicloFormativo.Anio anio;

    @ManyToOne
    @JoinColumn(name = "instituto_id", nullable = false)
    private Instituto instituto;



}
