package es.camporoberto.tfc.api.insticonnect.services;

import es.camporoberto.tfc.api.insticonnect.entidades.Alumno;
import es.camporoberto.tfc.api.insticonnect.repositories.AlumnoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AlumnoRepository alumnoRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Alumno alumno = alumnoRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el correo: " + email));

        return new org.springframework.security.core.userdetails.User(
                alumno.getEmail(),
                alumno.getClave(),
                new ArrayList<>()
        );
    }

    // Método adicional para buscar por ID
    public UserDetails loadUserById(Integer id) throws UsernameNotFoundException {
        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el ID: " + id));

        return new org.springframework.security.core.userdetails.User(
                alumno.getEmail(),
                alumno.getClave(),
                new ArrayList<>()
        );
    }
}
