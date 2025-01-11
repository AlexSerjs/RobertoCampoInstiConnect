package es.camporoberto.tfc.api.insticonnect.services;

import es.camporoberto.tfc.api.insticonnect.entidades.Administrador;
import es.camporoberto.tfc.api.insticonnect.repositories.AdministradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdministradorService {

    @Autowired
    private AdministradorRepository administradorRepository;

    public Optional<Administrador> findByNombre(String nombre) {
        return administradorRepository.findByNombre(nombre);
    }
}
