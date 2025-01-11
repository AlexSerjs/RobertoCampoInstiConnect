package es.camporoberto.tfc.api.insticonnect.controllers;

import es.camporoberto.tfc.api.insticonnect.dtos.EncuestaDTO;
import es.camporoberto.tfc.api.insticonnect.entidades.*;
import es.camporoberto.tfc.api.insticonnect.entidades.Publicacion.TipoContenido;
import es.camporoberto.tfc.api.insticonnect.handler.NotificacionWebSocketHandler;
import es.camporoberto.tfc.api.insticonnect.repositories.AlumnoRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.GrupoRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.OpcionEncuestaRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.VotosRepository;
import es.camporoberto.tfc.api.insticonnect.services.PublicacionService;
import es.camporoberto.tfc.api.insticonnect.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/encuestas")
public class EncuestaController {

    private final PublicacionService publicacionService;
    private final GrupoRepository grupoRepository;
    private final AlumnoRepository alumnoRepository;
    private final JwtUtil jwtUtil;
    private final OpcionEncuestaRepository opcionEncuestaRepository;
    private final VotosRepository votosRepository;
    private final NotificacionWebSocketHandler notificacionWebSocketHandler;

    @Autowired
    public EncuestaController(PublicacionService publicacionService, GrupoRepository grupoRepository, AlumnoRepository alumnoRepository, JwtUtil jwtUtil, OpcionEncuestaRepository opcionEncuestaRepository, VotosRepository votosRepository, NotificacionWebSocketHandler notificacionWebSocketHandler) {
        this.publicacionService = publicacionService;
        this.grupoRepository = grupoRepository;
        this.alumnoRepository = alumnoRepository;
        this.jwtUtil = jwtUtil;
        this.opcionEncuestaRepository = opcionEncuestaRepository;
        this.votosRepository = votosRepository;
        this.notificacionWebSocketHandler = notificacionWebSocketHandler;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> crearEncuesta(@Valid @RequestBody EncuestaDTO encuestaDTO, @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Alumno> alumnoOptional = validarUsuario(token, response);
            if (alumnoOptional.isEmpty()) return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

            Alumno alumno = alumnoOptional.get();

            // Verificar si el alumno tiene permiso para publicar
            if (!alumno.getPuedePublicar()) {
                response.put("error", "No tienes permiso para publicar encuestas");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }

            Grupo grupo = alumno.getGrupo();

            if (grupo == null) {
                response.put("error", "No estás asignado a un grupo");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            Publicacion encuesta = new Publicacion();
            encuesta.setTitulo(encuestaDTO.getPregunta().toUpperCase());
            encuesta.setContenido(encuestaDTO.getPregunta());
            encuesta.setTipoContenido(TipoContenido.encuesta);
            encuesta.setFechaCreacion(new Date());
            encuesta.setGrupo(grupo);
            encuesta.setUsuario(alumno);

            Publicacion savedEncuesta = publicacionService.guardar(encuesta);

            List<OpcionEncuesta> opciones = encuestaDTO.getOpciones().stream()
                    .map(opcionTexto -> new OpcionEncuesta(opcionTexto, 0, savedEncuesta))
                    .toList();

            opcionEncuestaRepository.saveAll(opciones);

            notificacionWebSocketHandler.notifyClients("ACTUALIZACION_PUBLICACIONES", "Una nueva encuesta ha sido creada.");


            response.put("id", savedEncuesta.getId());
            response.put("pregunta", savedEncuesta.getContenido());
            response.put("opciones", opciones.stream().map(OpcionEncuesta::getTexto).toList());
            response.put("grupoId", grupo.getId());

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error interno: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> obtenerEncuestas() {
        try {
            // Obtener todas las encuestas de tipo ENCUESTA
            List<Publicacion> encuestas = publicacionService.obtenerPorTipo(TipoContenido.encuesta);

            // Construir la respuesta incluyendo el nombre del usuario
            List<Map<String, Object>> response = encuestas.stream()
                    .map(encuesta -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", encuesta.getId());
                        map.put("pregunta", encuesta.getContenido());
                        map.put("fechaCreacion", encuesta.getFechaCreacion());
                        map.put("opciones", encuesta.getOpciones().stream().map(opcion -> Map.of(
                                "id", opcion.getId(),
                                "texto", opcion.getTexto(),
                                "votos", opcion.getVotos()
                        )).toList());
                        map.put("creador", Map.of(
                                "nombre", encuesta.getUsuario().getNombreCompleto(),
                                "email", encuesta.getUsuario().getEmail()
                        ));
                        return map;
                    })
                    .toList();

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/votar")
    public ResponseEntity<Map<String, Object>> votar(@RequestBody Map<String, Object> payload, @RequestHeader("Authorization") String token) {
        try {
            // Extraer opcionId y encuestaId del payload
            Integer opcionId = (Integer) payload.get("opcionId");
            Integer encuestaId = (Integer) payload.get("encuestaId");

            if (opcionId == null || encuestaId == null) {
                return new ResponseEntity<>(Map.of("error", "Datos inválidos: opciónId o encuestaId faltante"), HttpStatus.BAD_REQUEST);
            }

            // Validar el token y obtener el alumno
            String email = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            Optional<Alumno> alumnoOptional = alumnoRepository.findByEmail(email);
            if (alumnoOptional.isEmpty()) {
                return new ResponseEntity<>(Map.of("error", "Usuario no autorizado"), HttpStatus.UNAUTHORIZED);
            }

            Alumno alumno = alumnoOptional.get();

            // Verificar si el alumno ya votó en esta encuesta
            boolean yaVoto = votosRepository.existsByAlumnoIdAndEncuestaId(alumno.getId(), encuestaId);
            if (yaVoto) {
                return new ResponseEntity<>(Map.of("error", "Ya has votado en esta encuesta"), HttpStatus.BAD_REQUEST);
            }

            // Buscar la opción de encuesta
            Optional<OpcionEncuesta> opcionOptional = opcionEncuestaRepository.findById(opcionId);
            if (opcionOptional.isEmpty()) {
                return new ResponseEntity<>(Map.of("error", "Opción no encontrada"), HttpStatus.NOT_FOUND);
            }

            OpcionEncuesta opcion = opcionOptional.get();

            // Registrar el voto
            Voto nuevoVoto = new Voto();
            nuevoVoto.setAlumnoId(alumno.getId());
            nuevoVoto.setEncuestaId(encuestaId);
            nuevoVoto.setOpcionId(opcionId);
            votosRepository.save(nuevoVoto);

            // Incrementar el contador de votos en la opción seleccionada
            opcion.setVotos(opcion.getVotos() + 1);
            opcionEncuestaRepository.save(opcion);

            notificacionWebSocketHandler.notifyClients("ACTUALIZACION_PUBLICACIONES", "Una nueva encuesta ha sido votada.");


            return new ResponseEntity<>(Map.of(
                    "message", "Voto registrado exitosamente",
                    "opcionId", opcion.getId(),
                    "votos", opcion.getVotos()
            ), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Map.of("error", "Error interno: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





    private Optional<Alumno> validarUsuario(String token, Map<String, Object> response) {
        try {
            String email = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            Optional<Alumno> alumnoOptional = alumnoRepository.findByEmail(email);

            if (alumnoOptional.isEmpty()) {
                response.put("error", "Usuario no autorizado");
                return Optional.empty();
            }

            return alumnoOptional;
        } catch (Exception e) {
            response.put("error", "Error al validar el usuario: " + e.getMessage());
            return Optional.empty();
        }
    }



}
