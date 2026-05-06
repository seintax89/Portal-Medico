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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias + Estado - CitaService")
class CitaServiceTest {

    @Mock private CitaRepository citaRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private MedicoRepository medicoRepository;
    @Mock private EspecialidadRepository especialidadRepository;
    @Mock private OrdenEspecialidadRepository ordenEspecialidadRepository;

    @InjectMocks
    private CitaService citaService;

    private Paciente pacienteAdulto;
    private Paciente pacienteNino;
    private Medico medicoGeneral;
    private Medico medicoPediatra;
    private Medico medicoCardiologo;
    private Especialidad espGeneral;
    private Especialidad espPediatria;
    private Especialidad espCardiologia;

    @BeforeEach
    void setUp() {
        espGeneral = new Especialidad();
        espGeneral.setId(1);
        espGeneral.setNombre("MEDICINA_GENERAL");

        espPediatria = new Especialidad();
        espPediatria.setId(2);
        espPediatria.setNombre("PEDIATRIA");

        espCardiologia = new Especialidad();
        espCardiologia.setId(3);
        espCardiologia.setNombre("CARDIOLOGIA");

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

    @Test
    @DisplayName("Agendar cita exitosa — paciente adulto con médico general")
    void agendarCita_pacienteAdulto_medicoGeneral_exitoso() {
        LocalDateTime fechaValida = proximoLunesA(10);
        CitaRequest req = buildCitaRequest(10L, 1L, fechaValida);

        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(pacienteAdulto));
        when(especialidadRepository.findById(1L)).thenReturn(Optional.of(espGeneral));
        when(medicoRepository.findAll()).thenReturn(List.of(medicoGeneral, medicoPediatra, medicoCardiologo));
        when(citaRepository.existsByMedicoUsuarioIdAndFechaHora(anyLong(), any())).thenReturn(false);
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> copiarCitaConId(inv.getArgument(0), 1L));

        CitaResponse resp = citaService.agendarCita(req, "ROLE_PACIENTE");

        assertThat(resp).isNotNull();
        assertThat(resp.getEstado()).isEqualTo("PROGRAMADA");
        assertThat(resp.getNombrePaciente()).contains("Carlos");
    }

    @Test
    @DisplayName("Agendar cita en fin de semana → RuntimeException")
    void agendarCita_finDeSemana_lanzaExcepcion() {
        LocalDateTime sabado = proximoSabadoA(10);
        CitaRequest req = buildCitaRequest(10L, 1L, sabado);

        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(pacienteAdulto));
        when(especialidadRepository.findById(1L)).thenReturn(Optional.of(espGeneral));

        assertThatThrownBy(() -> citaService.agendarCita(req, "ROLE_PACIENTE"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("fines de semana");
    }

    @Test
    @DisplayName("Solapamiento de cita → RuntimeException")
    void agendarCita_solapamiento_lanzaExcepcion() {
        LocalDateTime fechaValida = proximoLunesA(10);
        CitaRequest req = buildCitaRequest(10L, 1L, fechaValida);

        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(pacienteAdulto));
        when(especialidadRepository.findById(1L)).thenReturn(Optional.of(espGeneral));
        when(medicoRepository.findAll()).thenReturn(List.of(medicoGeneral));
        when(citaRepository.existsByMedicoUsuarioIdAndFechaHora(anyLong(), any())).thenReturn(true);

        assertThatThrownBy(() -> citaService.agendarCita(req, "ROLE_PACIENTE"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No hay médicos disponibles");
    }

    @Test
    @DisplayName("Niño en PEDIATRÍA → cita exitosa")
    void agendarCita_ninoEnPediatria_exitoso() {
        LocalDateTime fechaValida = proximoLunesA(10);
        CitaRequest req = buildCitaRequest(11L, 2L, fechaValida);

        when(pacienteRepository.findById(11L)).thenReturn(Optional.of(pacienteNino));
        when(especialidadRepository.findById(2L)).thenReturn(Optional.of(espPediatria));
        when(medicoRepository.findAll()).thenReturn(List.of(medicoPediatra));
        when(citaRepository.existsByMedicoUsuarioIdAndFechaHora(anyLong(), any())).thenReturn(false);
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> copiarCitaConId(inv.getArgument(0), 2L));

        CitaResponse resp = citaService.agendarCita(req, "ROLE_PACIENTE");

        assertThat(resp.getEstado()).isEqualTo("PROGRAMADA");
        assertThat(resp.getEspecialidad()).isEqualTo("PEDIATRIA");
    }

    @Test
    @DisplayName("Paciente intenta agendar cita especializada (Cardiología) sin orden → RuntimeException")
    void agendarCita_pacienteAgendaEspecializadaSinOrden_lanzaExcepcion() {
        LocalDateTime fechaValida = proximoLunesA(10);
        CitaRequest req = buildCitaRequest(10L, 3L, fechaValida);

        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(pacienteAdulto));
        when(especialidadRepository.findById(3L)).thenReturn(Optional.of(espCardiologia));
        when(ordenEspecialidadRepository.findByPacienteUsuarioIdAndUsadaFalse(10L)).thenReturn(List.of());

        assertThatThrownBy(() -> citaService.agendarCita(req, "ROLE_PACIENTE"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No tienes una orden activa");
    }

    private CitaRequest buildCitaRequest(Long pacienteId, Long especialidadId, LocalDateTime fechaHora) {
        CitaRequest req = new CitaRequest();
        req.setPacienteId(pacienteId);
        req.setEspecialidadId(especialidadId);
        req.setFechaHora(fechaHora);
        return req;
    }

    private LocalDateTime proximoLunesA(int hora) {
        LocalDateTime ahora = LocalDateTime.now().plusDays(1);
        while (ahora.getDayOfWeek().getValue() != 1) {
            ahora = ahora.plusDays(1);
        }
        return ahora.withHour(hora).withMinute(0).withSecond(0).withNano(0);
    }

    private LocalDateTime proximoSabadoA(int hora) {
        LocalDateTime ahora = LocalDateTime.now().plusDays(1);
        while (ahora.getDayOfWeek().getValue() != 6) {
            ahora = ahora.plusDays(1);
        }
        return ahora.withHour(hora).withMinute(0).withSecond(0).withNano(0);
    }

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
