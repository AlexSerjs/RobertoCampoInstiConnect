package es.camporoberto.tfc.api.insticonnect.controllers;

import es.camporoberto.tfc.api.insticonnect.dtos.AsignaturaDTO;
import es.camporoberto.tfc.api.insticonnect.dtos.HorarioClasesDTO;
import es.camporoberto.tfc.api.insticonnect.entidades.Alumno;
import es.camporoberto.tfc.api.insticonnect.handler.HorarioWebSocketHandler;
import es.camporoberto.tfc.api.insticonnect.repositories.AlumnoRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.AsignaturaRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.GrupoRepository;
import es.camporoberto.tfc.api.insticonnect.services.AlumnoService;
import es.camporoberto.tfc.api.insticonnect.entidades.Grupo;
import es.camporoberto.tfc.api.insticonnect.entidades.HorarioClases;
import es.camporoberto.tfc.api.insticonnect.repositories.HorarioClasesRepository;
import es.camporoberto.tfc.api.insticonnect.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/horario")
public class HorarioClasesController {

    private final JwtUtil jwtUtil;
    private final HorarioClasesRepository horarioClasesRepository;
    private final AlumnoService alumnoService;
    private final GrupoRepository grupoRepository;
    private final AlumnoRepository alumnoRepository;
    private final AsignaturaRepository asignaturaRepository;
    private final HorarioWebSocketHandler horarioWebSocketHandler;

    @Autowired
    public HorarioClasesController(JwtUtil jwtUtil, AlumnoRepository alumnoRepository, AsignaturaRepository asignaturaRepository,
                                   HorarioClasesRepository horarioClasesRepository, AlumnoService alumnoService, GrupoRepository grupoRepository, AlumnoRepository alumnoRepository1, AsignaturaRepository asignaturaRepository1, HorarioWebSocketHandler horarioWebSocketHandler)
    {
        this.jwtUtil = jwtUtil;
        this.horarioClasesRepository = horarioClasesRepository;
        this.alumnoService = alumnoService;
        this.grupoRepository = grupoRepository;
        this.alumnoRepository = alumnoRepository1;
        this.asignaturaRepository = asignaturaRepository1;
        this.horarioWebSocketHandler = horarioWebSocketHandler;
    }


    @GetMapping("/existe")
    public ResponseEntity<Map<String, Boolean>> verificarHorarioExiste(@RequestHeader("Authorization") String token) {
        // Limpiar el token y extraer el email
        token = token.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);

        // Obtener el grupo asociado al alumno con ese email
        Grupo grupo = alumnoService.obtenerGrupoPorEmail(email);
        if (grupo == null) {
            throw new RuntimeException("Grupo no encontrado para el alumno con email: " + email);
        }

        // Verificar si el grupo ya tiene un horario asociado
        boolean existe = Boolean.TRUE.equals(grupo.getHorarioAsociado());
        Map<String, Boolean> response = new HashMap<>();
        response.put("existe", existe);
        return ResponseEntity.ok(response);
    }





    @PostMapping("/crearPredeterminado")
    public ResponseEntity<String> crearHorarioPredeterminado(@RequestHeader("Authorization") String token) {
        // Elimina el prefijo "Bearer " del token
        token = token.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token); // Extrae el email del token

        // Obtener el grupo asociado al alumno por email
        Grupo grupo = alumnoService.obtenerGrupoPorEmail(email);
        if (grupo == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Grupo no encontrado para el alumno.");
        }

        // Verificar si el grupo ya tiene un horario asociado
        if (Boolean.TRUE.equals(grupo.getHorarioAsociado())) {
            return ResponseEntity.badRequest().body("El grupo ya tiene un horario asociado.");
        }

        // Crear el horario predeterminado
        List<String> bloques = Arrays.asList("primeraHora", "segundaHora", "terceraHora", "cuartaHora", "quintaHora", "sextaHora");

        for (String bloque : bloques) {
            HorarioClases horario = crearBloqueHorario(grupo, bloque);
            horarioClasesRepository.save(horario);
        }

        // Marcar que el grupo tiene un horario asociado
        grupo.setHorarioAsociado(true);
        grupoRepository.save(grupo);
        horarioWebSocketHandler.notifyHorarioUpdate("Se ha creado un horario predeterminado para el grupo " + grupo.getCodigoGrupo());

        return ResponseEntity.status(HttpStatus.CREATED).body("Horario predeterminado creado exitosamente.");
    }

    private HorarioClases crearBloqueHorario(Grupo grupo, String bloque) {
        HorarioClases horario = new HorarioClases();
        horario.setGrupo(grupo);
        horario.setBloque(bloque);
        horario.setHoraInicio(Time.valueOf("12:00:00"));
        horario.setHoraFin(Time.valueOf("12:00:00"));
        horario.setLunes("Libre");
        horario.setMartes("Libre");
        horario.setMiercoles("Libre");
        horario.setJueves("Libre");
        horario.setViernes("Libre");
        return horario;
    }


    @DeleteMapping("/eliminarHorario")
    public ResponseEntity<String> eliminarHorario(@RequestHeader("Authorization") String token) {
        // Extraer el grupoId a partir del token
        token = token.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);

        // Obtener el grupo asociado al alumno con ese email
        Grupo grupo = alumnoService.obtenerGrupoPorEmail(email);
        if (grupo == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Grupo no encontrado para el alumno.");
        }

        Integer grupoId = grupo.getId();

        // Eliminar todos los horarios asociados al grupo
        List<HorarioClases> horarios = horarioClasesRepository.findByGrupoId(grupoId);
        if (horarios.isEmpty()) {
            return ResponseEntity.badRequest().body("No hay horarios asociados a este grupo.");
        }

        horarioClasesRepository.deleteAll(horarios);

        // Marcar que el grupo ya no tiene un horario asociado
        grupo.setHorarioAsociado(false);
        grupoRepository.save(grupo);
        horarioWebSocketHandler.notifyHorarioUpdate("El horario del grupo " + grupo.getCodigoGrupo() + " ha sido eliminado.");

        return ResponseEntity.ok("Horario eliminado correctamente.");
    }



    @GetMapping("/datos")
    public ResponseEntity<List<HorarioClasesDTO>> obtenerHorario(@RequestHeader("Authorization") String token) {
        try {
            // Obtener el email del token
            token = token.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(token);

            // Obtener el grupo asociado al alumno con ese email
            Grupo grupo = alumnoService.obtenerGrupoPorEmail(email);
            if (grupo == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            List<HorarioClases> horario = horarioClasesRepository.findByGrupoId(grupo.getId());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            // Convertir la lista de `HorarioClases` a una lista de `HorarioClasesDTO`
            List<HorarioClasesDTO> horarioDTO = horario.stream().map(h -> new HorarioClasesDTO(
                    h.getBloque(),
                    h.getHoraInicio().toString(),
                    h.getHoraFin().toString(),
                    h.getLunes(),
                    h.getMartes(),
                    h.getMiercoles(),
                    h.getJueves(),
                    h.getViernes()
            )).collect(Collectors.toList());

            return ResponseEntity.ok(horarioDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping("/asignaturas")
    public ResponseEntity<List<AsignaturaDTO>> getAsignaturas(
            @RequestHeader("Authorization") String token) {
        try {
            // Extraer el email del token
            token = token.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(token);

            // Obtener el alumno y su grupo
            Alumno delegado = alumnoRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Delegado no encontrado"));
            Grupo grupo = delegado.getGrupo();

            // Verificar que el grupo no sea nulo
            if (grupo == null) {
                return ResponseEntity.badRequest().body(null);
            }

            // Obtener asignaturas del grupo y mapear a DTOs
            List<AsignaturaDTO> asignaturas = asignaturaRepository.findByGrupoId(grupo.getId()).stream()
                    .map(asignatura -> new AsignaturaDTO(
                            asignatura.getId(),
                            asignatura.getNombre(),
                            asignatura.getProfesor(),
                            asignatura.getEmail()))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(asignaturas);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PutMapping("/actualizar")
    public ResponseEntity<String> actualizarHorario(
            @RequestHeader("Authorization") String token,
            @RequestBody List<HorarioClasesDTO> horarioActualizado) {
        try {
            token = token.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(token);

            Grupo grupo = alumnoService.obtenerGrupoPorEmail(email);
            if (grupo == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Grupo no encontrado");
            }

            for (HorarioClasesDTO dto : horarioActualizado) {
                HorarioClases horario = horarioClasesRepository.findByGrupoAndBloque(grupo, dto.getBloque())
                        .orElse(new HorarioClases()); // Crea un nuevo horario si no existe

                horario.setGrupo(grupo); // Asegúrate de establecer el grupo si es un nuevo horario
                horario.setBloque(dto.getBloque());
                horario.setHoraInicio(Time.valueOf(dto.getHoraInicio()));
                horario.setHoraFin(Time.valueOf(dto.getHoraFin()));
                horario.setLunes(dto.getLunes());
                horario.setMartes(dto.getMartes());
                horario.setMiercoles(dto.getMiercoles());
                horario.setJueves(dto.getJueves());
                horario.setViernes(dto.getViernes());

                horarioClasesRepository.save(horario); // Guardar cambios
            }

            horarioWebSocketHandler.notifyHorarioUpdate("El horario ha sido actualizado para el grupo " + grupo.getCodigoGrupo());


            return ResponseEntity.ok("Horario actualizado correctamente");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el horario");
        }
    }


    //------------------------------------------------------------------------------------------------------------------
    //Alumnos

    @GetMapping("/alumno")
    public ResponseEntity<List<HorarioClasesDTO>> obtenerHorarioAlumno(@RequestHeader("Authorization") String token) {
        try {
            // Eliminar el prefijo "Bearer " y extraer el email
            token = token.replace("Bearer ", "");
            String email = jwtUtil.extractUsername(token);

            // Obtener el grupo asociado al alumno con ese email
            Grupo grupo = alumnoService.obtenerGrupoPorEmail(email);
            if (grupo == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // Obtener el horario asociado al grupo
            List<HorarioClases> horario = horarioClasesRepository.findByGrupoId(grupo.getId());

            // Si no hay horarios asociados
            if (horario.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Devuelve 404 si no hay horarios
            }

            // Convertir la lista de `HorarioClases` a una lista de `HorarioClasesDTO`
            List<HorarioClasesDTO> horarioDTO = horario.stream().map(h -> new HorarioClasesDTO(
                    h.getBloque(),
                    h.getHoraInicio().toString(),
                    h.getHoraFin().toString(),
                    h.getLunes(),
                    h.getMartes(),
                    h.getMiercoles(),
                    h.getJueves(),
                    h.getViernes()
            )).collect(Collectors.toList());

            return ResponseEntity.ok(horarioDTO); // Retornar los datos del horario
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Devuelve un error 500 en caso de excepción
        }
    }




}
