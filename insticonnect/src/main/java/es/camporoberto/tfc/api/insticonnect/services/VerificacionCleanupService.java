package es.camporoberto.tfc.api.insticonnect.services;

import es.camporoberto.tfc.api.insticonnect.entidades.Alumno;
import es.camporoberto.tfc.api.insticonnect.entidades.Grupo;
import es.camporoberto.tfc.api.insticonnect.repositories.AlumnoRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.GrupoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VerificacionCleanupService {

    private final AlumnoRepository alumnoRepository;
    private final GrupoRepository grupoRepository;

    @Autowired
    public VerificacionCleanupService(AlumnoRepository alumnoRepository, GrupoRepository grupoRepository) {
        this.alumnoRepository = alumnoRepository;
        this.grupoRepository = grupoRepository;
    }

    // Esta tarea se ejecuta cada hora para eliminar alumnos no verificados
    @Scheduled(fixedRate = 3600000) // 1 hora
    public void eliminarAlumnosNoVerificados() {
        LocalDateTime unaHoraAntes = LocalDateTime.now().minusHours(1);
        List<Alumno> alumnosNoVerificados = alumnoRepository.findByIsVerifiedFalseAndTipoAndFechaCreacionBefore(Alumno.Tipo.delegado, unaHoraAntes);

        for (Alumno alumno : alumnosNoVerificados) {
            Grupo grupo = alumno.getGrupo();
            if (grupo != null) {
                // Verificar si no hay otros alumnos en el grupo
                List<Alumno> alumnosDelGrupo = alumnoRepository.findByGrupo(grupo);
                if (alumnosDelGrupo.size() == 1 && alumnosDelGrupo.get(0).getId().equals(alumno.getId())) {
                    // Si solo tiene al delegado, eliminar el grupo
                    grupoRepository.delete(grupo);
                }
            }
            // Elimina al alumno no verificado
            alumnoRepository.delete(alumno);
        }
    }
}
