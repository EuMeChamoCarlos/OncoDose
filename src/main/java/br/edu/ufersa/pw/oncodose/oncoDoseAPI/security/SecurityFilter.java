package br.edu.ufersa.pw.oncodose.oncoDoseAPI.security;


import br.edu.ufersa.pw.oncodose.oncoDoseAPI.infrastructure.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;



@Configuration
public class SecurityFilter {
    public static final String BASE_URL = "/api/user";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityFilter(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\":\"Não autenticado\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\":\"Acesso negado\"}");
                        }))
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(CorsConfig.corsConfigurationSource()))
.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/error").anonymous()
                        .requestMatchers(BASE_URL + "/register").permitAll()
                        .requestMatchers(BASE_URL + "/login").permitAll()
                        .requestMatchers(BASE_URL + "/me").authenticated()
                        .anyRequest().authenticated())
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
