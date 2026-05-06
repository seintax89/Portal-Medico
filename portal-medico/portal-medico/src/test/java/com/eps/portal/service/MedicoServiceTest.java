package com.eps.portal.service;

import com.eps.portal.dto.response.MedicoResponse;
import com.eps.portal.entity.Especialidad;
import com.eps.portal.entity.Medico;
import com.eps.portal.repository.MedicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * =====================================================================
 * PRUEBAS UNITARIAS (FUNCIÓN) — MedicoService
 * Tipo: Unit Test con Mockito
 * Objetivo: Verificar el filtrado del directorio de médicos.
 * =====================================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - MedicoService")
class MedicoServiceTest {

    @Mock private MedicoRepository medicoRepository;

    @InjectMocks
    private MedicoService medicoService;

    private List<Medico> todosMedicos;
    private List<Medico> medicosPediatria;

    @BeforeEach
    void setUp() {
        Especialidad espGeneral = crearEspecialidad(1, "MEDICINA_GENERAL");
        Especialidad espPediatria = crearEspecialidad(2, "PEDIATRIA");

        Medico m1 = crearMedico(1L, "Ana", "Gómez", "RM-001", espGeneral);
        Medico m2 = crearMedico(2L, "Luisa", "Torres", "RM-002", espGeneral);
        Medico m3 = crearMedico(3L, "Pedro", "Ruiz", "RM-003", espPediatria);

        todosMedicos = List.of(m1, m2, m3);
        medicosPediatria = List.of(m3);
    }

    @Test
    @DisplayName("FN-15: Sin filtro de especialidad → retorna todos los médicos")
    void obtenerDirectorio_sinFiltro_retornaTodos() {
        // ARRANGE
        when(medicoRepository.findAll()).thenReturn(todosMedicos);

        // ACT
        List<MedicoResponse> resultado = medicoService.obtenerDirectorio(null);

        // ASSERT
        assertThat(resultado).hasSize(3);
        assertThat(resultado).extracting(MedicoResponse::getNombreCompleto)
                .allMatch(nombre -> nombre.startsWith("Dr."));
        verify(medicoRepository).findAll();
        verify(medicoRepository, never()).findByEspecialidadId(any());
    }

    @Test
    @DisplayName("FN-16: Con filtro de especialidad (ID=2 Pediatría) → retorna solo pediatras")
    void obtenerDirectorio_conFiltroEspecialidad_retornaFiltrados() {
        // ARRANGE
        when(medicoRepository.findByEspecialidadId(2)).thenReturn(medicosPediatria);

        // ACT
        List<MedicoResponse> resultado = medicoService.obtenerDirectorio(2);

        // ASSERT
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getEspecialidad()).isEqualTo("PEDIATRIA");
        assertThat(resultado.get(0).getNombreCompleto()).contains("Pedro");
        verify(medicoRepository).findByEspecialidadId(2);
        verify(medicoRepository, never()).findAll();
    }

    @Test
    @DisplayName("FN-17: El DTO de respuesta no expone la entidad (password safe)")
    void obtenerDirectorio_respuesta_formatoCorrectoDTO() {
        // ARRANGE
        when(medicoRepository.findAll()).thenReturn(todosMedicos);

        // ACT
        List<MedicoResponse> resultado = medicoService.obtenerDirectorio(null);

        // ASSERT: verificamos que el DTO tiene los campos correctos
        MedicoResponse primero = resultado.get(0);
        assertThat(primero.getId()).isNotNull();
        assertThat(primero.getNombreCompleto()).isNotBlank();
        assertThat(primero.getEspecialidad()).isNotBlank();
        assertThat(primero.getRegistroMedico()).isNotBlank();
    }

    // ─── Helpers ────────────────────────────────────────────

    private Especialidad crearEspecialidad(Integer id, String nombre) {
        Especialidad e = new Especialidad();
        e.setId(id);
        e.setNombre(nombre);
        return e;
    }

    private Medico crearMedico(Long id, String nombres, String apellidos, String rm, Especialidad esp) {
        Medico m = new Medico();
        m.setUsuarioId(id);
        m.setNombres(nombres);
        m.setApellidos(apellidos);
        m.setRegistroMedico(rm);
        m.setEspecialidad(esp);
        return m;
    }
}
