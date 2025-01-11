package es.camporoberto.tfc.api.insticonnect.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.camporoberto.tfc.api.insticonnect.dtos.ComentarioDTO;
import es.camporoberto.tfc.api.insticonnect.entidades.Alumno;
import es.camporoberto.tfc.api.insticonnect.entidades.Comentarios;
import es.camporoberto.tfc.api.insticonnect.entidades.Publicacion;
import es.camporoberto.tfc.api.insticonnect.handler.NotificacionWebSocketHandler;
import es.camporoberto.tfc.api.insticonnect.repositories.AlumnoRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.ComentariosRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.PublicacionRepository;
import es.camporoberto.tfc.api.insticonnect.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comentarios")
public class ComentariosController {

    @Autowired
    private ComentariosRepository comentariosRepository;

    @Autowired
    private PublicacionRepository publicacionRepository;

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private NotificacionWebSocketHandler notificacionWebSocketHandler;

    // Endpoint para obtener comentarios por publicación
    @GetMapping("/{publicacionId}")
    public ResponseEntity<List<ComentarioDTO>> obtenerComentariosPorPublicacion(@PathVariable Integer publicacionId) {
        List<Comentarios> comentarios = comentariosRepository.findByPublicacionId(publicacionId);

        List<ComentarioDTO> comentariosDTO = comentarios.stream()
                .map(ComentarioDTO::new)
                .collect(Collectors.toList());

        return new ResponseEntity<>(comentariosDTO, HttpStatus.OK);
    }

    // Endpoint para añadir un comentario
    @PostMapping
    public ResponseEntity<String> agregarComentario(@Valid @RequestBody ComentarioDTO comentarioDTO,
                                                    @RequestHeader("Authorization") String token) {
        try {
            // Validar token y obtener alumno
            String email = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            Optional<Alumno> alumnoOptional = alumnoRepository.findByEmail(email);

            if (alumnoOptional.isEmpty()) {
                return new ResponseEntity<>("Alumno no autorizado", HttpStatus.UNAUTHORIZED);
            }

            Alumno alumno = alumnoOptional.get();

            // Validar la publicación
            Optional<Publicacion> publicacionOptional = publicacionRepository.findById(comentarioDTO.getPublicacionId());
            if (publicacionOptional.isEmpty()) {
                return new ResponseEntity<>("Publicación no encontrada", HttpStatus.NOT_FOUND);
            }

            Publicacion publicacion = publicacionOptional.get();

            // Crear el comentario
            Comentarios comentario = new Comentarios();
            comentario.setContenido(comentarioDTO.getContenido());
            comentario.setFechaCreacion(new Date());
            comentario.setAlumno(alumno);
            comentario.setPublicacion(publicacion);

            comentariosRepository.save(comentario);

// Notificar a los clientes sobre el nuevo comentario
            notificacionWebSocketHandler.notifyClients(
                    "ACTUALIZACION_PUBLICACIONES",
                    String.format("Nuevo comentario en '%s' por %s", publicacion.getTitulo(), alumno.getNombreCompleto())
            );


            return new ResponseEntity<>("Comentario añadido exitosamente", HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error interno al añadir el comentario", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
