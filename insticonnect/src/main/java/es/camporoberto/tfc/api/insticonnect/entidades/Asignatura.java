package es.camporoberto.tfc.api.insticonnect.entidades;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "asignaturas")
public class Asignatura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    private String nombre;
    private String profesor;
    private String email;


}
