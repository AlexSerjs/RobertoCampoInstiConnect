package es.camporoberto.tfc.api.insticonnect.entidades;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Time;

@Entity
@Data
@Table(name = "horario_clases")
public class HorarioClases {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @Column(nullable = false, length = 20)
    private String bloque;

    @Column(name = "horaInicio", nullable = true)
    private Time horaInicio = Time.valueOf("12:00:00");

    @Column(name = "horaFin", nullable = true)
    private Time horaFin = Time.valueOf("12:00:00");

    @Column(name = "lunes", length = 100, nullable = true)
    private String lunes = "Libre";

    @Column(name = "martes", length = 100, nullable = true)
    private String martes = "Libre";

    @Column(name = "miercoles", length = 100, nullable = true)
    private String miercoles = "Libre";

    @Column(name = "jueves", length = 100, nullable = true)
    private String jueves = "Libre";

    @Column(name = "viernes", length = 100, nullable = true)
    private String viernes = "Libre";
}
