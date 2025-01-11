package es.camporoberto.tfc.api.insticonnect.repositories;

import es.camporoberto.tfc.api.insticonnect.entidades.Voto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VotosRepository extends JpaRepository<Voto, Integer> {
    boolean existsByAlumnoIdAndEncuestaId(Integer alumnoId, Integer encuestaId);
    Optional<Voto> findByAlumnoIdAndEncuestaId(Integer alumnoId, Integer encuestaId);
}
