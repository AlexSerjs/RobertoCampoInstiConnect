package es.camporoberto.tfc.api.insticonnect.repositories;

import es.camporoberto.tfc.api.insticonnect.entidades.CicloFormativo;
import es.camporoberto.tfc.api.insticonnect.entidades.Instituto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutoRepository extends JpaRepository<Instituto, Integer> {

    @Query("SELECT i FROM Instituto i JOIN FETCH i.ciclosFormativos cf WHERE i.nombre = :nombre AND cf.nombre = :cicloNombre AND cf.anio = :anio")
    Optional<Instituto> findFirstInstitutoByNombreAndCiclo(
            @Param("nombre") String nombre,
            @Param("cicloNombre") String cicloNombre,
            @Param("anio") CicloFormativo.Anio anio);

    Optional<Instituto> findFirstByNombre(String nombre);

    @Query("SELECT i FROM Instituto i WHERE i.comunidadAutonoma.nombre = :nombre")
    List<Instituto> findByComunidadAutonomaNombre(@Param("nombre") String nombre);

    Optional<Instituto> findByNombre(String nombre);
    List<Instituto> findByComunidadAutonomaId(Integer comunidadId);
}
