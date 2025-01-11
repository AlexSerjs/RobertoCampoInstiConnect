package es.camporoberto.tfc.api.insticonnect.repositories;

import es.camporoberto.tfc.api.insticonnect.entidades.OpcionEncuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface OpcionEncuestaRepository extends JpaRepository<OpcionEncuesta, Integer> {

    List<OpcionEncuesta> findByEncuestaId(int id);
}
