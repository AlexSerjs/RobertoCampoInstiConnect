package es.camporoberto.tfc.api.insticonnect.repositories;

import es.camporoberto.tfc.api.insticonnect.entidades.Publicacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface PublicacionRepository extends JpaRepository<Publicacion, Integer> {

    List<Publicacion> findByTipoContenido(Publicacion.TipoContenido tipoContenido);

    List<Publicacion> findByGrupoId(Integer grupoId);
    Page<Publicacion> findByGrupoId(Integer grupoId, Pageable pageable);

    List<Publicacion> findByGrupoIdAndTipoContenido(Integer grupoId, Publicacion.TipoContenido tipoContenido);

}
