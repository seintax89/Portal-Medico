package com.eps.portal.controller;

import com.eps.portal.dto.request.LoginRequest;
import com.eps.portal.dto.request.RegistroMedicoRequest;
import com.eps.portal.dto.request.RegistroPacienteRequest;
import com.eps.portal.dto.response.JwtResponse;
import com.eps.portal.dto.response.MensajeResponse;
import com.eps.portal.entity.Role;
import com.eps.portal.entity.Usuario;
import com.eps.portal.repository.UsuarioRepository;
import com.eps.portal.security.JwtUtils;
import com.eps.portal.security.UserDetailsServiceImpl;
import com.eps.portal.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * =====================================================================
 * PRUEBAS DE RUTA — AuthController
 * Tipo: Slice Test con @WebMvcTest
 * Objetivo: Verificar que las rutas HTTP del controlador de
 *           autenticación responden con los códigos y formatos correctos.
 * =====================================================================
 */
@org.springframework.boot.test.context.SpringBootTest
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
@DisplayName("Pruebas de Ruta - AuthController")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // Mocks de la capa de seguridad (requeridos por @WebMvcTest con SecurityConfig)
    @MockBean AuthenticationManager authenticationManager;
    @MockBean JwtUtils jwtUtils;
    @MockBean UserDetailsServiceImpl userDetailsService;
    @MockBean AuthService authService;
    @MockBean UsuarioRepository usuarioRepository;
    @MockBean com.eps.portal.security.AuthTokenFilter authTokenFilter;

    // ──────────────────────────────────────────────────────
    // RUTA: POST /api/auth/login
    // ──────────────────────────────────────────────────────

    @Test
    @DisplayName("RUTA-01: POST /api/auth/login con credenciales válidas → 200 OK + token JWT")
    void login_credencialesValidas_retorna200ConToken() throws Exception {
        // ARRANGE
        LoginRequest req = new LoginRequest();
        req.setEmail("admin@eps.com");
        req.setPassword("Admin123*");

        UserDetails userDetails = new User(
                "admin@eps.com", "hashed",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        Role rolAdmin = new Role();
        rolAdmin.setId(1);
        rolAdmin.setNombre("ROLE_ADMIN");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("admin@eps.com");
        usuario.setRol(rolAdmin);

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtils.generarJwtToken(any())).thenReturn("mock.jwt.token");
        when(usuarioRepository.findByEmail("admin@eps.com")).thenReturn(Optional.of(usuario));

        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.rol").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.email").value("admin@eps.com"));
    }

    @Test
    @DisplayName("RUTA-02: POST /api/auth/login con credenciales inválidas → 401 UNAUTHORIZED")
    void login_credencialesInvalidas_retorna401() throws Exception {
        // ARRANGE
        LoginRequest req = new LoginRequest();
        req.setEmail("fake@test.com");
        req.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciales incorrectas"));

        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("RUTA-03: POST /api/auth/login sin body → 400 BAD REQUEST")
    void login_sinBody_retorna400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ──────────────────────────────────────────────────────
    // RUTA: POST /api/auth/registro/paciente
    // ──────────────────────────────────────────────────────

    @Test
    @DisplayName("RUTA-04: POST /api/auth/registro/paciente con datos válidos → 201 CREATED")
    void registrarPaciente_datosValidos_retorna201() throws Exception {
        // ARRANGE
        RegistroPacienteRequest req = new RegistroPacienteRequest();
        req.setEmail("nuevo@test.com");
        req.setPassword("Password123*");
        req.setNombres("Ana");
        req.setApellidos("Martínez");
        req.setTipoDocumento("CC");
        req.setNumeroDocumento("987654321");
        req.setFechaNacimiento(java.time.LocalDate.of(1995, 3, 20));

        doNothing().when(authService).registrarPaciente(any());

        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/registro/paciente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Paciente registrado exitosamente"));
    }

    @Test
    @DisplayName("RUTA-05: POST /api/auth/registro/paciente sin email → 400 BAD REQUEST (validación Bean)")
    void registrarPaciente_sinEmail_retorna400() throws Exception {
        // ARRANGE: Falta el campo email
        String bodyInvalido = """
                {
                    "password": "Password123*",
                    "nombres": "Test",
                    "apellidos": "Usuario",
                    "tipoDocumento": "CC",
                    "numeroDocumento": "111222333",
                    "fechaNacimiento": "1990-01-01"
                }
                """;

        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/registro/paciente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyInvalido))
                .andExpect(status().isBadRequest());
    }

    // ──────────────────────────────────────────────────────
    // RUTA: POST /api/auth/registro/medico
    // ──────────────────────────────────────────────────────

    @Test
    @DisplayName("RUTA-06: POST /api/auth/registro/medico con datos válidos → 201 CREATED")
    void registrarMedico_datosValidos_retorna201() throws Exception {
        // ARRANGE
        RegistroMedicoRequest req = new RegistroMedicoRequest();
        req.setEmail("medico@test.com");
        req.setPassword("Password123*");
        req.setNombres("Dr. Juan");
        req.setApellidos("Valdés");
        req.setRegistroMedico("RM-99999");
        req.setEspecialidadId(1);

        doNothing().when(authService).registrarMedico(any());

        // ACT & ASSERT
        mockMvc.perform(post("/api/auth/registro/medico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Médico registrado exitosamente"));
    }
}
