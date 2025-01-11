package es.camporoberto.tfc.api.insticonnect.entidades;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "votos")
@Data
public class Voto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer alumnoId;

    @Column(nullable = false)
    private Integer encuestaId;

    @Column(nullable = false)
    private Integer opcionId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaVoto = LocalDateTime.now();


}
