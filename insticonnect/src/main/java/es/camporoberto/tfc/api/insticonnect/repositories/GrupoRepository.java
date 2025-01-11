package es.camporoberto.tfc.api.insticonnect.repositories;

import es.camporoberto.tfc.api.insticonnect.entidades.Curso;
import es.camporoberto.tfc.api.insticonnect.entidades.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Integer> {
    Optional<Grupo> findByCurso(Curso curso);
    boolean existsByCodigoGrupo(String codigoGrupo);
    Optional<Grupo> findByCodigoGrupo(String codigoGrupo);
    int countByCursoId(int cursoId);

    Optional<Grupo> findByDelegadoId(int delegadoId);


    @Query("SELECT g.codigoGrupo FROM Grupo g WHERE g.delegado.id = :delegadoId")
    Optional<String> findCodigoGrupoByDelegadoId(@Param("delegadoId") Integer delegadoId);

    Optional<Grupo> findByDelegadoId(Integer delegadoId); // Buscar el grupo por delegado
    boolean existsByDelegadoId(Integer delegadoId);
    void deleteById(Integer id);



    @Query(value = """
        SELECT 
            gr.id AS idGrupo,
            gr.codigo_grupo AS codigoGrupo,
            gr.delegado_id AS delegadoId,
            al.nombre_completo AS nombreDelegado,
            al.email AS emailDelegado,
            COUNT(a.id) AS numeroAlumnosDelGrupo,
            cf.nombre AS nombreCiclo,
            cf.anio AS anioCiclo
        FROM grupos gr
        JOIN alumnos al ON al.id = gr.delegado_id
        JOIN cursos cu ON cu.id = gr.curso_id
        JOIN ciclos_formativos cf ON cf.id = cu.ciclo_id
        LEFT JOIN alumnos a ON a.grupo_id = gr.id
        GROUP BY 
            gr.id, 
            gr.codigo_grupo, 
            gr.delegado_id, 
            al.nombre_completo, 
            al.email, 
            cf.nombre, 
            cf.anio
        """, nativeQuery = true)
    List<Map<String, Object>> getGroupDetails();


    @Query("SELECT g FROM Grupo g WHERE g.curso.instituto.id = :institutoId")
    List<Grupo> findByInstitutoId(@Param("institutoId") Integer institutoId);
}
