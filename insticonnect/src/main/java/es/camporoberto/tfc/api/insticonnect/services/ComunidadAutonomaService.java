package es.camporoberto.tfc.api.insticonnect.services;

import es.camporoberto.tfc.api.insticonnect.entidades.ComunidadAutonoma;
import es.camporoberto.tfc.api.insticonnect.repositories.ComunidadAutonomaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ComunidadAutonomaService {

    private final ComunidadAutonomaRepository comunidadAutonomaRepository;

    @Autowired
    public ComunidadAutonomaService(ComunidadAutonomaRepository comunidadAutonomaRepository) {
        this.comunidadAutonomaRepository = comunidadAutonomaRepository;
    }

    public List<ComunidadAutonoma> getAllComunidades() {
        return comunidadAutonomaRepository.findAll();
    }

    public String getNombreById(Integer id) {
        ComunidadAutonoma comunidad = comunidadAutonomaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comunidad Autónoma no encontrada"));
        return comunidad.getNombre();
    }

    public Optional<ComunidadAutonoma> findById(Integer id) {
        return comunidadAutonomaRepository.findById(id);
    }

}


