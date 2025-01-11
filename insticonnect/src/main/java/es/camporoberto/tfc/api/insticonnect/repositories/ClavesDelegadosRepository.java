package es.camporoberto.tfc.api.insticonnect.repositories;

import es.camporoberto.tfc.api.insticonnect.entidades.ClavesDelegados;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClavesDelegadosRepository extends JpaRepository<ClavesDelegados, Integer> {
    boolean existsByClave(String clave);

    Optional<ClavesDelegados> findByClave(String clave);
}
