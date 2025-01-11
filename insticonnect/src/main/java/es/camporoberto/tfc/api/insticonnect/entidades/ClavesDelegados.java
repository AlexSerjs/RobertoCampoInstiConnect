package es.camporoberto.tfc.api.insticonnect.entidades;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "claves_delegados")
@Data
public class ClavesDelegados {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String clave;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comunidad_id", nullable = false)
    private ComunidadAutonoma comunidadAutonoma;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instituto_id", nullable = false)
    private Instituto instituto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrador_id", nullable = false)
    private Administrador administrador;

    @Column(nullable = false)
    private Boolean estado; // Estado de la clave: usada o no

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_utilizacion")
    private LocalDateTime fechaUtilizacion;



}
