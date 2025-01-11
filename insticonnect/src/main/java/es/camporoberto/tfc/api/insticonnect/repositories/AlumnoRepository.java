package es.camporoberto.tfc.api.insticonnect.repositories;

import es.camporoberto.tfc.api.insticonnect.dtos.AlumnoDTO;
import es.camporoberto.tfc.api.insticonnect.dtos.AlumnoInfoDTO;
import es.camporoberto.tfc.api.insticonnect.entidades.Alumno;
import es.camporoberto.tfc.api.insticonnect.entidades.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlumnoRepository extends JpaRepository<Alumno, Integer> {

    @Query("SELECT new es.camporoberto.tfc.api.insticonnect.dtos.AlumnoInfoDTO(" +
            "a.nombreCompleto, " +
            "CONCAT(cf.nombre, ' - ', c.anio, ' año'), " +
            "ca.nombre, " +
            "i.nombre, " +
            "c.anioLectivo, " +
            "a.email, " +
            "a.foto) " +
            "FROM Alumno a " +
            "JOIN a.grupo g " +
            "JOIN g.curso c " +
            "JOIN c.ciclo cf " +
            "JOIN c.instituto i " +
            "JOIN i.comunidadAutonoma ca " +
            "WHERE a.email = :email")
    Optional<AlumnoInfoDTO> findAlumnoInfoByEmail(@Param("email") String email);


    Optional<Alumno> findByEmail(String email);

    List<Alumno> findByIsVerifiedFalseAndTipoAndFechaCreacionBefore(Alumno.Tipo tipo, LocalDateTime fechaCreacion);

    List<Alumno> findAllByIsVerifiedFalse();

    List<Alumno> findByGrupo(Grupo grupo);


    @Query("SELECT new es.camporoberto.tfc.api.insticonnect.dtos.AlumnoDTO(" +
            "a.id, " +
            "a.nombreCompleto, " +
            "a.email, " +
            "a.codigoGrupo) " +
            "FROM Alumno a WHERE a.grupo = :grupo")
    List<AlumnoDTO> findAlumnoDTOByGrupo(@Param("grupo") Grupo grupo);

    List<Alumno> findByGrupoAndPuedePublicarTrue(Grupo grupo);

    @Query("SELECT a FROM Alumno a WHERE a.codigoGrupo = :codigoGrupo")
    List<Alumno> findByCodigoGrupo(@Param("codigoGrupo") String codigoGrupo);


    Optional<Alumno> findByNombreCompleto(String nombreCompleto);

    @Query("SELECT a FROM Alumno a " +
            "JOIN FETCH a.grupo g " +
            "JOIN FETCH g.curso c " +
            "JOIN FETCH c.ciclo cf " +
            "JOIN FETCH c.instituto i " +
            "WHERE a.nombreCompleto = :nombreCompleto")
    Optional<Alumno> findByNombreCompletoConRelaciones(@Param("nombreCompleto") String nombreCompleto);

    void deleteByGrupoId(Integer grupoId); // Eliminar todos los alumnos por grupo

}