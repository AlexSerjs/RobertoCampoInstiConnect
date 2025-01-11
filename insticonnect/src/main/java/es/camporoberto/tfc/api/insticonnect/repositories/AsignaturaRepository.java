package es.camporoberto.tfc.api.insticonnect.repositories;

import es.camporoberto.tfc.api.insticonnect.entidades.Asignatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AsignaturaRepository extends JpaRepository<Asignatura, Integer> {

    List<Asignatura> findByGrupoId(Integer grupoId);

    Optional<Asignatura> findByEmail(String email);
}
