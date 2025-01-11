package es.camporoberto.tfc.api.insticonnect.controllers;

import es.camporoberto.tfc.api.insticonnect.entidades.Instituto;
import es.camporoberto.tfc.api.insticonnect.repositories.InstitutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api")
public class InstitutoController {

    @Autowired
    private InstitutoRepository institutoRepository;

    @GetMapping("/institutos")
    public ResponseEntity<List<Instituto>> getInstitutosByRegion(@RequestParam String region) {
        try {
            List<Instituto> institutos = institutoRepository.findByComunidadAutonomaNombre(region);

            if (institutos.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(institutos, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

