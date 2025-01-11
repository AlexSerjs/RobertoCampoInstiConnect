package es.camporoberto.tfc.api.insticonnect.controllers;

import es.camporoberto.tfc.api.insticonnect.services.GrupoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/grupos")
public class AdminGruposController {

    private final GrupoService grupoService;

    @Autowired
    public AdminGruposController(GrupoService grupoService) {
        this.grupoService = grupoService;
    }

    @GetMapping("/detalles")
    public ResponseEntity<?> getGroupDetails() {
        try {
            List<Map<String, Object>> groupDetails = grupoService.getGroupDetails();
            return ResponseEntity.ok(groupDetails);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }
}
