package es.camporoberto.tfc.api.insticonnect.controllers;

import es.camporoberto.tfc.api.insticonnect.dtos.PublicacionComunicadoDTO;
import es.camporoberto.tfc.api.insticonnect.entidades.Alumno;
import es.camporoberto.tfc.api.insticonnect.entidades.Publicacion;
import es.camporoberto.tfc.api.insticonnect.handler.NotificacionWebSocketHandler;
import es.camporoberto.tfc.api.insticonnect.repositories.AlumnoRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.PublicacionRepository;
import es.camporoberto.tfc.api.insticonnect.services.PublicacionService;
import es.camporoberto.tfc.api.insticonnect.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/comunicados")
public class ComunicadoController {

    private final JwtUtil jwtUtil;
    private final AlumnoRepository alumnoRepository;
    private final PublicacionService publicacionService;
    private final NotificacionWebSocketHandler notificacionWebSocketHandler;

    public ComunicadoController(JwtUtil jwtUtil, AlumnoRepository alumnoRepository, PublicacionService publicacionService, NotificacionWebSocketHandler notificacionWebSocketHandler) {
        this.jwtUtil = jwtUtil;
        this.alumnoRepository = alumnoRepository;
        this.publicacionService = publicacionService;
        this.notificacionWebSocketHandler = notificacionWebSocketHandler;
    }

    @PostMapping("/comunicado")
    public ResponseEntity<Map<String, Object>> crearComunicado(
            @RequestHeader("Authorization") String token,
            @RequestBody PublicacionComunicadoDTO comunicadoDTO) {
        try {
            // Extraer el email del token
            String email = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            Optional<Alumno> alumnoOptional = alumnoRepository.findByEmail(email);

            if (alumnoOptional.isEmpty()) {
                return new ResponseEntity<>(Map.of("error", "Usuario no autorizado"), HttpStatus.UNAUTHORIZED);
            }

            Alumno alumno = alumnoOptional.get();

            if (alumno.getGrupo() == null) {
                return new ResponseEntity<>(Map.of("error", "El alumno no está asignado a un grupo"), HttpStatus.BAD_REQUEST);
            }

            // Crear una nueva publicación de tipo "comunicado"
            Publicacion comunicado = new Publicacion();
            comunicado.setTitulo(comunicadoDTO.getTitulo()); // Título del comunicado
            comunicado.setContenido(comunicadoDTO.getContenido());
            comunicado.setTipoContenido(Publicacion.TipoContenido.comunicado); // Tipo comunicado
            comunicado.setUsuario(alumno);// Asignar el autor
            comunicado.setGrupo(alumno.getGrupo()); // Asociar al grupo del alumno
            comunicado.setFechaCreacion(new Date()); // Fecha actual

            // Guardar el comunicado
            Publicacion comunicadoGuardado = publicacionService.guardar(comunicado);

            // Notificar a los clientes conectados
            notificacionWebSocketHandler.notifyClients(
                    "NUEVO_COMUNICADO",
                    String.format("Nuevo comunicado: %s por %s", comunicado.getTitulo(), alumno.getNombreCompleto())
            );

            return new ResponseEntity<>(Map.of("comunicado", new PublicacionComunicadoDTO(comunicadoGuardado)), HttpStatus.CREATED);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Map.of("error", "Error al crear el comunicado"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
