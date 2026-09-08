package br.edu.ufersa.pw.oncodose.oncoDoseAPI.infrastructure.jwt;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtValidator {

    private final String secretKey;

    public JwtValidator(@Value("${api.security.token.secret}") String secretKey) {
        if (!StringUtils.hasText(secretKey)) {
            throw new IllegalStateException("Missing required configuration: api.security.token.secret");
        }
        this.secretKey = secretKey;
    }

    public Claims validateTokenAndGetClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(e.getHeader(), e.getClaims(), "Token expirado");
        } catch (JwtException e) {
            throw new JwtException("Token inválido");
        }
    }

}
