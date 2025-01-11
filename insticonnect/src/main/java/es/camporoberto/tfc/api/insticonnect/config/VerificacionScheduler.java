package es.camporoberto.tfc.api.insticonnect.config;

import es.camporoberto.tfc.api.insticonnect.entidades.Alumno;
import es.camporoberto.tfc.api.insticonnect.entidades.Grupo;
import es.camporoberto.tfc.api.insticonnect.services.AlumnoService;
import es.camporoberto.tfc.api.insticonnect.services.GrupoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class VerificacionScheduler {

    private final AlumnoService alumnoService;
    private final GrupoService grupoService;

    public VerificacionScheduler(AlumnoService alumnoService, GrupoService grupoService) {
        this.alumnoService = alumnoService;
        this.grupoService = grupoService;
    }

    @Scheduled(fixedRate = 3600000) // Cada hora
    public void eliminarAlumnosNoVerificados() {
        List<Alumno> alumnosNoVerificados = alumnoService.findAllNoVerificados();

        for (Alumno alumno : alumnosNoVerificados) {
            try {
                // Verificar si el alumno está asociado a un grupo
                Grupo grupo = alumno.getGrupo();
                if (grupo != null) {
                    // Si el alumno es delegado, eliminar la referencia en el grupo
                    if (alumno.getTipo() == Alumno.Tipo.delegado) {
                        grupo.setDelegado(null);
                        grupoService.guardarGrupo(grupo);
                        log.info("Delegado del grupo con ID {} eliminado.", grupo.getId());

                        // Verificar si el grupo tiene más alumnos
                        List<Alumno> alumnosGrupo = alumnoService.findByGrupo(grupo);
                        if (alumnosGrupo.isEmpty()) {
                            // Eliminar el grupo si no hay más alumnos
                            grupoService.eliminarGrupo(grupo);
                            log.info("Grupo con ID {} eliminado porque no tenía más alumnos.", grupo.getId());
                        }
                    }
                }
                // Eliminar el alumno
                alumnoService.eliminarAlumno(alumno);
                log.info("Alumno con ID {} eliminado por no verificar su correo dentro del tiempo límite.", alumno.getId());

            } catch (Exception e) {
                log.error("Error al eliminar el alumno con ID {}: {}", alumno.getId(), e.getMessage());
            }
        }
    }
}
