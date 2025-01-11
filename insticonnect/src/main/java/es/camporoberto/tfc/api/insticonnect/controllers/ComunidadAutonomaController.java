package es.camporoberto.tfc.api.insticonnect.controllers;

import es.camporoberto.tfc.api.insticonnect.entidades.ComunidadAutonoma;
import es.camporoberto.tfc.api.insticonnect.repositories.ComunidadAutonomaRepository;
import es.camporoberto.tfc.api.insticonnect.services.ComunidadAutonomaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/comunidades")
public class ComunidadAutonomaController {

    @Autowired
    private ComunidadAutonomaService comunidadAutonomaService;

    @Autowired
    private ComunidadAutonomaRepository comunidadAutonomaRepository;

    // Endpoint para obtener todas las comunidades autónomas
    @GetMapping
    public ResponseEntity<List<ComunidadAutonoma>> getAllComunidades() {
        List<ComunidadAutonoma> comunidades = comunidadAutonomaService.getAllComunidades();
        return ResponseEntity.ok(comunidades);
    }

    @GetMapping("/dominio")
    public ResponseEntity<?> getDominioCorreoByComunidad(@RequestParam("comunidad_autonoma") String nombre) {
        Optional<ComunidadAutonoma> comunidadOptional = comunidadAutonomaRepository.findByNombre(nombre);

        if (comunidadOptional.isPresent()) {
            ComunidadAutonoma comunidad = comunidadOptional.get();
            return ResponseEntity.ok().body(Map.of("dominio_correo", comunidad.getDominioCorreo()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comunidad autónoma no encontrada");
        }
    }

    @GetMapping("/dominio-correo")
    public ResponseEntity<?> getDominioCorreoByCodigoPrefijo(@RequestParam("codigo_prefijo") String codigoPrefijo) {
        Optional<ComunidadAutonoma> comunidadOptional = comunidadAutonomaRepository.findByCodigoPrefijo(codigoPrefijo);

        if (comunidadOptional.isPresent()) {
            ComunidadAutonoma comunidad = comunidadOptional.get();
            return ResponseEntity.ok().body(Map.of("dominio_correo", comunidad.getDominioCorreo()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Código prefijo no encontrado");
        }
    }

}
