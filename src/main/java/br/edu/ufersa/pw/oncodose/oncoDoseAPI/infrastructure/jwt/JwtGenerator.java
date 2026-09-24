package br.edu.ufersa.pw.oncodose.oncoDoseAPI.infrastructure.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.user.CustomUserDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtGenerator {

    @Value("${api.security.token.secret}")
    private String SECRET_KEY;

    static final long EXPIRATION_TIME = (1000 * 60 * 60 * 10); // 10 horas

    public String generateToken(Authentication authentication) {
        var userDetails = (CustomUserDetails) authentication.getPrincipal();
        assert userDetails != null;
        var user = userDetails.getUser();

        return Jwts.builder()
                .subject(user.getAuth().getUsername())
                .claims(
                    Map.of(
                        "UUID", user.getId(),
                        "ROLE", user.getRole().getName(),
                        "name", user.getProfile() == null
                                ? "Usuário Indefinido"
                                : user.getProfile().getName()
                    )
                )
                .issuedAt(new Date(System.currentTimeMillis())) // Data de emissão
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Expira em 10 horas
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}

