package com.eps.portal.service;

import com.eps.portal.dto.request.RegistroMedicoRequest;
import com.eps.portal.dto.request.RegistroPacienteRequest;
import com.eps.portal.entity.*;
import com.eps.portal.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * =====================================================================
 * PRUEBAS UNITARIAS (FUNCIÓN) — AuthService
 * Tipo: Unit Test con Mockito
 * Objetivo: Verificar la lógica de negocio del registro de usuarios
 *           sin necesidad de base de datos (mock de repositorios).
 * =====================================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias - AuthService")
class AuthServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private MedicoRepository medicoRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private EspecialidadRepository especialidadRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private Role rolPaciente;
    private Role rolMedico;
    private Usuario usuarioGuardado;

    @BeforeEach
    void setUp() {
        rolPaciente = new Role();
        rolPaciente.setId(2);
        rolPaciente.setNombre("ROLE_PACIENTE");

        rolMedico = new Role();
        rolMedico.setId(3);
        rolMedico.setNombre("ROLE_MEDICO");

        usuarioGuardado = new Usuario();
        usuarioGuardado.setId(1L);
        usuarioGuardado.setEmail("test@test.com");
    }

    // ──────────────────────────────────────────────────────
    // PRUEBAS: registrarPaciente
    // ──────────────────────────────────────────────────────

    @Test
    @DisplayName("FN-01: Registro de paciente adulto (18 años) → tipo ADULTO")
    void registrarPaciente_adulto_tipoPacienteAdulto() {
        // ARRANGE
        RegistroPacienteRequest req = buildPacienteRequest(
                "Carlos", "García", LocalDate.now().minusYears(18)
        );

        when(usuarioRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(roleRepository.findByNombre("ROLE_PACIENTE")).thenReturn(Optional.of(rolPaciente));
        when(passwordEncoder.encode(anyString())).thenReturn("hash_password");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(inv -> {
            Paciente p = inv.getArgument(0);
            // VERIFICAMOS la lógica de clasificación
            assertThat(p.getTipoPaciente()).isEqualTo("ADULTO");
            return p;
        });

        // ACT & ASSERT — No debe lanzar excepción
        assertThatCode(() -> authService.registrarPaciente(req)).doesNotThrowAnyException();
        verify(pacienteRepository).save(any(Paciente.class));
    }

    @Test
    @DisplayName("FN-02: Registro de paciente menor de 14 años → tipo NINO")
    void registrarPaciente_menorEdad_tipoPacienteNino() {
        // ARRANGE
        RegistroPacienteRequest req = buildPacienteRequest(
                "Sofia", "López", LocalDate.now().minusYears(8)
        );

        when(usuarioRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(roleRepository.findByNombre("ROLE_PACIENTE")).thenReturn(Optional.of(rolPaciente));
        when(passwordEncoder.encode(anyString())).thenReturn("hash_password");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(inv -> {
            Paciente p = inv.getArgument(0);
            assertThat(p.getTipoPaciente()).isEqualTo("NINO");
            return p;
        });

        // ACT & ASSERT
        assertThatCode(() -> authService.registrarPaciente(req)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("FN-03: Registro de paciente mayor de 60 años → tipo ADULTO_MAYOR")
    void registrarPaciente_adultoMayor_tipoAdultoMayor() {
        // ARRANGE
        RegistroPacienteRequest req = buildPacienteRequest(
                "Marta", "Rodríguez", LocalDate.now().minusYears(65)
        );

        when(usuarioRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(roleRepository.findByNombre("ROLE_PACIENTE")).thenReturn(Optional.of(rolPaciente));
        when(passwordEncoder.encode(anyString())).thenReturn("hash_password");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(inv -> {
            Paciente p = inv.getArgument(0);
            assertThat(p.getTipoPaciente()).isEqualTo("ADULTO_MAYOR");
            return p;
        });

        // ACT & ASSERT
        assertThatCode(() -> authService.registrarPaciente(req)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("FN-04: Registro con email duplicado → lanza RuntimeException")
    void registrarPaciente_emailDuplicado_lanzaExcepcion() {
        // ARRANGE
        RegistroPacienteRequest req = buildPacienteRequest(
                "Juan", "Pérez", LocalDate.now().minusYears(30)
        );
        when(usuarioRepository.existsByEmail(req.getEmail())).thenReturn(true);

        // ACT & ASSERT
        assertThatThrownBy(() -> authService.registrarPaciente(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ya está registrado");

        // VERIFICAMOS que nunca se guardó nada en BD
        verify(usuarioRepository, never()).save(any());
        verify(pacienteRepository, never()).save(any());
    }

    // ──────────────────────────────────────────────────────
    // PRUEBAS: registrarMedico
    // ──────────────────────────────────────────────────────

    @Test
    @DisplayName("FN-05: Registro de médico exitoso")
    void registrarMedico_exitoso() {
        // ARRANGE
        RegistroMedicoRequest req = buildMedicoRequest(1);
        Especialidad esp = new Especialidad();
        esp.setId(1);
        esp.setNombre("MEDICINA_GENERAL");

        when(usuarioRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(roleRepository.findByNombre("ROLE_MEDICO")).thenReturn(Optional.of(rolMedico));
        when(passwordEncoder.encode(anyString())).thenReturn("hash_password");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);
        when(especialidadRepository.findById(1)).thenReturn(Optional.of(esp));
        when(medicoRepository.save(any(Medico.class))).thenAnswer(inv -> inv.getArgument(0));

        // ACT & ASSERT
        assertThatCode(() -> authService.registrarMedico(req)).doesNotThrowAnyException();
        verify(medicoRepository).save(any(Medico.class));
    }

    @Test
    @DisplayName("FN-06: Registro de médico con especialidad inexistente → lanza RuntimeException")
    void registrarMedico_especialidadInexistente_lanzaExcepcion() {
        // ARRANGE
        RegistroMedicoRequest req = buildMedicoRequest(999);

        when(usuarioRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(roleRepository.findByNombre("ROLE_MEDICO")).thenReturn(Optional.of(rolMedico));
        when(passwordEncoder.encode(anyString())).thenReturn("hash_password");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioGuardado);
        when(especialidadRepository.findById(999)).thenReturn(Optional.empty());

        // ACT & ASSERT
        assertThatThrownBy(() -> authService.registrarMedico(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("especialidad");

        verify(medicoRepository, never()).save(any());
    }

    // ─── Helpers para construir requests de prueba ────────

    private RegistroPacienteRequest buildPacienteRequest(String nombres, String apellidos, LocalDate fechaNac) {
        RegistroPacienteRequest req = new RegistroPacienteRequest();
        req.setNombres(nombres);
        req.setApellidos(apellidos);
        req.setEmail(nombres.toLowerCase() + "@test.com");
        req.setPassword("Password123*");
        req.setTipoDocumento("CC");
        req.setNumeroDocumento("123456789");
        req.setFechaNacimiento(fechaNac);
        return req;
    }

    private RegistroMedicoRequest buildMedicoRequest(Integer especialidadId) {
        RegistroMedicoRequest req = new RegistroMedicoRequest();
        req.setNombres("Doctor");
        req.setApellidos("Prueba");
        req.setEmail("medico@test.com");
        req.setPassword("Password123*");
        req.setRegistroMedico("RM-00001");
        req.setEspecialidadId(especialidadId);
        return req;
    }
}
