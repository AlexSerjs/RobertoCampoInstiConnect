package es.camporoberto.tfc.api.insticonnect.controllers;

import es.camporoberto.tfc.api.insticonnect.dtos.AlumnoInfoDTO;
import es.camporoberto.tfc.api.insticonnect.entidades.Alumno;
import es.camporoberto.tfc.api.insticonnect.repositories.AlumnoRepository;
import es.camporoberto.tfc.api.insticonnect.services.AlumnoService;
import es.camporoberto.tfc.api.insticonnect.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/alumnos")
public class PerfilAlumnoController {

    private final AlumnoRepository alumnoRepository;
    private final JwtUtil jwtUtil;
    private final AlumnoService alumnoService;

    @Autowired
    public PerfilAlumnoController(AlumnoRepository alumnoRepository, JwtUtil jwtUtil, AlumnoService alumnoService) {
        this.alumnoRepository = alumnoRepository;
        this.jwtUtil = jwtUtil;
        this.alumnoService = alumnoService;
    }

    @GetMapping("/info")
    public ResponseEntity<?> getAlumnoInfo(@RequestHeader("Authorization") String token) {
        // Eliminamos el prefijo "Bearer " del token
        token = token.replace("Bearer ", "");

        // Extraer el correo electrónico del token
        String email = jwtUtil.extractUsername(token);

        // Buscar la información del alumno por correo
        Optional<AlumnoInfoDTO> alumnoInfo = alumnoRepository.findAlumnoInfoByEmail(email);

        if (alumnoInfo.isPresent()) {
            return ResponseEntity.ok(alumnoInfo.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Alumno no encontrado");
        }
    }


    @GetMapping("/puede-publicar")
    public ResponseEntity<Map<String, Boolean>> verificarPermisoPublicar(@RequestHeader("Authorization") String token) {
        try {
            // Eliminar el prefijo "Bearer " y extraer el email del token
            token = token.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(token);

            // Obtener el alumno por su email usando el servicio
            Alumno alumno = alumnoService.findByEmail(email).orElse(null);
            if (alumno == null) {
                return ResponseEntity.badRequest().body(Map.of("error", false));  // Cambiado de String a Boolean
            }

            // Verificar si el alumno tiene permiso de publicar
            boolean puedePublicar = alumno.getPuedePublicar();

            // Devolver la respuesta
            return ResponseEntity.ok(Map.of("puedePublicar", puedePublicar));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", false));  // Cambiado de String a Boolean
        }
    }


}
