package es.camporoberto.tfc.api.insticonnect.controllers;

import es.camporoberto.tfc.api.insticonnect.dtos.ComunidadAutonomaDTO;
import es.camporoberto.tfc.api.insticonnect.dtos.CursoDTO;
import es.camporoberto.tfc.api.insticonnect.dtos.GrupoDTO;
import es.camporoberto.tfc.api.insticonnect.entidades.ComunidadAutonoma;
import es.camporoberto.tfc.api.insticonnect.entidades.Curso;
import es.camporoberto.tfc.api.insticonnect.entidades.Instituto;
import es.camporoberto.tfc.api.insticonnect.services.ComunidadAutonomaService;
import es.camporoberto.tfc.api.insticonnect.services.CursoService;
import es.camporoberto.tfc.api.insticonnect.services.GrupoService;
import es.camporoberto.tfc.api.insticonnect.services.InstitutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/instituto")
public class AdminInstitutoController {
    private final ComunidadAutonomaService comunidadAutonomaService;
    private final InstitutoService institutoService;
    private final CursoService cursoService;
    private final GrupoService grupoService;

    @Autowired
    public AdminInstitutoController(ComunidadAutonomaService comunidadAutonomaService, InstitutoService institutoService, CursoService cursoService, GrupoService grupoService) {
        this.comunidadAutonomaService = comunidadAutonomaService;
        this.institutoService = institutoService;
        this.cursoService = cursoService;
        this.grupoService = grupoService;
    }

    @GetMapping("/comunidades")
    public ResponseEntity<?> getComunidades() {
        try {
            List<ComunidadAutonoma> comunidades = comunidadAutonomaService.getAllComunidades();

            // Convertir las entidades a DTOs
            List<ComunidadAutonomaDTO> response = comunidades.stream()
                    .map(comunidad -> new ComunidadAutonomaDTO(comunidad.getId(), comunidad.getNombre()))
                    .toList();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    @GetMapping("/comunidades/{id}/institutos")
    public ResponseEntity<?> getInstitutosByComunidad(@PathVariable("id") Integer id) {
        try {
            List<Instituto> institutos = institutoService.getInstitutosByComunidad(id);

            if (institutos.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "No se encontraron institutos para la comunidad autónoma especificada."));
            }

            return ResponseEntity.ok(institutos);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    @GetMapping("/institutos/{id}/cursos")
    public ResponseEntity<?> getCursosByInstituto(@PathVariable("id") Integer institutoId) {
        try {
            List<CursoDTO> cursos = cursoService.getCursosByInstitutoId(institutoId);

            if (cursos.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "No se encontraron cursos para el instituto especificado."));
            }

            return ResponseEntity.ok(cursos);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }


    @GetMapping("/institutos/{id}/grupos")
    public ResponseEntity<?> getGruposByInstituto(@PathVariable("id") Integer institutoId) {
        try {
            List<GrupoDTO> grupos = grupoService.getGruposByInstitutoId(institutoId);
            if (grupos.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of("error", "No se encontraron grupos para el instituto especificado."));
            }
            return ResponseEntity.ok(grupos);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }




}
