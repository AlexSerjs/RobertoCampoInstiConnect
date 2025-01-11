package es.camporoberto.tfc.api.insticonnect.controllers;

import es.camporoberto.tfc.api.insticonnect.dtos.ConcreteErrorResponse;
import es.camporoberto.tfc.api.insticonnect.dtos.LoginRequest;
import es.camporoberto.tfc.api.insticonnect.entidades.Administrador;
import es.camporoberto.tfc.api.insticonnect.entidades.Alumno;
import es.camporoberto.tfc.api.insticonnect.services.AdministradorService;
import es.camporoberto.tfc.api.insticonnect.services.AlumnoService;
import es.camporoberto.tfc.api.insticonnect.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private AlumnoService alumnoService; // Servicio para alumnos

    @Autowired
    private AdministradorService administradorService; // Servicio para administradores

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil; // Generación y validación de tokens JWT

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String input = loginRequest.getCorreo(); // Entrada del usuario
            log.info("Iniciando sesión para el input: {}", input);

            // Verificar si contiene un '@' (alumno) o no (administrador)
            if (input.contains("@")) {
                // Autenticación para alumnos
                Optional<Alumno> alumnoOptional = alumnoService.findByEmail(input);

                if (alumnoOptional.isEmpty()) {
                    log.warn("No se encontró ningún alumno con el correo: {}", input);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ConcreteErrorResponse("error", "Correo o contraseña incorrectos."));
                }

                Alumno alumno = alumnoOptional.get();

                // Validar contraseña
                if (!passwordEncoder.matches(loginRequest.getClave(), alumno.getClave())) {
                    log.warn("Contraseña incorrecta para el correo: {}", input);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new ConcreteErrorResponse("error", "Correo o contraseña incorrectos."));
                }

                // Verificar si el alumno está verificado
                if (!alumno.getIsVerified()) {
                    log.warn("El correo no ha sido verificado: {}", input);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new ConcreteErrorResponse("error", "El correo no ha sido verificado. Por favor, revisa tu bandeja de entrada."));
                }

                // Generar el token JWT
                String token = jwtUtil.generateToken(alumno.getEmail());

                // Respuesta con el token y el tipo de usuario
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("tipo", "alumno");
                log.info("Inicio de sesión exitoso para el alumno con correo: {}", input);

                return ResponseEntity.ok(response);
            } else {
                // Autenticación para administradores
                Optional<Administrador> adminOptional = administradorService.findByNombre(input);

                if (adminOptional.isEmpty()) {
                    log.warn("No se encontró ningún administrador con el nombre: {}", input);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ConcreteErrorResponse("error", "Nombre o contraseña incorrectos."));
                }

                Administrador administrador = adminOptional.get();

                // Validar contraseña
                if (!passwordEncoder.matches(loginRequest.getClave(), administrador.getContrasena())) {
                    log.warn("Contraseña incorrecta para el nombre: {}", input);
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new ConcreteErrorResponse("error", "Nombre o contraseña incorrectos."));
                }

                // Generar el token JWT
                String token = jwtUtil.generateToken(administrador.getNombre());

                // Respuesta con el token y el tipo de usuario
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("tipo", "administrador");
                log.info("Inicio de sesión exitoso para el administrador con nombre: {}", input);

                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            log.error("Error al iniciar sesión: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ConcreteErrorResponse("error", "Error interno del servidor al intentar iniciar sesión."));
        }
    }
}
