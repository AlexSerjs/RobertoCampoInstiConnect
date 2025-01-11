package es.camporoberto.tfc.api.insticonnect.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.camporoberto.tfc.api.insticonnect.dtos.AsignaturaDTO;
import es.camporoberto.tfc.api.insticonnect.dtos.IntegranteDTO;
import es.camporoberto.tfc.api.insticonnect.entidades.Alumno;
import es.camporoberto.tfc.api.insticonnect.entidades.Asignatura;
import es.camporoberto.tfc.api.insticonnect.entidades.Grupo;
import es.camporoberto.tfc.api.insticonnect.handler.AsignaturasWebSocketHandler;
import es.camporoberto.tfc.api.insticonnect.handler.PermisoWebSocketHandler;
import es.camporoberto.tfc.api.insticonnect.repositories.AlumnoRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.AsignaturaRepository;
import es.camporoberto.tfc.api.insticonnect.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/grupo")
public class PerfilDelegadoController {

    private final AlumnoRepository alumnoRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final JwtUtil jwtUtil;
    private final AsignaturasWebSocketHandler asignaturasWebSocketHandler;
    private final PermisoWebSocketHandler permisoWebSocketHandler;


    @Autowired
    public PerfilDelegadoController(AlumnoRepository alumnoRepository, AsignaturaRepository asignaturaRepository, JwtUtil jwtUtil, AsignaturasWebSocketHandler asignaturasWebSocketHandler, PermisoWebSocketHandler permisoWebSocketHandler) {
        this.alumnoRepository = alumnoRepository;
        this.asignaturaRepository = asignaturaRepository;
        this.jwtUtil = jwtUtil;
        this.asignaturasWebSocketHandler = asignaturasWebSocketHandler;
        this.permisoWebSocketHandler = permisoWebSocketHandler;
    }

    @GetMapping("/integrantes")
    public ResponseEntity<List<IntegranteDTO>> getIntegrantes(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);

        Alumno delegado = alumnoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Delegado no encontrado"));

        Grupo grupo = delegado.getGrupo();
        if (grupo == null) {
            return ResponseEntity.badRequest().body(null);
        }

        List<IntegranteDTO> integrantes = alumnoRepository.findByGrupo(grupo).stream()
                .map(alumno -> new IntegranteDTO(
                        alumno.getId(),
                        alumno.getNombreCompleto(),
                        alumno.getEmail(),
                        alumno.getIsVerified(),
                        alumno.getPuedePublicar(),
                        alumno.getTipo().toString()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(integrantes);
    }


    @PutMapping("/integrantes/{id}/publicar")
    public ResponseEntity<String> actualizarPermisoPublicacion(
            @PathVariable Integer id,
            @RequestParam Boolean puedePublicar) {

        Optional<Alumno> alumnoOpt = alumnoRepository.findById(id);

        if (alumnoOpt.isPresent()) {
            Alumno alumno = alumnoOpt.get();
            alumno.setPuedePublicar(puedePublicar);
            alumnoRepository.save(alumno);

            // Crear un JSON con los detalles de la actualización
            Map<String, Object> mensaje = new HashMap<>();
            mensaje.put("tipo", "ACTUALIZACION_PERMISO");
            mensaje.put("mensaje", "Permiso actualizado para: " + alumno.getNombreCompleto());
            mensaje.put("alumnoId", alumno.getId());
            mensaje.put("puedePublicar", puedePublicar);

            try {
                String jsonMensaje = new ObjectMapper().writeValueAsString(mensaje);
                permisoWebSocketHandler.notifyClients(jsonMensaje);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al enviar notificación");
            }

            return ResponseEntity.ok("Permiso de publicación actualizado correctamente");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Alumno no encontrado");
        }
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------
    @PostMapping("/agregarAsignaturas")
    public ResponseEntity<String> addAsignatura(
            @RequestHeader("Authorization") String token,
            @RequestBody AsignaturaDTO asignaturaDTO) {
        token = token.replace("Bearer ", "");
        System.out.println("Token recibido: " + token);
        String email = jwtUtil.extractUsername(token);
        System.out.println("Email extraído del token: " + email);

        Alumno delegado = alumnoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Delegado no encontrado"));

        Grupo grupo = delegado.getGrupo();
        if (grupo == null) {
            System.out.println("Grupo no encontrado para el delegado");
            return ResponseEntity.badRequest().body("Grupo no encontrado");
        }

        Asignatura asignatura = new Asignatura();
        asignatura.setGrupo(grupo);
        asignatura.setNombre(asignaturaDTO.getNombre());
        asignatura.setProfesor(asignaturaDTO.getProfesor());
        asignatura.setEmail(asignaturaDTO.getEmail());

        asignaturaRepository.save(asignatura);

        // Notificar a los clientes sobre la nueva asignatura
        asignaturasWebSocketHandler.notifyClients("NUEVA_ASIGNATURA", "Se ha agregado una nueva asignatura: " + asignaturaDTO.getNombre());

        return ResponseEntity.ok("Asignatura agregada correctamente");
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------
    @GetMapping("/asignaturas")
    public ResponseEntity<List<AsignaturaDTO>> getAsignaturas(
            @RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);

        Alumno delegado = alumnoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Delegado no encontrado"));

        Grupo grupo = delegado.getGrupo();
        if (grupo == null) {
            return ResponseEntity.badRequest().body(null);
        }

        List<AsignaturaDTO> asignaturas = asignaturaRepository.findByGrupoId(grupo.getId()).stream()
                .map(asignatura -> new AsignaturaDTO(
                        asignatura.getId(),
                        asignatura.getNombre(),
                        asignatura.getProfesor(),
                        asignatura.getEmail()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(asignaturas);
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------
    // Eliminar asignatura
    @DeleteMapping("/eliminarAsignaturas")
    public ResponseEntity<String> eliminarAsignaturaPorId(@RequestParam Integer id) {
        Optional<Asignatura> asignatura = asignaturaRepository.findById(id);

        if (asignatura.isPresent()) {
            String nombreAsignatura = asignatura.get().getNombre();
            asignaturaRepository.delete(asignatura.get());

            asignaturasWebSocketHandler.notifyClients("ELIMINAR_ASIGNATURA", "Se ha eliminado la asignatura: " + nombreAsignatura);


            return ResponseEntity.ok("Asignatura eliminada correctamente");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Asignatura no encontrada");
        }
    }


    //---------------------------------------------------------------------------------------------------------------------------------------------------------------

    // Endpoint para actualizar una asignatura por ID
    @PutMapping("/actualizarAsignatura/{id}")
    public ResponseEntity<String> actualizarAsignatura(
            @PathVariable Integer id,
            @RequestBody AsignaturaDTO asignaturaDTO) {

        Optional<Asignatura> asignaturaOpt = asignaturaRepository.findById(id);

        if (asignaturaOpt.isPresent()) {
            Asignatura asignatura = asignaturaOpt.get();
            asignatura.setNombre(asignaturaDTO.getNombre());
            asignatura.setProfesor(asignaturaDTO.getProfesor());
            asignatura.setEmail(asignaturaDTO.getEmail());

            asignaturaRepository.save(asignatura);
            asignaturasWebSocketHandler.notifyClients("ACTUALIZACION_ASIGNATURA", "Se ha actualizado la asignatura: " + asignaturaDTO.getNombre());

            return ResponseEntity.ok("Asignatura actualizada correctamente");
        } else {
            return ResponseEntity.status(404).body("Asignatura no encontrada");
        }
    }


    //---------------------------------------------------------------------------------------------------------------------------------------------------------------
}
