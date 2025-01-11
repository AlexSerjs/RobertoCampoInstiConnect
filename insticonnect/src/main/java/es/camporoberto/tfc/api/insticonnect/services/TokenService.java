package es.camporoberto.tfc.api.insticonnect.services;

import es.camporoberto.tfc.api.insticonnect.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenService {

    private final JwtConfig jwtConfig;

    @Autowired
    public TokenService(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    // Generar un token JWT
    public String generarToken(String correo) {
        return Jwts.builder()
                .setSubject(correo)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpirationMs()))  // Tiempo de expiración configurado
                .signWith(SignatureAlgorithm.HS256, jwtConfig.getSecretKey().getBytes()) // Utilizamos la clave secreta desde JwtConfig
                .compact();
    }

    // Validar un token JWT
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtConfig.getSecretKey().getBytes()) // Utilizamos la clave secreta desde JwtConfig
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().after(new Date()); // Verificar que el token no haya expirado
        } catch (Exception e) {
            // Cualquier excepción significa que el token no es válido
            return false;
        }
    }

    // Extraer el correo del token JWT
    public String extraerCorreo(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtConfig.getSecretKey().getBytes()) // Utilizamos la clave secreta desde JwtConfig
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject(); // Extraer el correo que se guardó como subject del token
    }



}
