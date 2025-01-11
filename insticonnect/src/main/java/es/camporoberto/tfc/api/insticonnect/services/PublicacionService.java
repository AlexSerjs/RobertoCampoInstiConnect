package es.camporoberto.tfc.api.insticonnect.services;

import es.camporoberto.tfc.api.insticonnect.entidades.Publicacion;
import es.camporoberto.tfc.api.insticonnect.repositories.PublicacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;

    @Autowired
    public PublicacionService(PublicacionRepository publicacionRepository) {
        this.publicacionRepository = publicacionRepository;
    }

    public Publicacion guardar(Publicacion publicacion) {
        return publicacionRepository.save(publicacion);
    }

    public List<Publicacion> obtenerPorTipo(Publicacion.TipoContenido tipoContenido) {
        return publicacionRepository.findByTipoContenido(tipoContenido);
    }

    public Publicacion obtenerPorId(int id) {
        return publicacionRepository.findById(id).orElse(null);
    }

    public void eliminar(int id) {
        publicacionRepository.deleteById(id);
    }

    public List<Publicacion> obtenerPorGrupo(Integer grupoId) {
        return publicacionRepository.findByGrupoId(grupoId);
    }

    public Page<Publicacion> obtenerPorGrupoPaginadas(Integer grupoId, Pageable pageable) {
        return publicacionRepository.findByGrupoId(grupoId, pageable);
    }

    public List<Publicacion> obtenerPorGrupoYTipo(Integer grupoId, Publicacion.TipoContenido tipoContenido) {
        return publicacionRepository.findByGrupoIdAndTipoContenido(grupoId, tipoContenido);
    }

}
