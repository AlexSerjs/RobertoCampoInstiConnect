package es.camporoberto.tfc.api.insticonnect.repositories;

import es.camporoberto.tfc.api.insticonnect.entidades.ComunidadAutonoma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComunidadAutonomaRepository extends JpaRepository<ComunidadAutonoma, Integer> {
    Optional<ComunidadAutonoma> findByNombre(String nombre);
    Optional<ComunidadAutonoma> findByCodigoPrefijo(String codigoPrefijo);
    List<ComunidadAutonoma> findAll();

}