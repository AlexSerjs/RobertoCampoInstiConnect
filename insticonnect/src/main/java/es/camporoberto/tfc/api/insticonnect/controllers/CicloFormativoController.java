package es.camporoberto.tfc.api.insticonnect.controllers;

import es.camporoberto.tfc.api.insticonnect.entidades.CicloFormativo;
import es.camporoberto.tfc.api.insticonnect.services.CicloFormativoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CicloFormativoController {

    @Autowired
    private CicloFormativoService cicloFormativoService;

    @GetMapping("/ciclos_formativos")
    public ResponseEntity<List<CicloFormativo>> getCiclosFormativosByInstitutoNombre(@RequestParam("institutoNombre") String institutoNombre) {
        List<CicloFormativo> ciclosFormativos = cicloFormativoService.getCiclosFormativosByInstitutoNombre(institutoNombre);
        if (ciclosFormativos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(ciclosFormativos);
    }

}
