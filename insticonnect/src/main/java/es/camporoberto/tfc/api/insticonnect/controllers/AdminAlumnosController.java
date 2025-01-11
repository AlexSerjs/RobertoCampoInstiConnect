package es.camporoberto.tfc.api.insticonnect.controllers;

import es.camporoberto.tfc.api.insticonnect.dtos.AlumnoDetailsDTO;
import es.camporoberto.tfc.api.insticonnect.entidades.*;
import es.camporoberto.tfc.api.insticonnect.repositories.AlumnoRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.GrupoRepository;
import es.camporoberto.tfc.api.insticonnect.services.AlumnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/alumnos")
public class AdminAlumnosController {

    private final AlumnoService alumnoService;
    private final AlumnoRepository alumnoRepository;
    private final GrupoRepository grupoRepository;

    @Autowired
    public AdminAlumnosController(AlumnoService alumnoService, AlumnoRepository alumnoRepository, GrupoRepository grupoRepository) {
        this.alumnoService = alumnoService;
        this.alumnoRepository = alumnoRepository;
        this.grupoRepository = grupoRepository;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAlumnosList() {
        try {
            List<Map<String, Object>> alumnosList = alumnoService.getAllAlumnos().stream()
                    .map(alumno -> {
                        Map<String, Object> alumnoMap = new HashMap<>();
                        alumnoMap.put("id", alumno.getId());
                        alumnoMap.put("nombreCompleto", alumno.getNombreCompleto());
                        return alumnoMap;
                    })
                    .toList();

            return ResponseEntity.ok(alumnosList);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/detalles/{id}")
    public ResponseEntity<?> getAlumnoDetailsById(@PathVariable Integer id) {
        try {
            Optional<Alumno> alumnoOptional = alumnoRepository.findById(id);

            if (alumnoOptional.isPresent()) {
                Alumno alumno = alumnoOptional.get();

                Grupo grupo = alumno.getGrupo();
                Curso curso = grupo.getCurso();
                CicloFormativo ciclo = curso.getCiclo();
                Instituto instituto = curso.getInstituto();

                AlumnoDetailsDTO alumnoDetailsDTO = new AlumnoDetailsDTO(
                        alumno.getId(),
                        alumno.getNombreCompleto(),
                        grupo.getCodigoGrupo(),
                        alumno.getEmail(),
                        alumno.getTipo().toString(),
                        alumno.getIsVerified(),
                        alumno.getPuedePublicar(),
                        alumno.getFechaCreacion(),
                        ciclo.getNombre(),
                        ciclo.getNivel(),
                        ciclo.getAnio().toString(),
                        curso.getAnioLectivo(),
                        instituto.getNombre(),
                        instituto.getComunidadAutonoma().getNombre()
                );

                return ResponseEntity.ok(alumnoDetailsDTO);
            } else {
                return ResponseEntity.status(404).body(Map.of("error", "Alumno no encontrado"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarAlumno(@PathVariable Integer id) {
        System.out.println("Solicitud DELETE recibida para el ID: " + id);

        try {
            Optional<Alumno> alumnoOptional = alumnoRepository.findById(id);

            if (alumnoOptional.isEmpty()) {
                System.out.println("Alumno no encontrado con ID: " + id);
                return ResponseEntity.status(404).body(Map.of("error", "Alumno no encontrado"));
            }

            Alumno alumno = alumnoOptional.get();
            System.out.println("Alumno encontrado: " + alumno.getNombreCompleto());

            if (esDelegado(alumno.getId())) {
                System.out.println("El alumno es delegado, eliminando grupo asociado.");
                Grupo grupo = grupoRepository.findByDelegadoId(alumno.getId())
                        .orElseThrow(() -> new IllegalStateException("Grupo no encontrado para el delegado"));

                alumnoRepository.deleteByGrupoId(grupo.getId());
                grupoRepository.deleteById(grupo.getId());
                alumnoRepository.deleteById(alumno.getId());

                System.out.println("Delegado y su grupo eliminados exitosamente.");
                return ResponseEntity.ok(Map.of(
                        "message", "El delegado y su grupo han sido eliminados exitosamente",
                        "isDelegado", true
                ));
            } else {
                System.out.println("El alumno no es delegado, eliminándolo directamente.");
                alumnoRepository.deleteById(id);
                return ResponseEntity.ok(Map.of(
                        "message", "Alumno eliminado exitosamente",
                        "isDelegado", false
                ));
            }
        } catch (Exception e) {
            System.out.println("Error en el servidor: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }



    private boolean esDelegado(Integer id) {
        // Verificar si existe un grupo donde el delegado_id coincide con el ID del alumno

        return grupoRepository.existsByDelegadoId(id);
    }



}
