package es.camporoberto.tfc.api.insticonnect.services;

import es.camporoberto.tfc.api.insticonnect.entidades.CicloFormativo;
import es.camporoberto.tfc.api.insticonnect.entidades.Instituto;
import es.camporoberto.tfc.api.insticonnect.repositories.CicloFormativoRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.InstitutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CicloFormativoService {

    @Autowired
    private CicloFormativoRepository cicloFormativoRepository;

    @Autowired
    private InstitutoRepository institutoRepository;

    public List<CicloFormativo> getCiclosFormativosByInstitutoNombre(String institutoNombre) {
        Optional<Instituto> instituto = institutoRepository.findByNombre(institutoNombre);
        if (instituto.isPresent()) {
            return cicloFormativoRepository.findByInstituto(instituto.get());
        }
        return List.of();
    }
}
