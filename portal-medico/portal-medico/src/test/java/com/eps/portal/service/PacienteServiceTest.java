package com.eps.portal.service;

import com.eps.portal.dto.response.PacienteResponse;
import com.eps.portal.entity.Paciente;
import com.eps.portal.entity.Role;
import com.eps.portal.entity.Usuario;
import com.eps.portal.repository.PacienteRepository;
import com.eps.portal.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * =====================================================================
 * PRUEBAS UNITARIAS (FUNCIÓN) — PacienteService
 * Tipo: Unit Test con Mockito
 * Objetivo: Verificar la obtención del perfil del paciente.
 * =====================================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - PacienteService")
class PacienteServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PacienteRepository pacienteRepository;

    @InjectMocks
    private PacienteService pacienteService;

    private Usuario usuario;
    private Paciente paciente;

    @BeforeEach
    void setUp() {
        Role rol = new Role();
        rol.setId(2);
        rol.setNombre("ROLE_PACIENTE");

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("paciente@test.com");
        usuario.setPassword("hashed");
        usuario.setRol(rol);

        paciente = new Paciente();
        paciente.setUsuarioId(1L);
        paciente.setUsuario(usuario);
        paciente.setNombres("Carlos");
        paciente.setApellidos("García");
        paciente.setNumeroDocumento("12345678");
        paciente.setFechaNacimiento(LocalDate.of(1990, 5, 15));
        paciente.setTipoPaciente("ADULTO");
    }

    @Test
    @DisplayName("FN-18: Obtener perfil de paciente por email → retorna DTO sin password")
    void obtenerPerfilPorEmail_emailValido_retornaPerfil() {
        // ARRANGE
        when(usuarioRepository.findByEmail("paciente@test.com")).thenReturn(Optional.of(usuario));
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        // ACT
        PacienteResponse resp = pacienteService.obtenerPerfilPorEmail("paciente@test.com");

        // ASSERT
        assertThat(resp).isNotNull();
        assertThat(resp.getEmail()).isEqualTo("paciente@test.com");
        assertThat(resp.getNombres()).isEqualTo("Carlos");
        assertThat(resp.getApellidos()).isEqualTo("García");
        assertThat(resp.getTipoPaciente()).isEqualTo("ADULTO");
        assertThat(resp.getFechaNacimiento()).isEqualTo(LocalDate.of(1990, 5, 15));
        // CRÍTICO: el DTO no debe exponer el password
        // (PacienteResponse no tiene campo 'password' por diseño)
    }

    @Test
    @DisplayName("FN-19: Email inexistente → lanza RuntimeException")
    void obtenerPerfilPorEmail_emailInexistente_lanzaExcepcion() {
        // ARRANGE
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> pacienteService.obtenerPerfilPorEmail("noexiste@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(pacienteRepository, never()).findById(any());
    }

    @Test
    @DisplayName("FN-20: Usuario existe pero sin perfil de paciente → lanza RuntimeException")
    void obtenerPerfilPorEmail_sinPerfilPaciente_lanzaExcepcion() {
        // ARRANGE: El usuario existe pero su perfil de paciente no fue creado
        when(usuarioRepository.findByEmail("paciente@test.com")).thenReturn(Optional.of(usuario));
        when(pacienteRepository.findById(1L)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> pacienteService.obtenerPerfilPorEmail("paciente@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Perfil de paciente no encontrado");
    }
}
