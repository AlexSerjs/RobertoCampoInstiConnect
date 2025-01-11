package es.camporoberto.tfc.api.insticonnect.controllers;

import es.camporoberto.tfc.api.insticonnect.dtos.ConcreteErrorResponse;
import es.camporoberto.tfc.api.insticonnect.dtos.ReenviarCorreoRequest;
import es.camporoberto.tfc.api.insticonnect.dtos.RespuestaGeneral;
import es.camporoberto.tfc.api.insticonnect.dtos.VerificacionResponse;
import es.camporoberto.tfc.api.insticonnect.entidades.Alumno;
import es.camporoberto.tfc.api.insticonnect.services.AlumnoService;
import es.camporoberto.tfc.api.insticonnect.services.EmailService;
import es.camporoberto.tfc.api.insticonnect.services.TokenService;
import es.camporoberto.tfc.api.insticonnect.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController // Definir como controlador REST
@RequestMapping("/api") // Definir la ruta base
public class EmailVerificacionController {

    private final AlumnoService alumnoService;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final TokenService tokenService;

    @Autowired
    public EmailVerificacionController(AlumnoService alumnoService, JwtUtil jwtUtil, EmailService emailService, TokenService tokenService) {
        this.alumnoService = alumnoService;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.tokenService = tokenService;
    }

    // Endpoint para verificar el token del correo electrónico

    @GetMapping("/test-verify")
    public ResponseEntity<?> verificarCorreo(@RequestParam("token") String token) {
        log.info("Recibiendo solicitud para verificar el token: {}", token);
        try {
            boolean isValidToken = tokenService.validateToken(token);
            if (isValidToken) {
                String email = tokenService.extraerCorreo(token);
                log.info("Token válido. Correo extraído: {}", email);
                Optional<Alumno> alumnoOptional = alumnoService.findByEmail(email);
                if (alumnoOptional.isPresent()) {
                    Alumno alumno = alumnoOptional.get();
                    alumno.setIsVerified(true);  // Marca al alumno como verificado
                    alumnoService.saveAlumno(alumno);
                    String tipo = alumno.getTipo().name();
                    log.info("Alumno con correo {} verificado exitosamente.", email);
                    return ResponseEntity.ok().body(new VerificacionResponse("Tu correo ya está verificado, cierra la ventana", tipo));
                } else {
                    log.warn("No se encontró el alumno con el correo: {}", email);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ConcreteErrorResponse("error", "No se encontró el usuario con el correo: " + email));
                }
            } else {
                log.warn("Token de verificación inválido o expirado: {}", token);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ConcreteErrorResponse("error", "Token de verificación inválido o expirado."));
            }
        } catch (Exception e) {
            log.error("Error al verificar el correo: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ConcreteErrorResponse("error", "Error al verificar el correo."));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> reenviarCorreoVerificacion(@RequestBody ReenviarCorreoRequest request) {
        log.info("Recibiendo solicitud para reenviar correo de verificación.");
        try {
            // Extraer el correo a partir del token proporcionado en la solicitud
            String email;
            try {
                email = tokenService.extraerCorreo(request.getToken());
            } catch (Exception e) {
                log.error("Error al extraer el correo del token: ", e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ConcreteErrorResponse("error", "Token inválido o expirado."));
            }

            if (email == null || email.isEmpty()) {
                log.warn("El token es inválido o ha expirado. No se pudo extraer el correo.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ConcreteErrorResponse("error", "Token inválido o expirado."));
            }

            // Buscar al alumno utilizando el correo extraído del token
            Optional<Alumno> alumnoOptional = alumnoService.findByEmail(email);
            if (alumnoOptional.isPresent()) {
                Alumno alumno = alumnoOptional.get();

                // Verificar si ya ha excedido el número máximo de intentos
                if (alumno.getIntentosVerificacion() >= 3) {
                    log.warn("Límite de intentos de verificación alcanzado para el correo: {}", email);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ConcreteErrorResponse("error", "Límite de intentos de verificación alcanzado."));
                }

                // Incrementar el número de intentos de verificación
                alumno.setIntentosVerificacion(alumno.getIntentosVerificacion() + 1);
                alumnoService.saveAlumno(alumno);
                log.info("Intento de verificación incrementado para el correo: {}. Intentos actuales: {}", email, alumno.getIntentosVerificacion());

                // Generar y reenviar el token de verificación
                String token = tokenService.generarToken(email);
                String linkVerificacion = "http://localhost:8080/api/test-verify?token=" + token;
                log.info("Enlace de verificación generado: {}", linkVerificacion);

                emailService.sendVerificationEmail(email, linkVerificacion);
                log.info("Correo de verificación reenviado a: {}", email);

                RespuestaGeneral respuesta = new RespuestaGeneral("resent", "Correo de verificación reenviado.");
                respuesta.setToken(token);
                return ResponseEntity.ok().body(respuesta);
            } else {
                log.warn("No se encontró el alumno con el correo: {}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ConcreteErrorResponse("error", "No se encontró el usuario con el correo: " + email));
            }
        } catch (Exception e) {
            log.error("Error al reenviar el correo de verificación: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ConcreteErrorResponse("error", "Error al reenviar el correo de verificación."));
        }
    }




    @GetMapping("/test-email")
    public ResponseEntity<String> sendTestEmail() {
        try {
            // Cambia el correo del destinatario al que quieres probar (en este caso 'alexserjs@gmail.com')
            String receptor = "alexserjs@gmail.com";
            String linkVerificacion = "https://theserjs.es/verificacion";

            emailService.sendVerificationEmail(receptor, linkVerificacion);

            return ResponseEntity.ok("Correo de prueba enviado.");
        } catch (Exception e) {
            log.error("Error al enviar el correo de prueba: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No se pudo enviar el correo de prueba.");
        }
    }


}
