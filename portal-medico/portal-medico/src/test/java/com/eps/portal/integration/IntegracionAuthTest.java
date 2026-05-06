package com.eps.portal.integration;

import com.eps.portal.dto.request.LoginRequest;
import com.eps.portal.dto.request.RegistroPacienteRequest;
import com.eps.portal.entity.Role;
import com.eps.portal.repository.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * =====================================================================
 * PRUEBAS DE INTEGRACIÓN — Flujo de Autenticación
 * Tipo: @SpringBootTest con H2 en memoria + @AutoConfigureMockMvc
 * Perfil: "test" → usa application-test.yml (H2, no Supabase)
 *
 * Flujo de prueba (simula el ciclo de vida de autenticación):
 *   1. Registro de nuevo paciente
 *   2. Login con sus credenciales → obtiene token JWT
 *   3. Uso del token JWT para acceder a un recurso protegido
 *   4. Rechazo con credenciales incorrectas o sin token
 * =====================================================================
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("Pruebas de Integración - Flujo de Autenticación")
class IntegracionAuthTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired RoleRepository roleRepository;

    // Token compartido entre los tests
    private static String tokenJwt;
    private static final String EMAIL_PACIENTE = "integracion.auth@test.com";
    private static final String PASSWORD_PACIENTE = "Password123*";

    @BeforeEach
    void asegurarRolesExisten() {
        // El DataInitializer solo crea ROLE_ADMIN. Para los tests necesitamos ROLE_PACIENTE.
        roleRepository.findByNombre("ROLE_PACIENTE").orElseGet(() -> {
            Role r = new Role();
            r.setNombre("ROLE_PACIENTE");
            return roleRepository.save(r);
        });
        roleRepository.findByNombre("ROLE_MEDICO").orElseGet(() -> {
            Role r = new Role();
            r.setNombre("ROLE_MEDICO");
            return roleRepository.save(r);
        });
    }

    @Test
    @Order(1)
    @DisplayName("INT-01: Registro de nuevo paciente → 201 CREATED")
    void paso1_registroPaciente_exitoso() throws Exception {
        RegistroPacienteRequest req = new RegistroPacienteRequest();
        req.setEmail(EMAIL_PACIENTE);
        req.setPassword(PASSWORD_PACIENTE);
        req.setNombres("Integración");
        req.setApellidos("Auth Test");
        req.setTipoDocumento("CC");
        req.setNumeroDocumento("INT-AUTH-001");
        req.setFechaNacimiento(LocalDate.of(1990, 6, 15));

        mockMvc.perform(post("/api/auth/registro/paciente")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Paciente registrado exitosamente"));
    }

    @Test
    @Order(2)
    @DisplayName("INT-02: Login del paciente registrado → 200 OK con token JWT válido")
    void paso2_loginPaciente_retornaTokenJwt() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(EMAIL_PACIENTE);
        req.setPassword(PASSWORD_PACIENTE);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.rol").value("ROLE_PACIENTE"))
                .andExpect(jsonPath("$.email").value(EMAIL_PACIENTE))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        tokenJwt = objectMapper.readTree(responseBody).get("token").asText();

        assertThat(tokenJwt).isNotBlank();
        assertThat(tokenJwt.split("\\.")).hasSize(3);
    }

    @Test
    @Order(3)
    @DisplayName("INT-03: Acceso a /api/medicos con token válido → 200 OK")
    void paso3_accesoConToken_recursoProtegido() throws Exception {
        Assumptions.assumeTrue(tokenJwt != null, "Saltando: token no disponible");

        mockMvc.perform(get("/api/medicos")
                        .header("Authorization", "Bearer " + tokenJwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(4)
    @DisplayName("INT-04: Acceso a /api/medicos SIN token → 403 FORBIDDEN")
    void paso4_accesoSinToken_retorna403() throws Exception {
        mockMvc.perform(get("/api/medicos"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(5)
    @DisplayName("INT-05: Login con contraseña incorrecta → 401 UNAUTHORIZED")
    void paso5_loginContrasenaIncorrecta_retorna401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(EMAIL_PACIENTE);
        req.setPassword("ContraseñaIncorrecta");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @Order(6)
    @DisplayName("INT-06: Login con email que no existe → 401 UNAUTHORIZED")
    void paso6_loginEmailInexistente_retorna401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("noexiste@test.com");
        req.setPassword(PASSWORD_PACIENTE);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(7)
    @DisplayName("INT-07: Admin (DataInitializer) puede hacer login → 200 OK con ROLE_ADMIN")
    void paso7_adminPreCargado_puedeLogin() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("admin@eps.com");
        req.setPassword("Admin123*");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ROLE_ADMIN"));
    }
}
