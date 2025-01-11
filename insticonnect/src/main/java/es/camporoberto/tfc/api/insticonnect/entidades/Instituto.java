package es.camporoberto.tfc.api.insticonnect.entidades;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "institutos")
@Data
public class Instituto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    // Relación con ComunidadAutonoma
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comunidad_autonoma_id", nullable = false)
    @JsonBackReference // Rompe el ciclo al serializar
    private ComunidadAutonoma comunidadAutonoma;

    // Relación con CiclosFormativos
    @OneToMany(mappedBy = "instituto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CicloFormativo> ciclosFormativos;

    @Override
    public String toString() {
        return "Instituto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}
