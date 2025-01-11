package es.camporoberto.tfc.api.insticonnect.controllers;

import es.camporoberto.tfc.api.insticonnect.dtos.*;
import es.camporoberto.tfc.api.insticonnect.entidades.*;
import es.camporoberto.tfc.api.insticonnect.repositories.AlumnoRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.OpcionEncuestaRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.VotosRepository;
import es.camporoberto.tfc.api.insticonnect.services.*;
import es.camporoberto.tfc.api.insticonnect.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/publicaciones")
public class PublicacionController {


    private final PublicacionService publicacionService;
    private final JwtUtil jwtUtil;
    private final AlumnoRepository alumnoRepository;
    private final VotosRepository votosRepository;
    private final OpcionEncuestaRepository opcionEncuestaRepository;

    @Autowired
    public PublicacionController(PublicacionService publicacionService, JwtUtil jwtUtil, AlumnoRepository alumnoRepository, VotosRepository votosRepository, OpcionEncuestaRepository opcionEncuestaRepository) {
        this.publicacionService = publicacionService;
        this.jwtUtil = jwtUtil;
        this.alumnoRepository = alumnoRepository;
        this.votosRepository = votosRepository;
        this.opcionEncuestaRepository = opcionEncuestaRepository;
    }

    @GetMapping("/grupo")
    public ResponseEntity<Map<String, Object>> obtenerPublicacionesPorGrupo(@RequestHeader("Authorization") String token) {
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

            // Obtener todas las publicaciones asociadas al grupo del alumno
            List<Publicacion> publicaciones = publicacionService.obtenerPorGrupo(alumno.getGrupo().getId());

            // Ordenar las publicaciones por fecha de creación de manera descendente
            publicaciones.sort((p1, p2) -> p2.getFechaCreacion().compareTo(p1.getFechaCreacion()));

            // Convertir las publicaciones a DTO
            List<PublicacionDTO> publicacionesDTO = publicaciones.stream()
                    .map(publicacion -> mapearPublicacionDTOConVoto(publicacion, alumno))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(Map.of("publicaciones", publicacionesDTO), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Map.of("error", "Error al obtener las publicaciones"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private PublicacionDTO mapearPublicacionDTOConVoto(Publicacion publicacion, Alumno alumno) {
        PublicacionDTO publicacionDTO = new PublicacionDTO(publicacion);

        if (publicacion.getTipoContenido() == Publicacion.TipoContenido.encuesta) {
            boolean yaVoto = votosRepository.existsByAlumnoIdAndEncuestaId(alumno.getId(), publicacion.getId());
            publicacionDTO.setYaVotado(yaVoto);

            if (yaVoto) {
                Optional<Voto> voto = votosRepository.findByAlumnoIdAndEncuestaId(alumno.getId(), publicacion.getId());
                voto.ifPresent(v -> {
                    OpcionEncuesta opcion = opcionEncuestaRepository.findById(v.getOpcionId()).orElse(null);
                    if (opcion != null) {
                        publicacionDTO.setOpcionSeleccionada(new OpcionEncuestaDTO(opcion));
                    }
                });
            }
        }

        return publicacionDTO;
    }




    @GetMapping("/grupo/historico")
    public ResponseEntity<Map<String, Object>> obtenerPublicacionesHistoricas(
            @RequestHeader("Authorization") String token) {
        try {
            // Extraer el email del token
            String email = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            System.out.println("Email extraído del token: " + email);

            // Buscar al alumno por email
            Optional<Alumno> alumnoOptional = alumnoRepository.findByEmail(email);

            if (alumnoOptional.isEmpty()) {
                return new ResponseEntity<>(Map.of("error", "Usuario no autorizado"), HttpStatus.UNAUTHORIZED);
            }

            Alumno alumno = alumnoOptional.get();
            System.out.println("Alumno encontrado: " + alumno.getNombreCompleto());

            if (alumno.getGrupo() == null) {
                return new ResponseEntity<>(Map.of("error", "El alumno no está asignado a un grupo"), HttpStatus.BAD_REQUEST);
            }

            // Obtener todas las publicaciones del grupo
            List<Publicacion> publicaciones = publicacionService.obtenerPorGrupo(alumno.getGrupo().getId());

            // Ordenar las publicaciones por fecha de creación de manera descendente
            publicaciones.sort((p1, p2) -> p2.getFechaCreacion().compareTo(p1.getFechaCreacion()));

            // Convertir las publicaciones en DTOs según el tipo de contenido
            List<Object> publicacionesDTO = publicaciones.stream()
                    .map(publicacion -> mapearPublicacionDTO(publicacion))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(Map.of("publicaciones", publicacionesDTO), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(Map.of("error", "Error al obtener las publicaciones"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    private Object mapearPublicacionDTO(Publicacion publicacion) {
        switch (publicacion.getTipoContenido()) {
            case texto:
                return new PublicacionTextoDTO(publicacion);
            case imagen:
                return new PublicacionImagenDTO(publicacion);
            case archivo:
                return new PublicacionArchivoDTO(publicacion);
            case encuesta:
                return new PublicacionEncuestaDTO(publicacion);
            case mixto:
                return new PublicacionMixtoDTO(publicacion);
            case comunicado:
                return new PublicacionComunicadoDTO(publicacion);
            default:
                return new PublicacionDTO(publicacion);
        }
    }




}
