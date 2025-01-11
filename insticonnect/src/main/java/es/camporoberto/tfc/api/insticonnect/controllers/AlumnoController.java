package es.camporoberto.tfc.api.insticonnect.controllers;

import es.camporoberto.tfc.api.insticonnect.entidades.*;
import es.camporoberto.tfc.api.insticonnect.mapper.AlumnoMapper;
import es.camporoberto.tfc.api.insticonnect.repositories.AlumnoRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.CicloFormativoRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.GrupoRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.InstitutoRepository;
import es.camporoberto.tfc.api.insticonnect.services.AlumnoService;
import es.camporoberto.tfc.api.insticonnect.services.CursoService;
import es.camporoberto.tfc.api.insticonnect.services.EmailService;
import es.camporoberto.tfc.api.insticonnect.util.JwtUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/alumnos")
public class AlumnoController {

    private final AlumnoService alumnoService;
    private final CursoService cursoService;
    private final InstitutoRepository institutoRepository;
    private final GrupoRepository grupoRepository;
    private final CicloFormativoRepository cicloFormativoRepository;
    private final AlumnoRepository alumnoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    private static final Logger log = LoggerFactory.getLogger(AlumnoController.class);


    @Autowired
    public AlumnoController(AlumnoService alumnoService, CursoService cursoService, InstitutoRepository institutoRepository, GrupoRepository grupoRepository,
                            CicloFormativoRepository cicloFormativoRepository, AlumnoRepository alumnoRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, EmailService emailService) {
        this.alumnoService = alumnoService;
        this.cursoService = cursoService;
        this.institutoRepository = institutoRepository;
        this.grupoRepository = grupoRepository;
        this.cicloFormativoRepository = cicloFormativoRepository;
        this.alumnoRepository = alumnoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }



    // Obtener todos los alumnos
    @GetMapping
    public ResponseEntity<List<Alumno>> getAllAlumnos() {
        List<Alumno> alumnos = alumnoService.getAllAlumnos();
        return new ResponseEntity<>(alumnos, HttpStatus.OK);
    }

    // Obtener un alumno por ID
    @GetMapping("/{id}")
    public ResponseEntity<Alumno> getAlumnoById(@PathVariable Integer id) {
        Optional<Alumno> alumno = alumnoService.getAlumnoById(id);
        return alumno.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Crear un nuevo alumno
    @PostMapping
    public ResponseEntity<Alumno> createAlumno(@RequestBody Alumno alumno) {
        Alumno newAlumno = alumnoService.saveAlumno(alumno);
        return new ResponseEntity<>(newAlumno, HttpStatus.CREATED);
    }

    // Actualizar un alumno por ID
    @PutMapping("/{id}")
    public ResponseEntity<Alumno> updateAlumno(@PathVariable Integer id, @RequestBody Alumno alumnoDetails) {
        Optional<Alumno> updatedAlumno = alumnoService.updateAlumno(id, alumnoDetails);
        return updatedAlumno.map(alumno -> new ResponseEntity<>(alumno, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Eliminar un alumno por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlumno(@PathVariable Integer id) {
        if (alumnoService.deleteAlumno(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/delegado")
    public ResponseEntity<Map<String, Object>> createDelegado(@RequestBody DelegadoRequest request) {
        try {
            log.info("Recibiendo solicitud para crear delegado con los siguientes datos: {}", request);

            String institutoNombreNormalizado = request.getInstitutoNombre().trim();
            String cicloFormativoNormalizado = request.getCicloFormativo().trim();
            String anioNormalizado = request.getAnio().toLowerCase().trim();

            log.info("Datos normalizados: institutoNombre={}, cicloFormativo={}, anio={}",
                    institutoNombreNormalizado, cicloFormativoNormalizado, anioNormalizado);

            CicloFormativo.Anio anioEnum;
            try {
                anioEnum = CicloFormativo.Anio.valueOf(anioNormalizado);
            } catch (IllegalArgumentException e) {
                log.error("El valor del año no es válido: {}", anioNormalizado);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            Optional<Instituto> institutoOptional = institutoRepository.findFirstByNombre(institutoNombreNormalizado);
            if (institutoOptional.isEmpty()) {
                log.error("Instituto no encontrado: nombre={}", institutoNombreNormalizado);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Instituto instituto = institutoOptional.get();

            Optional<CicloFormativo> cicloOptional = cicloFormativoRepository.findFirstByNombreAndAnioAndInstitutoId(
                    cicloFormativoNormalizado, anioEnum, instituto.getId()
            );
            if (cicloOptional.isEmpty()) {
                log.error("Ciclo formativo no encontrado: nombre={}, anio={}, institutoId={}", cicloFormativoNormalizado, anioEnum, instituto.getId());
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            CicloFormativo ciclo = cicloOptional.get();

            Optional<Curso> cursoOptional = cursoService.findCursoByDetails(
                    instituto.getId(), ciclo.getId(), request.getAnioLectivo(), anioEnum
            );

            if (cursoOptional.isEmpty()) {
                log.error("Curso no encontrado: institutoId={}, cicloId={}, anioLectivo={}, anio={}",
                        instituto.getId(), ciclo.getId(), request.getAnioLectivo(), anioEnum);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Curso curso = cursoOptional.get();

            Optional<Grupo> existingGrupo = grupoRepository.findByCurso(curso);
            if (existingGrupo.isPresent()) {
                log.error("Ya existe un grupo para el curso con id: {}", curso.getId());
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }

            Grupo grupo = new Grupo();
            grupo.setCodigoGrupo(generarCodigoGrupo(curso));
            grupo.setCurso(curso);

            Grupo savedGrupo = grupoRepository.save(grupo);

            Alumno delegado = new Alumno();
            delegado.setNombreCompleto(request.getNombreCompleto());
            delegado.setFoto(request.getFoto());
            delegado.setEmail(request.getCorreoEducativo());
            delegado.setClave(passwordEncoder.encode(request.getClave()));
            delegado.setTipo(Alumno.Tipo.delegado);
            delegado.setGrupo(savedGrupo);
            delegado.setCodigoGrupo(savedGrupo.getCodigoGrupo());
            delegado.setIsVerified(false);
            delegado.setIntentosVerificacion(0);

            Alumno savedDelegado = alumnoService.saveAlumno(delegado);
            savedGrupo.setDelegado(savedDelegado);
            grupoRepository.save(savedGrupo);

            // Generar y devolver el token de verificación
            String verificationToken = jwtUtil.generateToken(savedDelegado.getEmail());
            Map<String, Object> response = new HashMap<>();
            response.put("delegado", savedDelegado);
            response.put("verificationToken", verificationToken); // Incluir el token en la respuesta

            log.info("Delegado y grupo creados exitosamente con id de grupo: {}", savedGrupo.getId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error al crear delegado: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }





    // Método para generar el código de grupo
    private String generarCodigoGrupo(Curso curso) {
        // Obtener el instituto desde el curso
        Instituto instituto = curso.getInstituto();

        if (instituto == null || instituto.getComunidadAutonoma() == null) {
            throw new IllegalArgumentException("El instituto o la comunidad autónoma no es válida");
        }

        // Obtener el sufijo de la comunidad autónoma
        String sufijo = instituto.getComunidadAutonoma().getCodigoPrefijo();

        if (sufijo == null) {
            throw new IllegalArgumentException("Sufijo no encontrado para la Comunidad Autónoma");
        }

        // Generar un número aleatorio de 4 dígitos
        String numeroAleatorio;
        do {
            numeroAleatorio = String.format("%04d", new Random().nextInt(10000));
        } while (codigoGrupoExiste(sufijo + "-" + numeroAleatorio));

        return sufijo + "-" + numeroAleatorio;
    }

    // Método para verificar si el código de grupo ya existe en la base de datos
    private boolean codigoGrupoExiste(String codigoGrupo) {
        return grupoRepository.existsByCodigoGrupo(codigoGrupo);
    }


    // Clase auxiliar para recibir datos del delegado
    @Getter
    public static class DelegadoRequest {
        // Getters y setters
        private String institutoNombre;
        private String cicloFormativo;
        private String anioLectivo;
        private String anio;
        private String nombreCompleto;
        private String foto;
        private String correoEducativo;
        private String clave;


    }


    @PostMapping("/alumno")
    public ResponseEntity<Map<String, Object>> createAlumno(@RequestBody AlumnoRequest request) {
        try {
            log.info("Recibiendo solicitud para crear alumno con los siguientes datos: {}", request);

            // Buscar el grupo por código de grupo
            Optional<Grupo> grupoOptional = grupoRepository.findByCodigoGrupo(request.getCodigoGrupo());

            if (grupoOptional.isEmpty()) {
                log.error("Grupo no encontrado: codigoGrupo={}", request.getCodigoGrupo());
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            Grupo grupo = grupoOptional.get();

            // Crear un objeto Alumno y asociarlo al grupo
            Alumno alumno = new Alumno();
            alumno.setNombreCompleto(request.getNombreCompleto());
            alumno.setFoto(request.getFoto());
            alumno.setEmail(request.getCorreoEducativo());

            // Encriptar la contraseña
            String encodedPassword = passwordEncoder.encode(request.getClave());
            alumno.setClave(encodedPassword);

            alumno.setTipo(Alumno.Tipo.alumno);
            alumno.setGrupo(grupo);
            alumno.setCodigoGrupo(grupo.getCodigoGrupo());
            alumno.setIsVerified(false);
            alumno.setIntentosVerificacion(0);

            // Guardar el alumno
            log.info("Guardando alumno: {}", alumno);
            Alumno savedAlumno = alumnoService.saveAlumno(alumno);

            // Generar y devolver el token de verificación
            String verificationToken = jwtUtil.generateToken(savedAlumno.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("alumno", AlumnoMapper.fromEntity(savedAlumno));
            response.put("verificationToken", verificationToken);

            log.info("Alumno creado exitosamente.");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error al crear alumno: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @Getter
    public static class AlumnoRequest {
        private String nombreCompleto;
        private String foto;
        private String codigoGrupo;
        private String correoEducativo;
        private String clave;
    }

    @GetMapping("/codigo-grupo-delegado")
    public ResponseEntity<?> getCodigoGrupoDelegado(@RequestHeader("Authorization") String token) {
        try {
            // Extraer el email del token
            String email = jwtUtil.extractUsername(token.replace("Bearer ", ""));

            // Buscar al alumno por email
            Optional<Alumno> alumnoOptional = alumnoRepository.findByEmail(email);
            if (alumnoOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Alumno no encontrado"));
            }

            Alumno alumno = alumnoOptional.get();

            // Buscar el código del grupo donde es delegado
            Optional<String> codigoGrupoOptional = grupoRepository.findCodigoGrupoByDelegadoId(alumno.getId());
            if (codigoGrupoOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "El alumno no es delegado de ningún grupo"));
            }

            String codigoGrupo = codigoGrupoOptional.get();

            // Retornar solo el código del grupo
            return ResponseEntity.ok(Map.of("codigoGrupo", codigoGrupo));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error al obtener el código del grupo del delegado"));
        }
    }



}
