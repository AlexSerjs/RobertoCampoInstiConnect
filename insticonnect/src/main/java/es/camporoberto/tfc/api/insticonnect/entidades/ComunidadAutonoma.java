package es.camporoberto.tfc.api.insticonnect.entidades;



import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "comunidad_autonoma")
@Data
public class ComunidadAutonoma {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @Column(nullable = false, length = 100)
        private String nombre;

        @Column(name = "codigo_prefijo", nullable = false, length = 5)
        private String codigoPrefijo;

        @Column(name = "dominio_correo", nullable = false, length = 50)
        private String dominioCorreo;

        // Relación con Institutos
        @OneToMany(mappedBy = "comunidadAutonoma", fetch = FetchType.LAZY)
        @JsonManagedReference
        private List<Instituto> institutos;


        @Override
        public String toString() {
                return "ComunidadAutonoma{" +
                        "id=" + id +
                        ", nombre='" + nombre + '\'' +
                        ", codigoPrefijo='" + codigoPrefijo + '\'' +
                        '}';
        }
}

