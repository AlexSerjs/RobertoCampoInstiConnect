package es.camporoberto.tfc.api.insticonnect.services;

import es.camporoberto.tfc.api.insticonnect.dtos.CursoDTO;
import es.camporoberto.tfc.api.insticonnect.entidades.Curso;
import es.camporoberto.tfc.api.insticonnect.entidades.CicloFormativo;
import es.camporoberto.tfc.api.insticonnect.repositories.CursoRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.InstitutoRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.CicloFormativoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CursoService {

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private InstitutoRepository institutoRepository;

    @Autowired
    private CicloFormativoRepository cicloFormativoRepository;

    public Optional<Curso> findCursoByDetails(Integer institutoId, Integer cicloId, String anioLectivo, CicloFormativo.Anio anio) {
        return cursoRepository.findByInstitutoIdAndCicloIdAndAnioLectivoAndAnio(
                institutoId, cicloId, anioLectivo, anio
        );
    }

    public List<CursoDTO> getCursosByInstitutoId(Integer institutoId) {
        List<Curso> cursos = cursoRepository.findByInstitutoId(institutoId);

        // Mapear la lista de entidades Curso a CursoDTO
        return cursos.stream()
                .map(curso -> new CursoDTO(
                        curso.getId(),
                        curso.getCiclo().getNombre(),
                        curso.getCiclo().getNivel(),
                        curso.getAnioLectivo(),
                        curso.getAnio().toString()
                ))
                .toList();
    }




}
