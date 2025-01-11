package es.camporoberto.tfc.api.insticonnect.services;

import es.camporoberto.tfc.api.insticonnect.entidades.Curso;
import es.camporoberto.tfc.api.insticonnect.entidades.Instituto;
import es.camporoberto.tfc.api.insticonnect.repositories.CursoRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.InstitutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class InstitutoService {

    private final CursoRepository cursoRepository;
    private final InstitutoRepository institutoRepository;

    public InstitutoService(CursoRepository cursoRepository, InstitutoRepository institutoRepository) {
        this.cursoRepository = cursoRepository;
        this.institutoRepository = institutoRepository;
    }


    public List<Instituto> getInstitutosByComunidad(String nombreComunidad) {
        return institutoRepository.findByComunidadAutonomaNombre(nombreComunidad);
    }

    public List<Instituto> getInstitutosByComunidad(Integer comunidadId) {
        return institutoRepository.findByComunidadAutonomaId(comunidadId);
    }


    public Optional<Instituto> findById(Integer id) {
        return institutoRepository.findById(id);
    }
}


