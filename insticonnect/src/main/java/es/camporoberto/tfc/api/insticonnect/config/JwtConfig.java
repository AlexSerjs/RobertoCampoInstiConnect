package es.camporoberto.tfc.api.insticonnect.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expirationMs}")
    private Long expirationMs;

    public String getSecretKey() {
        return secretKey;
    }

    public Long getExpirationMs() {
        return expirationMs;
    }
}

