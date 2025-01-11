package es.camporoberto.tfc.api.insticonnect.repositories;

import es.camporoberto.tfc.api.insticonnect.entidades.CicloFormativo;
import es.camporoberto.tfc.api.insticonnect.entidades.Instituto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CicloFormativoRepository extends JpaRepository<CicloFormativo, Integer> {

     Optional<CicloFormativo> findFirstByNombreAndAnioAndInstitutoId(String nombre, CicloFormativo.Anio anio, Integer institutoId);
     List<CicloFormativo> findByInstituto(Instituto instituto);

}