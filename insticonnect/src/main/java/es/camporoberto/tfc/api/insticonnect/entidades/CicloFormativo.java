package es.camporoberto.tfc.api.insticonnect.entidades;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ciclos_formativos")
@Data
public class CicloFormativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String nivel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Anio anio;

    @ManyToOne
    @JoinColumn(name = "instituto_id", nullable = false)
    private Instituto instituto;

    public enum Anio {
        primero,
        segundo
    }
}
