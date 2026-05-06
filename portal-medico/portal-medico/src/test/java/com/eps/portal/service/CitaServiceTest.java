package com.eps.portal.service;

import com.eps.portal.dto.request.CitaRequest;
import com.eps.portal.dto.response.CitaResponse;
import com.eps.portal.entity.*;
import com.eps.portal.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * =====================================================================
 * PRUEBAS UNITARIAS (FUNCIÓN) + PRUEBAS DE ESTADO — CitaService
 *
 * Tipo: Unit Test con Mockito
 * Cubre:
 *   - Reglas de negocio de agendamiento (FN = Función)
 *   - Validación de estado de la cita al crearla (EST = Estado)
 *   - Reglas de control de acceso por rol (RN-04 Remisión)
 * =====================================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias + Estado - CitaService")
class CitaServiceTest {

    @Mock private CitaRepository citaRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private MedicoRepository medicoRepository;

    @InjectMocks
    private CitaService citaService;

    private Paciente pacienteAdulto;
    private Paciente pacienteNino;
    private Medico medicoGeneral;
    private Medico medicoPediatra;
    private Medico medicoCardiologo;

    @BeforeEach
    void setUp() {
        // Especialidades de prueba
        Especialidad espGeneral = new Especialidad();
        espGeneral.setId(1);
        espGeneral.setNombre("MEDICINA_GENERAL");

        Especialidad espPediatria = new Especialidad();
        espPediatria.setId(2);
        espPediatria.setNombre("PEDIATRIA");

        Especialidad espCardiologia = new Especialidad();
        espCardiologia.setId(3);
        espCardiologia.setNombre("CARDIOLOGIA");

        // Pacientes de prueba
        pacienteAdulto = new Paciente();
        pacienteAdulto.setUsuarioId(10L);
        pacienteAdulto.setNombres("Carlos");
        pacienteAdulto.setApellidos("Adulto");
        pacienteAdulto.setTipoPaciente("ADULTO");

        pacienteNino = new Paciente();
        pacienteNino.setUsuarioId(11L);
        pacienteNino.setNombres("Sofia");
        pacienteNino.setApellidos("Niña");
        pacienteNino.setTipoPaciente("NINO");

        // Médicos de prueba
        medicoGeneral = new Medico();
        medicoGeneral.setUsuarioId(20L);
        medicoGeneral.setNombres("Ana");
        medicoGeneral.setApellidos("General");
        medicoGeneral.setEspecialidad(espGeneral);

        medicoPediatra = new Medico();
        medicoPediatra.setUsuarioId(21L);
        medicoPediatra.setNombres("Pedro");
        medicoPediatra.setApellidos("Pediatra");
        medicoPediatra.setEspecialidad(espPediatria);

        medicoCardiologo = new Medico();
        medicoCardiologo.setUsuarioId(22L);
        medicoCardiologo.setNombres("Luis");
        medicoCardiologo.setApellidos("Cardiologo");
        medicoCardiologo.setEspecialidad(espCardiologia);
    }

    // ═══════════════════════════════════════════════════════
    // SECCIÓN 1: PRUEBAS DE FUNCIÓN — Lógica de negocio
    // ═══════════════════════════════════════════════════════

    @Test
    @DisplayName("FN-07: Agendar cita exitosa — paciente adulto con médico general")
    void agendarCita_pacienteAdulto_medicoGeneral_exitoso() {
        // ARRANGE: Lunes a las 10 AM (dentro del horario)
        LocalDateTime fechaValida = proximoLunesA(10);
        CitaRequest req = buildCitaRequest(10L, 20L, fechaValida);

        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(pacienteAdulto));
        when(medicoRepository.findById(20L)).thenReturn(Optional.of(medicoGeneral));
        when(citaRepository.existsByMedicoUsuarioIdAndFechaHora(anyLong(), any())).thenReturn(false);
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> {
            Cita c = inv.getArgument(0);
            c = copiarCitaConId(c, 1L);
            return c;
        });

        // ACT
        CitaResponse resp = citaService.agendarCita(req, "ROLE_PACIENTE");

        // ASSERT
        assertThat(resp).isNotNull();
        assertThat(resp.getEstado()).isEqualTo("PROGRAMADA");
        assertThat(resp.getNombrePaciente()).contains("Carlos");
        assertThat(resp.getNombreMedico()).contains("Dr.");
    }

    @Test
    @DisplayName("FN-08: Agendar cita en fin de semana (Sábado) → RuntimeException")
    void agendarCita_finDeSemana_lanzaExcepcion() {
        // ARRANGE: próximo Sábado a las 10 AM
        LocalDateTime sabado = proximoSabadoA(10);
        CitaRequest req = buildCitaRequest(10L, 20L, sabado);

        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(pacienteAdulto));
        when(medicoRepository.findById(20L)).thenReturn(Optional.of(medicoGeneral));

        // ACT & ASSERT
        assertThatThrownBy(() -> citaService.agendarCita(req, "ROLE_PACIENTE"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("fines de semana");
    }

    @Test
    @DisplayName("FN-09: Agendar cita fuera de horario (7:00 AM) → RuntimeException")
    void agendarCita_fueraDeHorario_lanzaExcepcion() {
        // ARRANGE: Lunes a las 7 AM (antes de apertura)
        LocalDateTime fueraHorario = proximoLunesA(7);
        CitaRequest req = buildCitaRequest(10L, 20L, fueraHorario);

        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(pacienteAdulto));
        when(medicoRepository.findById(20L)).thenReturn(Optional.of(medicoGeneral));

        // ACT & ASSERT
        assertThatThrownBy(() -> citaService.agendarCita(req, "ROLE_PACIENTE"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("horario de atención");
    }

    @Test
    @DisplayName("FN-10: Solapamiento de cita → RuntimeException")
    void agendarCita_solapamiento_lanzaExcepcion() {
        // ARRANGE: El médico ya tiene una cita a esa hora
        LocalDateTime fechaValida = proximoLunesA(10);
        CitaRequest req = buildCitaRequest(10L, 20L, fechaValida);

        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(pacienteAdulto));
        when(medicoRepository.findById(20L)).thenReturn(Optional.of(medicoGeneral));
        when(citaRepository.existsByMedicoUsuarioIdAndFechaHora(anyLong(), any())).thenReturn(true); // ← solapamiento

        // ACT & ASSERT
        assertThatThrownBy(() -> citaService.agendarCita(req, "ROLE_PACIENTE"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ya tiene una cita asignada");
    }

    @Test
    @DisplayName("FN-11: RN-05 Paciente NINO agendado en especialidad NO PEDIATRÍA → RuntimeException")
    void agendarCita_ninoEnEspecialidadNoPediatria_lanzaExcepcion() {
        // ARRANGE: Niño intentando cita con médico general
        LocalDateTime fechaValida = proximoLunesA(10);
        CitaRequest req = buildCitaRequest(11L, 20L, fechaValida);

        when(pacienteRepository.findById(11L)).thenReturn(Optional.of(pacienteNino));
        when(medicoRepository.findById(20L)).thenReturn(Optional.of(medicoGeneral));
        when(citaRepository.existsByMedicoUsuarioIdAndFechaHora(anyLong(), any())).thenReturn(false);

        // ACT & ASSERT
        assertThatThrownBy(() -> citaService.agendarCita(req, "ROLE_MEDICO"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("PEDIATRÍA");
    }

    @Test
    @DisplayName("FN-12: Niño en PEDIATRÍA → cita exitosa")
    void agendarCita_ninoEnPediatria_exitoso() {
        // ARRANGE
        LocalDateTime fechaValida = proximoLunesA(10);
        CitaRequest req = buildCitaRequest(11L, 21L, fechaValida);

        when(pacienteRepository.findById(11L)).thenReturn(Optional.of(pacienteNino));
        when(medicoRepository.findById(21L)).thenReturn(Optional.of(medicoPediatra));
        when(citaRepository.existsByMedicoUsuarioIdAndFechaHora(anyLong(), any())).thenReturn(false);
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> copiarCitaConId(inv.getArgument(0), 2L));

        // ACT
        CitaResponse resp = citaService.agendarCita(req, "ROLE_MEDICO");

        // ASSERT
        assertThat(resp.getEstado()).isEqualTo("PROGRAMADA");
        assertThat(resp.getEspecialidad()).isEqualTo("PEDIATRIA");
    }

    @Test
    @DisplayName("FN-13: RN-04 Paciente intenta agendar cita especializada (Cardiología) → RuntimeException")
    void agendarCita_pacienteAgendaEspecializada_lanzaExcepcion() {
        // ARRANGE: Paciente intenta agendar directo con cardiólogo (sin remisión)
        LocalDateTime fechaValida = proximoLunesA(10);
        CitaRequest req = buildCitaRequest(10L, 22L, fechaValida);

        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(pacienteAdulto));
        when(medicoRepository.findById(22L)).thenReturn(Optional.of(medicoCardiologo));
        when(citaRepository.existsByMedicoUsuarioIdAndFechaHora(anyLong(), any())).thenReturn(false);

        // ACT & ASSERT
        assertThatThrownBy(() -> citaService.agendarCita(req, "ROLE_PACIENTE"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("remisión");
    }

    @Test
    @DisplayName("FN-14: RN-04 Médico agenda cita especializada en nombre del paciente → exitoso")
    void agendarCita_medicoAgendaEspecializada_exitoso() {
        // ARRANGE: Un MÉDICO sí puede agendar citas especializadas
        LocalDateTime fechaValida = proximoLunesA(10);
        CitaRequest req = buildCitaRequest(10L, 22L, fechaValida);

        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(pacienteAdulto));
        when(medicoRepository.findById(22L)).thenReturn(Optional.of(medicoCardiologo));
        when(citaRepository.existsByMedicoUsuarioIdAndFechaHora(anyLong(), any())).thenReturn(false);
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> copiarCitaConId(inv.getArgument(0), 3L));

        // ACT
        CitaResponse resp = citaService.agendarCita(req, "ROLE_MEDICO");

        // ASSERT
        assertThat(resp.getEstado()).isEqualTo("PROGRAMADA");
        assertThat(resp.getEspecialidad()).isEqualTo("CARDIOLOGIA");
    }

    // ═══════════════════════════════════════════════════════
    // SECCIÓN 2: PRUEBAS DE ESTADO — Estado inicial de Cita
    // ═══════════════════════════════════════════════════════

    @Test
    @DisplayName("EST-01: Estado inicial de una cita recién creada → PROGRAMADA")
    void estadoCita_alCrearse_debeSerProgramada() {
        // ARRANGE
        LocalDateTime fechaValida = proximoLunesA(9);
        CitaRequest req = buildCitaRequest(10L, 20L, fechaValida);

        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(pacienteAdulto));
        when(medicoRepository.findById(20L)).thenReturn(Optional.of(medicoGeneral));
        when(citaRepository.existsByMedicoUsuarioIdAndFechaHora(anyLong(), any())).thenReturn(false);
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> {
            Cita cita = inv.getArgument(0);
            // VERIFICAMOS que el estado es PROGRAMADA antes de guardar
            assertThat(cita.getEstado()).isEqualTo("PROGRAMADA");
            return copiarCitaConId(cita, 99L);
        });

        // ACT
        CitaResponse resp = citaService.agendarCita(req, "ROLE_PACIENTE");

        // ASSERT FINAL: el response también refleja el estado correcto
        assertThat(resp.getEstado()).isEqualTo("PROGRAMADA");
    }

    @Test
    @DisplayName("EST-02: Una cita NO especializada tiene esEspecializada = false")
    void estadoCita_noEspecializada_esEspecializadaFalse() {
        // ARRANGE
        LocalDateTime fechaValida = proximoLunesA(11);
        CitaRequest req = buildCitaRequest(10L, 20L, fechaValida);

        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(pacienteAdulto));
        when(medicoRepository.findById(20L)).thenReturn(Optional.of(medicoGeneral));
        when(citaRepository.existsByMedicoUsuarioIdAndFechaHora(anyLong(), any())).thenReturn(false);
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> {
            Cita cita = inv.getArgument(0);
            // La cita con médico general NO es especializada
            assertThat(cita.getEsEspecializada()).isFalse();
            return copiarCitaConId(cita, 100L);
        });

        // ACT & ASSERT
        assertThatCode(() -> citaService.agendarCita(req, "ROLE_PACIENTE")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("EST-03: Una cita especializada (Cardiología) tiene esEspecializada = true")
    void estadoCita_especializada_esEspecializadaTrue() {
        // ARRANGE
        LocalDateTime fechaValida = proximoLunesA(11);
        CitaRequest req = buildCitaRequest(10L, 22L, fechaValida);

        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(pacienteAdulto));
        when(medicoRepository.findById(22L)).thenReturn(Optional.of(medicoCardiologo));
        when(citaRepository.existsByMedicoUsuarioIdAndFechaHora(anyLong(), any())).thenReturn(false);
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> {
            Cita cita = inv.getArgument(0);
            // La cita con cardiólogo SÍ es especializada
            assertThat(cita.getEsEspecializada()).isTrue();
            return copiarCitaConId(cita, 101L);
        });

        // ACT (solo un MEDICO puede hacer esto)
        assertThatCode(() -> citaService.agendarCita(req, "ROLE_MEDICO")).doesNotThrowAnyException();
    }

    // ─── Helpers ────────────────────────────────────────────

    private CitaRequest buildCitaRequest(Long pacienteId, Long medicoId, LocalDateTime fechaHora) {
        CitaRequest req = new CitaRequest();
        req.setPacienteId(pacienteId);
        req.setMedicoId(medicoId);
        req.setFechaHora(fechaHora);
        return req;
    }

    /** Retorna la próxima ocurrencia de un lunes a la hora especificada */
    private LocalDateTime proximoLunesA(int hora) {
        LocalDateTime ahora = LocalDateTime.now().plusDays(1);
        while (ahora.getDayOfWeek().getValue() != 1) {
            ahora = ahora.plusDays(1);
        }
        return ahora.withHour(hora).withMinute(0).withSecond(0).withNano(0);
    }

    /** Retorna la próxima ocurrencia de un sábado a la hora especificada */
    private LocalDateTime proximoSabadoA(int hora) {
        LocalDateTime ahora = LocalDateTime.now().plusDays(1);
        while (ahora.getDayOfWeek().getValue() != 6) {
            ahora = ahora.plusDays(1);
        }
        return ahora.withHour(hora).withMinute(0).withSecond(0).withNano(0);
    }

    /** Simula el guardado asignando un ID a la cita */
    private Cita copiarCitaConId(Cita original, Long id) {
        Cita copia = new Cita();
        copia.setId(id);
        copia.setPaciente(original.getPaciente());
        copia.setMedico(original.getMedico());
        copia.setFechaHora(original.getFechaHora());
        copia.setEstado(original.getEstado());
        copia.setEsEspecializada(original.getEsEspecializada());
        return copia;
    }
}
