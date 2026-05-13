package com.eps.portal.config;

import com.eps.portal.security.AuthTokenFilter;
import com.eps.portal.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthTokenFilter authTokenFilter;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configuración de CORS: Permite que tu Frontend (React) hable con el Backend.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Se agregan múltiples dominios de frontend permitidos (agregar más si es
        // necesario)
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:5173", // Localhost Vite
                "http://localhost:3000", // Localhost React tradicional
                "https://portal-medico.vercel.app", // Vercel
                "https://portal-medico-seintax89s-projects.vercel.app", // Nuevo dominio Vercel
                "https://*.vercel.app", // Vercel deployments and previews
                "https://portal-medico-uicj.onrender.com" // Render backend
        ));

        // Métodos HTTP permitidos para CORS
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Cabeceras permitidas por CORS
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "x-auth-token"));

        // Cabeceras expuestas (esto es necesario si quieres enviar tokens como
        // 'x-auth-token')
        configuration.setExposedHeaders(List.of("x-auth-token"));

        // Permitir el envío de cookies con las peticiones
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Cadena de Filtros: Define quién puede entrar y a dónde.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Aplicamos CORS y desactivamos CSRF (usamos JWT)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF (aplicable solo si usas JWT)

                // 2. Definimos que no guardaremos sesiones (Stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3. Reglas de acceso a las rutas
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Registro y Login son públicos
                        .requestMatchers("/api/especialidades").permitAll() // Catálogos son públicos
                        .requestMatchers("/error").permitAll() // Vital para ver errores reales en consola
                        .anyRequest().authenticated() // Todo lo demás requiere Token JWT
                )

                // 4. Conectamos nuestro proveedor de autenticación
                .authenticationProvider(authenticationProvider())

                // 5. Inyectamos nuestro filtro de Token ANTES del filtro de usuario/contraseña
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}