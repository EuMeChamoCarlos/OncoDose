package br.edu.ufersa.pw.oncodose.oncoDoseAPI.infrastructure.jwt;

import br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.user.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import java.io.IOException;

@NullMarked
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtValidator jwtValidator;
    private final CustomUserDetailsService userDetailsService;
    static final String PREFIX = "Bearer ";


    public JwtAuthenticationFilter(JwtValidator jwtValidator, CustomUserDetailsService userDetailsService) {
        this.jwtValidator = jwtValidator;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/user/register")
                || path.equals("/api/user/login")
                || path.equals("/error");
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws ServletException, IOException {
    
        final String authorizationHeader = request.getHeader("Authorization");
    
        String username;
        String jwt;
    
        try {
            if (authorizationHeader != null && authorizationHeader.startsWith(PREFIX)) {
                jwt = authorizationHeader.substring(7);
                Claims claims = jwtValidator.validateTokenAndGetClaims(jwt);
                username = claims.getSubject();

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (userDetails.isEnabled()) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        } catch (JwtException e) {
            SecurityContextHolder.clearContext();
        }
    
        chain.doFilter(request, response); // Próximo filtro
    }
    

}
