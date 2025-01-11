package es.camporoberto.tfc.api.insticonnect.services;

import es.camporoberto.tfc.api.insticonnect.entidades.Comentarios;
import es.camporoberto.tfc.api.insticonnect.repositories.ComentariosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComentarioService {

    private final ComentariosRepository comentariosRepository;

    @Autowired
    public ComentarioService(ComentariosRepository comentariosRepository) {
        this.comentariosRepository = comentariosRepository;
    }

    public List<Comentarios> obtenerComentariosPorPublicacion(int publicacionId) {
        return comentariosRepository.findByPublicacionId(publicacionId);
    }

    public Comentarios guardarComentario(Comentarios comentarios) {
        return comentariosRepository.save(comentarios);
    }
}
