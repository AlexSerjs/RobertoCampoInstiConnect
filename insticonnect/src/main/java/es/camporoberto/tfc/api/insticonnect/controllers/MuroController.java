package es.camporoberto.tfc.api.insticonnect.controllers;

import es.camporoberto.tfc.api.insticonnect.entidades.Alumno;
import es.camporoberto.tfc.api.insticonnect.repositories.AlumnoRepository;
import es.camporoberto.tfc.api.insticonnect.services.AlumnoService;
import es.camporoberto.tfc.api.insticonnect.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/muro")
public class MuroController {

    private static final Logger logger = LoggerFactory.getLogger(MuroController.class);


    private final JwtUtil jwtUtil;
    private final AlumnoService alumnoService;
    private final AlumnoRepository alumnoRepository;

    @Autowired
    public MuroController(JwtUtil jwtUtil, AlumnoService alumnoService, AlumnoRepository alumnoRepository) {
        this.jwtUtil = jwtUtil;
        this.alumnoService = alumnoService;
        this.alumnoRepository = alumnoRepository;
    }

    @GetMapping("/tipoPerfil")
    public ResponseEntity<?> getPerfil(@RequestHeader("Authorization") String token) {
        try {
            // Eliminar el prefijo "Bearer " del token
            token = token.replace("Bearer ", "");

            // Extraer el email del token
            String email = jwtUtil.extractUsername(token);

            // Buscar el alumno en la base de datos usando el servicio
            Optional<Alumno> alumnoOptional = alumnoService.findByEmail(email);

            if (alumnoOptional.isPresent()) {
                // Extraer el tipo de usuario (delegado o alumno) desde la entidad Alumno
                Alumno alumno = alumnoOptional.get();
                String tipoUsuario = alumno.getTipo().name(); // Convertir a String

                // Respuesta JSON con el tipo de usuario
                return ResponseEntity.ok(Map.of("tipoUsuario", tipoUsuario));
            } else {
                // Si no se encuentra el alumno
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Alumno no encontrado");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
        }
    }


    @PostMapping("/cambiarFoto")
    public ResponseEntity<?> cambiarFoto(@RequestHeader("Authorization") String token,
                                         @RequestParam("foto") MultipartFile foto) {

        logger.info("Solicitud recibida para cambiar foto"); // Log inicial
        try {
            // Validar el token y extraer el email del usuario
            logger.debug("Token recibido: {}", token);

            token = token.replace("Bearer ", "").trim();
            logger.debug("Token limpio: {}", token);

            String email = jwtUtil.extractUsername(token);
            logger.debug("Email extraído del token: {}", email);

            if (foto.isEmpty()) {
                logger.warn("Archivo vacío recibido para el email: {}", email);
                return ResponseEntity.badRequest().body("El archivo no puede estar vacío.");
            }

            logger.debug("Archivo recibido: nombre={}, tamaño={} bytes, tipo={}",
                    foto.getOriginalFilename(), foto.getSize(), foto.getContentType());

            // Guardar la foto y obtener la nueva ruta
            String nuevaRutaFoto = alumnoService.guardarFoto(email, foto);
            logger.info("Foto guardada correctamente para el usuario: {}", email);

            // Devolver la ruta actualizada al cliente
            return ResponseEntity.ok(Map.of("rutaFoto", nuevaRutaFoto));
        } catch (Exception e) {
            logger.error("Error al cambiar la foto", e); // Log de error con detalles
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al cambiar la foto.");
        }
    }


    @GetMapping("/obtenerFoto")
    public ResponseEntity<?> obtenerFoto(@RequestHeader("Authorization") String token) {
        try {
            // Log para verificar el token recibido
            System.out.println("Token recibido: " + token);

            // Extraer email del token
            token = token.replace("Bearer ", "");
            System.out.println("Token después de eliminar 'Bearer ': " + token);

            String email = jwtUtil.extractUsername(token);
            System.out.println("Email extraído del token: " + email);

            // Buscar al alumno en la base de datos
            Alumno alumno = alumnoRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));

            // Verificar si tiene una foto asignada
            String rutaFoto = alumno.getFoto();
            System.out.println("Ruta de la foto obtenida: " + rutaFoto);

            if (rutaFoto == null || rutaFoto.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontró una foto para este usuario.");
            }

            // Devolver la ruta relativa de la foto
            return ResponseEntity.ok(Map.of("rutaFoto", rutaFoto));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener la foto.");
        }
    }

    @GetMapping("/infoUsuario")
    public ResponseEntity<?> obtenerInfoUsuario(@RequestHeader("Authorization") String token) {
        try {
            // Log para depuración: Verifica el token recibido
            logger.debug("Token recibido para obtener información del usuario: {}", token);

            // Limpia el token eliminando el prefijo "Bearer "
            token = token.replace("Bearer ", "").trim();
            logger.debug("Token limpio: {}", token);

            // Extraer el email del token
            String email = jwtUtil.extractUsername(token);
            logger.debug("Email extraído del token: {}", email);

            // Buscar al alumno en la base de datos
            Alumno alumno = alumnoRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Alumno no encontrado en la base de datos"));

            // Crear la respuesta con los datos del usuario
            Map<String, String> usuarioInfo = Map.of(
                    "nombreCompleto", alumno.getNombreCompleto(),
                    "email", alumno.getEmail()
            );
            logger.debug("Información del usuario obtenida: {}", usuarioInfo);

            // Retornar la información del usuario
            return ResponseEntity.ok(usuarioInfo);

        } catch (Exception e) {
            logger.error("Error al obtener la información del usuario", e); // Log del error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener la información del usuario.");
        }
    }







}
