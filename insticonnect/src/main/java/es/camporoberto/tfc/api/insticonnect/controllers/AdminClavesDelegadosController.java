package es.camporoberto.tfc.api.insticonnect.controllers;

import es.camporoberto.tfc.api.insticonnect.dtos.ClaveDetalleDTO;
import es.camporoberto.tfc.api.insticonnect.dtos.GenerarClavesRequestDTO;
import es.camporoberto.tfc.api.insticonnect.entidades.Administrador;
import es.camporoberto.tfc.api.insticonnect.entidades.ClavesDelegados;
import es.camporoberto.tfc.api.insticonnect.entidades.ComunidadAutonoma;
import es.camporoberto.tfc.api.insticonnect.entidades.Instituto;
import es.camporoberto.tfc.api.insticonnect.repositories.AdministradorRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.ClavesDelegadosRepository;
import es.camporoberto.tfc.api.insticonnect.services.ComunidadAutonomaService;
import es.camporoberto.tfc.api.insticonnect.services.InstitutoService;
import es.camporoberto.tfc.api.insticonnect.util.JwtUtil;
import es.camporoberto.tfc.api.insticonnect.websocket.ClavesWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/claves")
public class AdminClavesDelegadosController {

    private final ComunidadAutonomaService comunidadAutonomaService;
    private final InstitutoService institutoService;
    private final ClavesDelegadosRepository clavesDelegadosRepository;
    private final JwtUtil jwtUtil;
    private final AdministradorRepository administradorRepository;
    private final ClavesWebSocketHandler clavesWebSocketHandler;

    @Autowired
    public AdminClavesDelegadosController(ComunidadAutonomaService comunidadAutonomaService,
                                          InstitutoService institutoService,
                                          ClavesDelegadosRepository clavesDelegadosRepository,
                                          JwtUtil jwtUtil,
                                          AdministradorRepository administradorRepository, ClavesWebSocketHandler clavesWebSocketHandler) {
        this.comunidadAutonomaService = comunidadAutonomaService;
        this.institutoService = institutoService;
        this.clavesDelegadosRepository = clavesDelegadosRepository;
        this.jwtUtil = jwtUtil;
        this.administradorRepository = administradorRepository;
        this.clavesWebSocketHandler = clavesWebSocketHandler;
    }

    @PostMapping("/generar")
    public ResponseEntity<?> generarClaves(@RequestHeader("Authorization") String token,
                                           @RequestBody GenerarClavesRequestDTO request) {
        try {
            System.out.println("Token recibido: " + token);

            // Eliminar prefijo "Bearer"
            token = token.replace("Bearer ", "");

            // Extraer el nombre del administrador del token
            String nombre = jwtUtil.extractUsername(token);
            System.out.println("Nombre extraído del token: " + nombre);

            // Buscar al administrador por nombre
            Administrador administrador = administradorRepository.findByNombre(nombre)
                    .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));

            // Validar estado del administrador
            if (!"activo".equalsIgnoreCase(administrador.getEstado())) {
                throw new RuntimeException("El administrador no está activo");
            }

            // Obtener comunidad e instituto
            ComunidadAutonoma comunidad = comunidadAutonomaService.findById(request.getComunidadId())
                    .orElseThrow(() -> new RuntimeException("Comunidad Autónoma no encontrada"));

            Instituto instituto = institutoService.findById(request.getInstitutoId())
                    .orElseThrow(() -> new RuntimeException("Instituto no encontrado"));

            // Generar claves
            List<String> clavesGeneradas = new ArrayList<>();
            Random random = new Random();

            for (int i = 0; i < request.getCantidad(); i++) {
                String clave = generarNumerosAleatorios(6, random);

                while (clavesDelegadosRepository.existsByClave(clave)) {
                    clave = generarNumerosAleatorios(6, random);
                }

                ClavesDelegados nuevaClave = new ClavesDelegados();
                nuevaClave.setClave(clave);
                nuevaClave.setComunidadAutonoma(comunidad);
                nuevaClave.setInstituto(instituto);
                nuevaClave.setAdministrador(administrador);
                nuevaClave.setEstado(false);
                nuevaClave.setFechaCreacion(LocalDateTime.now());

                clavesDelegadosRepository.save(nuevaClave);
                clavesGeneradas.add(clave);
            }

            System.out.println("Claves generadas correctamente: " + clavesGeneradas);
            clavesWebSocketHandler.notifyClients("Nuevas claves generadas: " + clavesGeneradas);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Claves generadas correctamente.",
                    "claves", clavesGeneradas
            ));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error durante la generación de claves: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al generar claves"));
        }
    }



    private boolean validarAdministrador(String identificador) {
        System.out.println("Iniciando validación del administrador con identificador: " + identificador);

        // Buscar al administrador con ID 1
        return administradorRepository.findById(1)
                .map(administrador -> {
                    System.out.println("Nombre del administrador en la base de datos: " + administrador.getNombre());
                    System.out.println("¿Coinciden los nombres? " + administrador.getNombre().equals(identificador));
                    return administrador.getNombre().equals(identificador);
                })
                .orElseGet(() -> {
                    System.out.println("No se encontró un administrador con ID 1 en la base de datos.");
                    return false;
                });
    }



    private String generarNumerosAleatorios(int length, Random random) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }


    @GetMapping("/todas")
    public ResponseEntity<List<ClaveDetalleDTO>> obtenerTodasLasClaves() {
        List<ClavesDelegados> claves = clavesDelegadosRepository.findAll();
        List<ClaveDetalleDTO> detalles = claves.stream().map(clave -> {
            ClaveDetalleDTO dto = new ClaveDetalleDTO();
            dto.setId(clave.getId());
            dto.setClave(clave.getClave());
            dto.setComunidadNombre(clave.getComunidadAutonoma().getNombre());
            dto.setInstitutoNombre(clave.getInstituto().getNombre());
            dto.setAdministradorNombre("Administrador");
            dto.setEstado(clave.getEstado());
            dto.setFechaCreacion(clave.getFechaCreacion().toString());
            dto.setFechaUtilizacion(
                    clave.getFechaUtilizacion() != null ? clave.getFechaUtilizacion().toString() : null
            );
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(detalles);
    }

    @GetMapping("/verificar-clave")
    public ResponseEntity<Map<String, Object>> verificarClave(@RequestParam("clave") String clave) {
        try {
            // Buscar la clave en la base de datos
            ClavesDelegados claveDelegado = clavesDelegadosRepository.findByClave(clave)
                    .orElse(null);

            if (claveDelegado == null) {
                // Si no existe la clave
                return ResponseEntity.ok(Map.of(
                        "valida", false,
                        "mensaje", "Clave no encontrada"
                ));
            }

            if (claveDelegado.getEstado()) {
                // Si la clave ya fue utilizada
                return ResponseEntity.ok(Map.of(
                        "valida", false,
                        "mensaje", "Clave ya utilizada"
                ));
            }

            // Marcar la clave como utilizada
            claveDelegado.setEstado(true);
            claveDelegado.setFechaUtilizacion(LocalDateTime.now());
            clavesDelegadosRepository.save(claveDelegado);

            // Responder que la clave es válida
            return ResponseEntity.ok(Map.of(
                    "valida", true,
                    "mensaje", "Clave válida y actualizada como utilizada"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "valida", false,
                            "mensaje", "Error al procesar la solicitud"
                    ));
        }
    }



}
