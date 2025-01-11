package es.camporoberto.tfc.api.insticonnect.controllers;

import es.camporoberto.tfc.api.insticonnect.entidades.Alumno;
import es.camporoberto.tfc.api.insticonnect.entidades.Publicacion;
import es.camporoberto.tfc.api.insticonnect.handler.NotificacionWebSocketHandler;
import es.camporoberto.tfc.api.insticonnect.repositories.AlumnoRepository;
import es.camporoberto.tfc.api.insticonnect.services.PublicacionService;
import es.camporoberto.tfc.api.insticonnect.util.JwtUtil;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/ficheros")
public class FicherosController {

    private final String UPLOAD_DIR = "uploads/ficheros/";
    private final JwtUtil jwtUtil;
    private final AlumnoRepository alumnoRepository;
    private final PublicacionService publicacionService;
    private final NotificacionWebSocketHandler notificacionWebSocketHandler;


    public FicherosController(JwtUtil jwtUtil, AlumnoRepository alumnoRepository,
                              PublicacionService publicacionService, NotificacionWebSocketHandler notificacionWebSocketHandler) {
        this.jwtUtil = jwtUtil;
        this.alumnoRepository = alumnoRepository;
        this.publicacionService = publicacionService;
        this.notificacionWebSocketHandler = notificacionWebSocketHandler;
    }

    @PostMapping("/subir")
    public ResponseEntity<Map<String, Object>> subirFichero(
            @RequestParam("fichero") MultipartFile archivo,
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

            // Verificar si el usuario tiene permiso para publicar
            if (!alumno.getPuedePublicar()) {
                response.put("error", "No tienes permiso para subir ficheros");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }

            // Validar el título
            if (titulo == null || titulo.trim().isEmpty()) {
                response.put("error", "El título es obligatorio");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Convertir el título a mayúsculas
            titulo = titulo.toUpperCase();

            // Verificar si el fichero es válido
            if (archivo.isEmpty()) {
                response.put("error", "No se ha enviado ningún fichero");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Validar el tipo de archivo permitido
            String contentType = archivo.getContentType();
            if (!esTipoDeFicheroValido(contentType)) {
                response.put("error", "El tipo de archivo no está permitido");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            // Crear el directorio si no existe
            Path rutaCarpeta = Paths.get(UPLOAD_DIR).toAbsolutePath();
            if (!Files.exists(rutaCarpeta)) {
                Files.createDirectories(rutaCarpeta); // Esto crea la carpeta si no existe
            }

            // Generar un nombre único para el fichero
            String nombreFichero = UUID.randomUUID().toString() + "_" + archivo.getOriginalFilename();
            Path rutaFichero = rutaCarpeta.resolve(nombreFichero);

            // Guardar el fichero
            Files.copy(archivo.getInputStream(), rutaFichero);

            // Crear una nueva publicación asociada al grupo y al alumno
            Publicacion publicacion = new Publicacion();
            publicacion.setTitulo(titulo); // Asignar el título a la publicación
            publicacion.setContenido("Fichero subido"); // Puedes personalizar este campo si es necesario
            publicacion.setTipoContenido(Publicacion.TipoContenido.archivo); // Establecer como tipo archivo
            publicacion.setFechaCreacion(new Date());
            publicacion.setArchivoUrl(nombreFichero); // Usamos archivoUrl para almacenar el nombre del archivo
            publicacion.setGrupo(alumno.getGrupo()); // Asociar al grupo
            publicacion.setUsuario(alumno); // Asociar al alumno que subió el fichero

            publicacionService.guardar(publicacion);
            // Notificar a los clientes conectados
            notificacionWebSocketHandler.notifyClients(
                    "NUEVA_PUBLICACION_ARCHIVO",
                    String.format("Nuevo archivo subido: %s por %s", titulo, alumno.getNombreCompleto())
            );

            // Respuesta exitosa
            response.put("mensaje", "Fichero subido exitosamente");
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (IOException e) {
            e.printStackTrace();
            response.put("error", "Error al guardar el fichero: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error interno: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private boolean esTipoDeFicheroValido(String contentType) {
        return contentType.equals("application/pdf") ||
                contentType.equals("application/msword") || // DOC
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") || // DOCX
                contentType.equals("application/vnd.ms-powerpoint") || // PPT
                contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation") || // PPTX
                contentType.equals("application/vnd.ms-excel") || // XLS
                contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // XLSX
    }


    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerFichero(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Obtener la publicación por ID
            Publicacion publicacion = publicacionService.obtenerPorId(id);

            if (publicacion == null || publicacion.getTipoContenido() != Publicacion.TipoContenido.archivo) {
                response.put("error", "Fichero no encontrado");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            // Obtener los detalles de la publicación
            response.put("id", publicacion.getId());
            response.put("documentoUrl", "/ficheros/" + publicacion.getImagenUrl()); // URL del fichero
            response.put("fechaCreacion", publicacion.getFechaCreacion());
            response.put("autor", Map.of(
                    "nombre", publicacion.getUsuario().getNombreCompleto(),
                    "email", publicacion.getUsuario().getEmail()
            ));

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error al obtener el fichero: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/descargar/{id}")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable int id) {
        try {
            // Buscar la publicación por su ID
            Publicacion publicacion = publicacionService.obtenerPorId(id);

            if (publicacion == null || publicacion.getTipoContenido() != Publicacion.TipoContenido.archivo) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Cambiar 'null' por un recurso vacío si es necesario
            }

            // Ruta completa del archivo asociado a la publicación
            Path rutaFichero = Paths.get("C:/Users/alexs/OneDrive/Desktop/robertoCampoTFC/insticonnect/uploads/ficheros")
                    .resolve(publicacion.getArchivoUrl())
                    .normalize();

            // Verificar que el archivo existe
            if (!Files.exists(rutaFichero)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Crear el recurso del archivo
            Resource recurso = new UrlResource(rutaFichero.toUri());

            if (!recurso.exists() || !recurso.isReadable()) {
                throw new IOException("No se puede leer el archivo: " + publicacion.getArchivoUrl());
            }

            // Obtener el tipo de contenido del archivo
            String tipoContenido = Files.probeContentType(rutaFichero);
            if (tipoContenido == null) {
                tipoContenido = "application/octet-stream";
            }

            // Configurar la respuesta con el recurso
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(tipoContenido))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
                    .body(recurso);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Aquí también asegúrate de devolver null correctamente
        }
    }


}

