package es.camporoberto.tfc.api.insticonnect.controllers;

import es.camporoberto.tfc.api.insticonnect.entidades.Alumno;
import es.camporoberto.tfc.api.insticonnect.entidades.RecuperacionClave;
import es.camporoberto.tfc.api.insticonnect.repositories.AlumnoRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.RecuperacionClaveRepository;
import es.camporoberto.tfc.api.insticonnect.services.AlumnoService;
import es.camporoberto.tfc.api.insticonnect.services.EmailService;
import es.camporoberto.tfc.api.insticonnect.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/recuperar")
public class RecuperarController {

    private final AlumnoService alumnoService;
    private final EmailService emailService;
    private final RecuperacionClaveRepository recuperacionClaveRepository;
    private final AlumnoRepository alumnoRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RecuperarController(AlumnoService alumnoService, EmailService emailService,
                               RecuperacionClaveRepository recuperacionClaveRepository,
                               AlumnoRepository alumnoRepository, PasswordEncoder passwordEncoder) {
        this.alumnoService = alumnoService;
        this.emailService = emailService;
        this.recuperacionClaveRepository = recuperacionClaveRepository;
        this.alumnoRepository = alumnoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1. Enviar código de recuperación
    @PostMapping("/enviar")
    public ResponseEntity<?> enviarCodigoRecuperacion(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        // Verificar que el correo existe
        Optional<Alumno> alumnoOptional = alumnoRepository.findByEmail(email);
        if (alumnoOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Correo no registrado"));
        }

        // Generar código y tiempo de expiración
        String recoveryCode = String.format("%06d", new Random().nextInt(1000000));
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(10);

        // Guardar en la tabla `recuperacion_clave`
        RecuperacionClave recuperacion = new RecuperacionClave();
        recuperacion.setEmail(email);
        recuperacion.setCodigoRecuperacion(recoveryCode);
        recuperacion.setFechaExpiracion(expirationTime);
        recuperacionClaveRepository.save(recuperacion);

        // Enviar código por correo
        emailService.sendPasswordRecoveryEmail(email, recoveryCode);

        return ResponseEntity.ok(Map.of("mensaje", "Código de recuperación enviado"));
    }

    // 2. Verificar código de recuperación
    @PostMapping("/verificar")
    public ResponseEntity<?> verificarCodigoRecuperacion(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String recoveryCode = request.get("codigo");

        // Verificar existencia del código
        Optional<RecuperacionClave> recuperacionOptional =
                recuperacionClaveRepository.findByEmailAndCodigoRecuperacion(email, recoveryCode);

        if (recuperacionOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Código de recuperación no encontrado"));
        }

        RecuperacionClave recuperacion = recuperacionOptional.get();

        // Validar si el código ha expirado o ya fue usado
        if (recuperacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "El código de recuperación ha expirado"));
        }

        if (recuperacion.getUsado()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "El código de recuperación ya fue usado"));
        }

        return ResponseEntity.ok(Map.of("mensaje", "Código válido"));
    }

    // 3. Cambiar contraseña
    @PostMapping("/cambiar")
    public ResponseEntity<?> cambiarContrasena(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String recoveryCode = request.get("codigo");
        String nuevaClave = request.get("nuevaClave");

        // Validar código de recuperación
        Optional<RecuperacionClave> recuperacionOptional =
                recuperacionClaveRepository.findByEmailAndCodigoRecuperacion(email, recoveryCode);

        if (recuperacionOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Código de recuperación no encontrado"));
        }

        RecuperacionClave recuperacion = recuperacionOptional.get();

        if (recuperacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "El código de recuperación ha expirado"));
        }

        if (recuperacion.getUsado()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "El código de recuperación ya fue usado"));
        }

        // Actualizar contraseña
        Optional<Alumno> alumnoOptional = alumnoRepository.findByEmail(email);
        if (alumnoOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Alumno no encontrado"));
        }

        Alumno alumno = alumnoOptional.get();
        alumno.setClave(passwordEncoder.encode(nuevaClave));
        alumnoRepository.save(alumno);

        // Marcar el código como usado
        recuperacion.setUsado(true);
        recuperacionClaveRepository.save(recuperacion);

        return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada exitosamente"));
    }
}
