package com.eps.portal.service;

import com.eps.portal.dto.request.CitaRequest;
import com.eps.portal.dto.response.CitaResponse;
import com.eps.portal.entity.Cita;
import com.eps.portal.entity.Medico;
import com.eps.portal.entity.Paciente;
import com.eps.portal.repository.CitaRepository;
import com.eps.portal.repository.MedicoRepository;
import com.eps.portal.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class CitaService {

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;

    @Transactional // Si falla alguna regla, deshace cualquier cambio en BD
    public CitaResponse agendarCita(CitaRequest request, String rolUsuarioLogueado) {

        // 1. Validar que existan el paciente y el médico
        Paciente paciente = pacienteRepository.findById(request.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Medico medico = medicoRepository.findById(request.getMedicoId())
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        LocalDateTime fechaHoraCita = request.getFechaHora();

        // ====================================================================
        // APLICACIÓN ESTRICTA DE REGLAS DE NEGOCIO (Fase 7)
        // ====================================================================

        // RN-01: Horario EPS (Ejemplo: Lunes a Viernes, 8:00 AM a 6:00 PM)
        int horaCita = fechaHoraCita.getHour();
        int diaSemana = fechaHoraCita.getDayOfWeek().getValue(); // 1=Lunes, 7=Domingo

        if (diaSemana > 5) {
            throw new RuntimeException("La EPS no atiende fines de semana.");
        }
        if (horaCita < 8 || horaCita >= 18) {
            throw new RuntimeException("La cita está fuera del horario de atención de la EPS (8:00 AM - 6:00 PM).");
        }

        // RN-02: Fecha pasada (Ya está validado parcialmente por @Future en el DTO, pero lo reforzamos)
        if (fechaHoraCita.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("No se pueden agendar citas en el pasado.");
        }

        // RN-03: Solapamiento (Verificamos si el médico ya tiene una cita a esa misma hora)
        boolean existeSolapamiento = citaRepository.existsByMedicoUsuarioIdAndFechaHora(medico.getUsuarioId(), fechaHoraCita);
        if (existeSolapamiento) {
            throw new RuntimeException("El médico seleccionado ya tiene una cita asignada en ese horario.");
        }

        // RN-05: Regla Pediátrica (Niños solo en pediatría)
        if ("NINO".equalsIgnoreCase(paciente.getTipoPaciente()) &&
                !"PEDIATRIA".equalsIgnoreCase(medico.getEspecialidad().getNombre())) {
            throw new RuntimeException("Los pacientes infantiles solo pueden ser agendados en la especialidad de PEDIATRÍA.");
        }

        // RN-04: Citas especializadas (Un paciente no puede agendar directo a Cardiología, necesita que lo haga un médico)
        boolean esEspecializada = !"MEDICINA_GENERAL".equalsIgnoreCase(medico.getEspecialidad().getNombre()) &&
                !"PEDIATRIA".equalsIgnoreCase(medico.getEspecialidad().getNombre());

        if (esEspecializada && "ROLE_PACIENTE".equals(rolUsuarioLogueado)) {
            throw new RuntimeException("Las citas especializadas (Ej. Cardiología) requieren remisión y deben ser agendadas por un Médico.");
        }

        // ====================================================================
        // Si pasa todas las validaciones, creamos la Cita
        // ====================================================================
        Cita nuevaCita = new Cita();
        nuevaCita.setPaciente(paciente);
        nuevaCita.setMedico(medico);
        nuevaCita.setFechaHora(fechaHoraCita);
        nuevaCita.setEstado("PROGRAMADA");
        nuevaCita.setEsEspecializada(esEspecializada);

        Cita citaGuardada = citaRepository.save(nuevaCita);

        // Retornamos el DTO de respuesta para no exponer las contraseñas/entidades al cliente
        return CitaResponse.builder()
                .idCita(citaGuardada.getId())
                .nombrePaciente(paciente.getNombres() + " " + paciente.getApellidos())
                .nombreMedico("Dr. " + medico.getNombres() + " " + medico.getApellidos())
                .especialidad(medico.getEspecialidad().getNombre())
                .fechaHora(citaGuardada.getFechaHora())
                .estado(citaGuardada.getEstado())
                .build();
    }

    public java.util.List<CitaResponse> obtenerMisCitas(String email) {
        return citaRepository.findByPacienteUsuarioEmailOrderByFechaHoraDesc(email).stream()
                .map(cita -> CitaResponse.builder()
                        .idCita(cita.getId())
                        .nombrePaciente(cita.getPaciente().getNombres() + " " + cita.getPaciente().getApellidos())
                        .nombreMedico("Dr. " + cita.getMedico().getNombres() + " " + cita.getMedico().getApellidos())
                        .especialidad(cita.getMedico().getEspecialidad().getNombre())
                        .fechaHora(cita.getFechaHora())
                        .estado(cita.getEstado())
                        .build())
                .toList();
    }
}