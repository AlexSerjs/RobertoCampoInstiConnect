package es.camporoberto.tfc.api.insticonnect.repositories;

import es.camporoberto.tfc.api.insticonnect.entidades.Grupo;
import es.camporoberto.tfc.api.insticonnect.entidades.HorarioClases;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HorarioClasesRepository extends JpaRepository<HorarioClases, Integer> {
    boolean existsByGrupoId(int grupoId);
    List<HorarioClases> findByGrupoId(int grupoId);
    Optional<HorarioClases> findByGrupoAndBloque(Grupo grupo, String bloque);


}
