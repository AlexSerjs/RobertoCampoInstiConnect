package es.camporoberto.tfc.api.insticonnect.repositories;

import es.camporoberto.tfc.api.insticonnect.entidades.RecuperacionClave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecuperacionClaveRepository extends JpaRepository<RecuperacionClave, Long> {
    Optional<RecuperacionClave> findByEmailAndCodigoRecuperacion(String email, String codigoRecuperacion);
}
