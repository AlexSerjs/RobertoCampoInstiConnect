package es.camporoberto.tfc.api.insticonnect.repositories;

import es.camporoberto.tfc.api.insticonnect.entidades.Curso;
import es.camporoberto.tfc.api.insticonnect.entidades.CicloFormativo;
import es.camporoberto.tfc.api.insticonnect.entidades.Instituto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Integer> {

        @Query("SELECT c FROM Curso c WHERE c.instituto.id = :institutoId AND c.ciclo.id = :cicloId AND c.anioLectivo = :anioLectivo AND c.anio = :anio")
        Optional<Curso> findByInstitutoIdAndCicloIdAndAnioLectivoAndAnio(@Param("institutoId") Integer institutoId,
                                                                         @Param("cicloId") Integer cicloId,
                                                                         @Param("anioLectivo") String anioLectivo,
                                                                         @Param("anio") CicloFormativo.Anio anio);

        List<Curso> findByInstitutoId(Integer institutoId);
}


