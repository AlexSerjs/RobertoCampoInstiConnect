package es.camporoberto.tfc.api.insticonnect.services;

import es.camporoberto.tfc.api.insticonnect.dtos.GrupoDTO;
import es.camporoberto.tfc.api.insticonnect.entidades.Grupo;
import es.camporoberto.tfc.api.insticonnect.repositories.GrupoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class GrupoService {


    private final GrupoRepository grupoRepository;

    @Autowired
    public GrupoService(GrupoRepository grupoRepository) {
        this.grupoRepository = grupoRepository;
    }

    public void eliminarGrupoSiNoHayAlumnos(int grupoId) {
        // Verificar si no hay alumnos en el grupo
        int alumnoCount = grupoRepository.countByCursoId(grupoId);
        if (alumnoCount == 0) {
            grupoRepository.deleteById(grupoId);
        }
    }

    public void eliminarGrupo(Grupo grupo) {
        grupoRepository.delete(grupo);
    }

    public Grupo guardarGrupo(Grupo grupo) {
        return grupoRepository.save(grupo);
    }

    public List<Map<String, Object>> getGroupDetails() {
        return grupoRepository.getGroupDetails();
    }



    public List<GrupoDTO> getGruposByInstitutoId(Integer institutoId) {
        List<Grupo> grupos = grupoRepository.findByInstitutoId(institutoId);

        return grupos.stream()
                .map(grupo -> new GrupoDTO(
                        grupo.getId(),
                        grupo.getCodigoGrupo(),
                        grupo.getCurso().getCiclo().getNombre(), // Nombre del curso (del ciclo formativo)
                        grupo.getDelegado() != null ? grupo.getDelegado().getNombreCompleto() : "Sin Delegado"
                ))
                .collect(Collectors.toList());
    }
}
