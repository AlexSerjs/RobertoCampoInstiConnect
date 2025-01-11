package es.camporoberto.tfc.api.insticonnect.services;

import es.camporoberto.tfc.api.insticonnect.entidades.*;
import es.camporoberto.tfc.api.insticonnect.repositories.AlumnoRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.GrupoRepository;
import es.camporoberto.tfc.api.insticonnect.repositories.ComunidadAutonomaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class AlumnoService {

    private final GrupoRepository grupoRepository;
    private final AlumnoRepository alumnoRepository;
    private final ComunidadAutonomaRepository comunidadAutonomaRepository;

    @Autowired
    public AlumnoService(GrupoRepository grupoRepository, AlumnoRepository alumnoRepository, ComunidadAutonomaRepository comunidadAutonomaRepository) {
        this.grupoRepository = grupoRepository;
        this.alumnoRepository = alumnoRepository;
        this.comunidadAutonomaRepository = comunidadAutonomaRepository;
    }

    public boolean deleteAlumno(Integer id) {
        if (alumnoRepository.existsById(id)) {
            alumnoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Alumno> getAllAlumnos() {
        try {
            return alumnoRepository.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al obtener los alumnos");
        }
    }

    // Guardar un alumno
    public Alumno saveAlumno(Alumno alumno) {
        return alumnoRepository.save(alumno);
    }

    // Obtener un alumno por ID
    public Optional<Alumno> getAlumnoById(Integer id) {
        return alumnoRepository.findById(id);
    }

    // Actualizar un alumno
    public Optional<Alumno> updateAlumno(Integer id, Alumno alumnoDetails) {
        return alumnoRepository.findById(id).map(alumno -> {
            alumno.setNombreCompleto(alumnoDetails.getNombreCompleto());
            alumno.setFoto(alumnoDetails.getFoto());
            alumno.setCodigoGrupo(alumnoDetails.getCodigoGrupo());
            alumno.setEmail(alumnoDetails.getEmail());
            alumno.setClave(alumnoDetails.getClave());
            alumno.setTipo(alumnoDetails.getTipo());
            return alumnoRepository.save(alumno);
        });
    }

    public Alumno createDelegado(Alumno delegado, Curso curso) {
        // Verificar si ya existe un grupo para el curso específico.
        Optional<Grupo> existingGrupo = grupoRepository.findByCurso(curso);

        if (existingGrupo.isPresent()) {
            throw new IllegalStateException("Ya existe un grupo para este curso.");
        }

        // Crear el delegado (Alumno).
        delegado.setTipo(Alumno.Tipo.delegado);
        Alumno savedDelegado = alumnoRepository.save(delegado);

        // Crear el grupo y asociarlo con el delegado y el curso.
        Grupo grupo = new Grupo();
        grupo.setCodigoGrupo(generarCodigoGrupo(curso)); // Generar el código de grupo (por ejemplo, 'MAD-001').
        grupo.setCurso(curso);
        grupo.setDelegado(savedDelegado);
        grupoRepository.save(grupo);

        return savedDelegado;
    }

    private String generarCodigoGrupo(Curso curso) {
        // Obtener el instituto del curso
        Instituto instituto = curso.getInstituto();
        if (instituto == null) {
            throw new IllegalArgumentException("Instituto no válido");
        }

        // Obtener la comunidad autónoma del instituto
        ComunidadAutonoma comunidadAutonoma = instituto.getComunidadAutonoma();
        if (comunidadAutonoma == null) {
            throw new IllegalArgumentException("Comunidad Autónoma no válida");
        }

        // Obtener el sufijo de la comunidad autónoma desde la base de datos
        String sufijo = comunidadAutonoma.getCodigoPrefijo();
        if (sufijo == null) {
            throw new IllegalArgumentException("Sufijo no encontrado para la Comunidad Autónoma");
        }

        // Generar un número aleatorio de 4 dígitos
        String numeroAleatorio;
        do {
            numeroAleatorio = String.format("%04d", new Random().nextInt(10000));
        } while (codigoGrupoExiste(sufijo + "-" + numeroAleatorio));

        return sufijo + "-" + numeroAleatorio;
    }

    private boolean codigoGrupoExiste(String codigoGrupo) {
        // Verificar si el código de grupo ya existe en la base de datos
        return grupoRepository.existsByCodigoGrupo(codigoGrupo);
    }

    public Optional<Alumno> findByEmail(String email) {
        return alumnoRepository.findByEmail(email);
    }

    public List<Alumno> findNonVerifiedDelegatesBefore(LocalDateTime fechaCreacion) {
        return alumnoRepository.findByIsVerifiedFalseAndTipoAndFechaCreacionBefore(Alumno.Tipo.delegado, fechaCreacion);
    }

    public List<Alumno> findAllNoVerificados() {
        // Obtiene todos los alumnos que no están verificados
        return alumnoRepository.findAllByIsVerifiedFalse();
    }

    public void eliminarAlumno(Alumno alumno) {
        alumnoRepository.delete(alumno);
    }

    public List<Alumno> findByGrupo(Grupo grupo) {
        return alumnoRepository.findByGrupo(grupo);
    }


    // Método para obtener el grupo usando el email
    public Grupo obtenerGrupoPorEmail(String email) {
        Alumno alumno = alumnoRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado con email: " + email));
        Grupo grupo = alumno.getGrupo();
        if (grupo == null) {
            throw new RuntimeException("Grupo no asociado al alumno con email: " + email);
        }
        return grupo;
    }

    public String guardarFoto(String email, MultipartFile foto) throws IOException {
        // Validar el tipo de archivo
        String contentType = foto.getContentType();
        if (!contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
            throw new IllegalArgumentException("Solo se permiten imágenes JPEG y PNG.");
        }

        // Generar un nombre único para el archivo
        String nombreArchivo = "foto_" + email + "_" + System.currentTimeMillis() + ".jpg";

        // Ruta de destino
        Path rutaDestino = Paths.get("uploads").resolve(nombreArchivo);

        // Guardar el archivo
        Files.copy(foto.getInputStream(), rutaDestino, StandardCopyOption.REPLACE_EXISTING);

        // Actualizar la base de datos
        Alumno alumno = alumnoRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Alumno no encontrado"));
        alumno.setFoto("uploads/" + nombreArchivo);
        alumnoRepository.save(alumno);

        // Devolver la ruta relativa
        return "uploads/" + nombreArchivo;
    }


    public Optional<Alumno> getAlumnoByNombre(String nombreCompleto) {
        try {
            return alumnoRepository.findByNombreCompleto(nombreCompleto);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al buscar el alumno por nombre completo");
        }
    }

    // Verificar si el alumno es delegado
    public boolean esDelegado(Integer alumnoId) {
        return grupoRepository.existsByDelegadoId(alumnoId);
    }

    // Lógica para eliminar un delegado
    @Transactional
    public void eliminarDelegado(Alumno delegado) {
        // Obtener el grupo al que pertenece el delegado
        Grupo grupo = grupoRepository.findByDelegadoId(delegado.getId())
                .orElseThrow(() -> new IllegalStateException("Grupo no encontrado para el delegado"));

        // 1. Desasociar al delegado del grupo
        grupo.setDelegado(null);
        grupoRepository.save(grupo);

        // 2. Eliminar todos los alumnos asociados al grupo
        alumnoRepository.deleteByGrupoId(grupo.getId());

        // 3. Eliminar el grupo
        grupoRepository.deleteById(grupo.getId());

        // 4. Finalmente, eliminar el delegado
        alumnoRepository.deleteById(delegado.getId());
    }


}
