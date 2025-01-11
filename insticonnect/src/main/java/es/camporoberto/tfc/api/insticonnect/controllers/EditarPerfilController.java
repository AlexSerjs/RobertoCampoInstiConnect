package es.camporoberto.tfc.api.insticonnect.controllers;

import es.camporoberto.tfc.api.insticonnect.entidades.Alumno;
import es.camporoberto.tfc.api.insticonnect.handler.DatosAlumnoWebSocketHandler;
import es.camporoberto.tfc.api.insticonnect.repositories.AlumnoRepository;
import es.camporoberto.tfc.api.insticonnect.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/perfil")
public class EditarPerfilController {

    private final JwtUtil jwtUtil;
    private final AlumnoRepository alumnoRepository;
    private final PasswordEncoder passwordEncoder;
    private final DatosAlumnoWebSocketHandler datosAlumnoWebSocketHandler;

    @Autowired
    public EditarPerfilController(JwtUtil jwtUtil, AlumnoRepository alumnoRepository, PasswordEncoder passwordEncoder, DatosAlumnoWebSocketHandler datosAlumnoWebSocketHandler) {
        this.jwtUtil = jwtUtil;
        this.alumnoRepository = alumnoRepository;
        this.passwordEncoder = passwordEncoder;
        this.datosAlumnoWebSocketHandler = datosAlumnoWebSocketHandler;
    }

    @PutMapping("/editar")
    public ResponseEntity<?> editarPerfil(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> datosPerfil) {
        try {
            // Extraer el email del token
            String email = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            Optional<Alumno> alumnoOptional = alumnoRepository.findByEmail(email);

            if (alumnoOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Usuario no encontrado."));
            }

            Alumno alumno = alumnoOptional.get();

            // Actualizar el nombre completo si está presente
            if (datosPerfil.containsKey("nombreCompleto")) {
                String nuevoNombre = datosPerfil.get("nombreCompleto").trim();
                if (!nuevoNombre.isEmpty()) {
                    alumno.setNombreCompleto(nuevoNombre);

                    // Notificar a los clientes
                    datosAlumnoWebSocketHandler.notifyClients(
                            "ACTUALIZACION_NOMBRE",
                            String.format("Nombre actualizado a '%s'", nuevoNombre)
                    );

                } else {
                    return ResponseEntity.badRequest().body(Map.of("error", "El nombre completo no puede estar vacío."));
                }
            }

            // Actualizar la contraseña si se proporciona
            if (datosPerfil.containsKey("contrasenaActual") && datosPerfil.containsKey("nuevaContrasena")) {
                String contrasenaActual = datosPerfil.get("contrasenaActual").trim();
                String nuevaContrasena = datosPerfil.get("nuevaContrasena").trim();

                if (!passwordEncoder.matches(contrasenaActual, alumno.getClave())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "La contraseña actual es incorrecta."));
                }

                if (nuevaContrasena.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "La nueva contraseña no puede estar vacía."));
                }

                alumno.setClave(passwordEncoder.encode(nuevaContrasena));

                // Notificar a los clientes
                datosAlumnoWebSocketHandler.notifyClients(
                        "ACTUALIZACION_CONTRASENA",
                        String.format("La contraseña de %s ha sido actualizada.", alumno.getEmail())
                );
            }

            // Guardar los cambios
            alumnoRepository.save(alumno);

            return ResponseEntity.ok(Map.of("message", "Perfil actualizado exitosamente."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Ocurrió un error al actualizar el perfil."));
        }
    }
}
