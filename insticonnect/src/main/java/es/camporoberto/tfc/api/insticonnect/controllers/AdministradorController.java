package es.camporoberto.tfc.api.insticonnect.controllers;

import es.camporoberto.tfc.api.insticonnect.entidades.Administrador;
import es.camporoberto.tfc.api.insticonnect.repositories.AdministradorRepository;

import es.camporoberto.tfc.api.insticonnect.services.AdministradorService;
import es.camporoberto.tfc.api.insticonnect.util.JwtUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/administradores")
public class AdministradorController {

    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AdministradorService administradorService;

    @Autowired
    public AdministradorController(AdministradorRepository administradorRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AdministradorService administradorService) {
        this.administradorRepository = administradorRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.administradorService = administradorService;
    }

    // Registro de un nuevo administrador
    @PostMapping("/registro")
    public ResponseEntity<Map<String, Object>> registrarAdministrador(@RequestBody AdministradorRequest request) {
        try {
            // Verificar si el email ya está registrado
            if (administradorRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "El correo electrónico ya está registrado."));
            }

            // Crear un nuevo objeto Administrador
            Administrador administrador = new Administrador();
            administrador.setNombre(request.getNombre());
            administrador.setEmail(request.getEmail());
            administrador.setContrasena(passwordEncoder.encode(request.getContrasena()));
            administrador.setEstado("activo");

            // Guardar en la base de datos
            Administrador savedAdministrador = administradorRepository.save(administrador);

            // Generar un token JWT para el administrador
            String token = jwtUtil.generateToken(savedAdministrador.getEmail());

            // Preparar la respuesta
            Map<String, Object> response = new HashMap<>();
            response.put("administrador", savedAdministrador);
            response.put("token", token);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping
    public ResponseEntity<?> getAllAdministradores() {
        try {
            return ResponseEntity.ok(administradorRepository.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error al obtener administradores"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarAdministrador(@PathVariable Integer id) {
        try {
            if (administradorRepository.existsById(id)) {
                administradorRepository.deleteById(id);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Administrador no encontrado"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error al eliminar el administrador"));
        }
    }

    @Getter
    public static class AdministradorRequest {
        private String nombre;
        private String email;
        private String contrasena;
    }

   //----------------------------------------------------------------------------------------------------------------------------------

    @PostMapping("/api/administradores/detalles")
    public ResponseEntity<?> obtenerDetallesAdministrador(@RequestHeader("Authorization") String token) {
        try {
            String nombre = jwtUtil.extractUsername(token.replace("Bearer ", ""));

            Optional<Administrador> adminOptional = administradorService.findByNombre(nombre);

            if (adminOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Administrador no encontrado."));
            }

            Administrador administrador = adminOptional.get();

            // Responder con nombre y email
            Map<String, String> response = new HashMap<>();
            response.put("nombre", administrador.getNombre());
            response.put("email", administrador.getEmail());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener los detalles del administrador."));
        }
    }




}
