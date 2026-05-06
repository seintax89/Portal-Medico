package com.eps.portal.controller;

import com.eps.portal.dto.response.MedicoResponse;
import com.eps.portal.security.JwtUtils;
import com.eps.portal.security.UserDetailsServiceImpl;
import com.eps.portal.service.MedicoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * =====================================================================
 * PRUEBAS DE RUTA — MedicoController
 * Tipo: Slice Test con @WebMvcTest
 * Objetivo: Verificar seguridad de la ruta de directorio médico.
 *   - Cualquier usuario autenticado puede ver el directorio.
 *   - Sin autenticación → 403 FORBIDDEN.
 * =====================================================================
 */
@org.springframework.boot.test.context.SpringBootTest
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
@DisplayName("Pruebas de Ruta - MedicoController")
class MedicoControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean MedicoService medicoService;
    @MockBean JwtUtils jwtUtils;
    @MockBean UserDetailsServiceImpl userDetailsService;
    @MockBean com.eps.portal.security.AuthTokenFilter authTokenFilter;

    @Test
    @DisplayName("RUTA-12: GET /api/medicos SIN token → 403 FORBIDDEN")
    void listarMedicos_sinToken_retorna403() throws Exception {
        mockMvc.perform(get("/api/medicos"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("RUTA-13: GET /api/medicos con cualquier usuario autenticado → 200 OK")
    @WithMockUser(username = "cualquiera@test.com", roles = {"PACIENTE"})
    void listarMedicos_autenticado_retorna200() throws Exception {
        // ARRANGE
        List<MedicoResponse> lista = List.of(
                MedicoResponse.builder()
                        .id(1L)
                        .nombreCompleto("Dr. Ana General")
                        .especialidad("MEDICINA_GENERAL")
                        .registroMedico("RM-001")
                        .build()
        );
        when(medicoService.obtenerDirectorio(any())).thenReturn(lista);

        // ACT & ASSERT
        mockMvc.perform(get("/api/medicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].nombreCompleto").value("Dr. Ana General"))
                .andExpect(jsonPath("$[0].especialidad").value("MEDICINA_GENERAL"));
    }

    @Test
    @DisplayName("RUTA-14: GET /api/medicos?especialidadId=2 → 200 OK con filtro")
    @WithMockUser(username = "paciente@test.com", roles = {"PACIENTE"})
    void listarMedicos_conFiltroEspecialidad_retorna200() throws Exception {
        // ARRANGE
        List<MedicoResponse> pediatras = List.of(
                MedicoResponse.builder()
                        .id(3L)
                        .nombreCompleto("Dr. Pedro Pediatra")
                        .especialidad("PEDIATRIA")
                        .registroMedico("RM-003")
                        .build()
        );
        when(medicoService.obtenerDirectorio(2)).thenReturn(pediatras);

        // ACT & ASSERT
        mockMvc.perform(get("/api/medicos?especialidadId=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].especialidad").value("PEDIATRIA"));
    }

    @Test
    @DisplayName("RUTA-15: GET /api/medicos con rol MEDICO → 200 OK (médicos también acceden)")
    @WithMockUser(username = "medico@test.com", roles = {"MEDICO"})
    void listarMedicos_conRolMedico_retorna200() throws Exception {
        when(medicoService.obtenerDirectorio(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/medicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
