package es.camporoberto.tfc.api.insticonnect;

import es.camporoberto.tfc.api.insticonnect.config.JwtConfig;
import es.camporoberto.tfc.api.insticonnect.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtUtilTest {

    @Mock
    private JwtConfig jwtConfig;

    @InjectMocks
    private JwtUtil jwtUtil;

    private final String secretKey = "mySecretKey";
    private final Long expirationMs = 1000L * 60 * 60; // 1 hora
    private final String username = "testuser";

    @BeforeEach
    public void configurar() {
        Mockito.when(jwtConfig.getSecretKey()).thenReturn(secretKey);
        Mockito.when(jwtConfig.getExpirationMs()).thenReturn(expirationMs);
    }

    @Test
    public void generarToken() {
        String token = jwtUtil.generateToken(username);
        System.out.println("Token generado: " + token);
        assertNotNull(token, "El token generado no debe ser nulo");
        assertTrue(token.length() > 0, "El token debe tener contenido");
    }

    @Test
    public void extraerNombreDeUsuario() {
        String token = jwtUtil.generateToken(username);
        String nombreExtraido = jwtUtil.extractUsername(token);
        assertEquals(username, nombreExtraido);}

    @Test
    public void extraerFechaDeExpiracion() {
        String token = jwtUtil.generateToken(username);
        Date expiracion = jwtUtil.extractExpiration(token);
        assertNotNull(expiracion);
        assertTrue(expiracion.after(new Date()));}

    @Test
    public void validarTokenValido() {
        String token = jwtUtil.generateToken(username);
        boolean esValido = jwtUtil.validateToken(token, username);
        assertTrue(esValido);}

    @Test
    public void validarTokenExpirado() throws InterruptedException {
        Mockito.when(jwtConfig.getExpirationMs()).thenReturn(1L); // Token expira en 1 ms
        String token = jwtUtil.generateToken(username);
        Thread.sleep(2);
        boolean esValido = jwtUtil.validateToken(token, username);
        assertFalse(esValido);}

    @Test
    public void validarTokenConNombreDeUsuarioInvalido() {
        String token = jwtUtil.generateToken(username);
        boolean esValido = jwtUtil.validateToken(token, "otrouser");
        assertFalse(esValido);}

}
