package es.camporoberto.tfc.api.insticonnect.controllers;
import es.camporoberto.tfc.api.insticonnect.dtos.ComentarioDTO;
import es.camporoberto.tfc.api.insticonnect.entidades.Alumno;
import es.camporoberto.tfc.api.insticonnect.entidades.Grupo;
import es.camporoberto.tfc.api.insticonnect.entidades.Publicacion;
import es.camporoberto.tfc.api.insticonnect.handler.NotificacionWebSocketHandler;
import es.camporoberto.tfc.api.insticonnect.repositories.AlumnoRepository;
import es.camporoberto.tfc.api.insticonnect.services.PublicacionService;
import es.camporoberto.tfc.api.insticonnect.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/imagenes")
public class ImagenController {

    private final String UPLOAD_DIR = "uploads/";
    private final JwtUtil jwtUtil;
    private final AlumnoRepository alumnoRepository;
    private final PublicacionService publicacionService;
    private final NotificacionWebSocketHandler notificacionWebSocketHandler;


    public ImagenController(JwtUtil jwtUtil, AlumnoRepository alumnoRepository,
                            PublicacionService publicacionService, NotificacionWebSocketHandler notificacionWebSocketHandler) {
        this.jwtUtil = jwtUtil;
        this.alumnoRepository = alumnoRepository;
        this.publicacionService = publicacionService;
        this.notificacionWebSocketHandler = notificacionWebSocketHandler;
    }


    @PostMapping("/subir")
    public ResponseEntity<Map<String, Object>> subirImagen(@RequestParam("imagen") MultipartFile archivo,
                                                           @RequestParam("titulo") String titulo,
                                                           @RequestHeader("Authorization") String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Validar el token y obtener el usuario
            String email = jwtUtil.extractUsername(token.replace("Bearer ", ""));
            Optional<Alumno> alumnoOptional = alumnoRepository.findByEmail(email);

            if (alumnoOptional.isEmpty()) {
                response.put("error", "Usuario no autorizado");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            Alumno alumno = alumnoOptional.get();
            if (!alumno.getPuedePublicar()) {
                response.put("error", "No tienes permiso para publicar imágenes");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }

            Grupo grupo = alumno.getGrupo();
            if (grupo == null) {
                response.put("error", "No estás asignado a un grupo");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            if (archivo.isEmpty()) {
                response.put("error", "No se ha enviado ningún archivo");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            if (titulo == null || titulo.trim().isEmpty()) {
                response.put("error", "El título es obligatorio");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Crear el directorio si no existe
            Path rutaCarpeta = Paths.get(UPLOAD_DIR).toAbsolutePath();
            if (!Files.exists(rutaCarpeta)) {
                Files.createDirectories(rutaCarpeta);
            }

            String nombreArchivo = UUID.randomUUID().toString() + "_" + archivo.getOriginalFilename();
            Path rutaArchivo = rutaCarpeta.resolve(nombreArchivo);

            Files.copy(archivo.getInputStream(), rutaArchivo);

            Publicacion publicacion = new Publicacion();
            publicacion.setTitulo(titulo); // Incluye el título
            publicacion.setContenido("Imagen subida");
            publicacion.setTipoContenido(Publicacion.TipoContenido.imagen);
            publicacion.setFechaCreacion(new Date());
            publicacion.setImagenUrl(nombreArchivo);
            publicacion.setGrupo(grupo);
            publicacion.setUsuario(alumno);

            publicacionService.guardar(publicacion);

            notificacionWebSocketHandler.notifyClients(
                    "NUEVA_IMAGEN",
                    String.format("Nueva imagen subida: %s por %s", titulo, alumno.getNombreCompleto())
            );

            response.put("mensaje", "Archivo subido exitosamente");
            response.put("url", "/uploads/" + nombreArchivo);
            response.put("grupo", Map.of("id", grupo.getId(), "codigoGrupo", grupo.getCodigoGrupo()));
            response.put("autor", Map.of("nombre", alumno.getNombreCompleto(), "email", alumno.getEmail()));

            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (IOException e) {
            e.printStackTrace();
            response.put("error", "Error al guardar el archivo: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error interno: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}
