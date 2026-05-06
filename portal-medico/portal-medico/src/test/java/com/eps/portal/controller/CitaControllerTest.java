package com.eps.portal.controller;

import com.eps.portal.dto.request.CitaRequest;
import com.eps.portal.dto.response.CitaResponse;
import com.eps.portal.security.JwtUtils;
import com.eps.portal.security.UserDetailsServiceImpl;
import com.eps.portal.service.CitaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * =====================================================================
 * PRUEBAS DE RUTA — CitaController
 * Tipo: Slice Test con @WebMvcTest
 * Objetivo: Verificar la seguridad de las rutas de citas:
 *   - Sin token → acceso denegado (403)
 *   - Con rol incorrecto → acceso denegado (403)
 *   - Con rol correcto → acceso permitido (201)
 * =====================================================================
 */
@org.springframework.boot.test.context.SpringBootTest
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
@DisplayName("Pruebas de Ruta - CitaController")
class CitaControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean CitaService citaService;
    @MockBean JwtUtils jwtUtils;
    @MockBean UserDetailsServiceImpl userDetailsService;
    @MockBean com.eps.portal.security.AuthTokenFilter authTokenFilter;

    private CitaRequest buildCitaRequest() {
        CitaRequest req = new CitaRequest();
        req.setPacienteId(1L);
        req.setMedicoId(2L);
        // Fecha futura válida: próximo lunes a las 10 AM
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(8)
                .withHour(10).withMinute(0).withSecond(0).withNano(0);
        // Ajustar a lunes si es necesario
        while (fechaFutura.getDayOfWeek().getValue() > 5) {
            fechaFutura = fechaFutura.plusDays(1);
        }
        req.setFechaHora(fechaFutura);
        return req;
    }

    @Test
    @DisplayName("RUTA-07: POST /api/citas SIN token → 403 FORBIDDEN")
    void agendarCita_sinToken_retorna403() throws Exception {
        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCitaRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RUTA-08: POST /api/citas con rol PACIENTE → 201 CREATED")
    @WithMockUser(username = "paciente@test.com", roles = {"PACIENTE"})
    void agendarCita_conRolPaciente_retorna201() throws Exception {
        // ARRANGE
        CitaResponse mockResp = CitaResponse.builder()
                .idCita(1L)
                .nombrePaciente("Carlos García")
                .nombreMedico("Dr. Ana General")
                .especialidad("MEDICINA_GENERAL")
                .fechaHora(LocalDateTime.now().plusDays(8))
                .estado("PROGRAMADA")
                .build();
        when(citaService.agendarCita(any(), anyString())).thenReturn(mockResp);

        // ACT & ASSERT
        mockMvc.perform(post("/api/citas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCitaRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"))
                .andExpect(jsonPath("$.nombrePaciente").exists());
    }

    @Test
    @DisplayName("RUTA-09: POST /api/citas con rol MEDICO → 201 CREATED")
    @WithMockUser(username = "medico@test.com", roles = {"MEDICO"})
    void agendarCita_conRolMedico_retorna201() throws Exception {
        // ARRANGE
        CitaResponse mockResp = CitaResponse.builder()
                .idCita(2L)
                .nombrePaciente("Sofia López")
                .nombreMedico("Dr. Pedro Pediatra")
                .especialidad("PEDIATRIA")
                .fechaHora(LocalDateTime.now().plusDays(8))
                .estado("PROGRAMADA")
                .build();
        when(citaService.agendarCita(any(), anyString())).thenReturn(mockResp);

        // ACT & ASSERT
        mockMvc.perform(post("/api/citas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCitaRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCita").value(2));
    }

    @Test
    @DisplayName("RUTA-10: POST /api/citas con regla de negocio violada → 400 BAD REQUEST")
    @WithMockUser(username = "paciente@test.com", roles = {"PACIENTE"})
    void agendarCita_reglaViolada_retorna400() throws Exception {
        // ARRANGE: El servicio lanza excepción por una regla violada
        when(citaService.agendarCita(any(), anyString()))
                .thenThrow(new RuntimeException("La EPS no atiende fines de semana."));

        // ACT & ASSERT
        mockMvc.perform(post("/api/citas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCitaRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("fines de semana")));
    }

    @Test
    @DisplayName("RUTA-11: POST /api/citas con rol ADMIN → 403 FORBIDDEN (no tiene acceso)")
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void agendarCita_conRolAdmin_retorna403() throws Exception {
        mockMvc.perform(post("/api/citas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildCitaRequest())))
                .andExpect(status().isForbidden());
    }
}
