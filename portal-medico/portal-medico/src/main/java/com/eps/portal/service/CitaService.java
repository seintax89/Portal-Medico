package com.eps.portal.service;

import com.eps.portal.dto.request.CitaRequest;
import com.eps.portal.dto.response.CitaResponse;
import com.eps.portal.entity.Cita;
import com.eps.portal.entity.Medico;
import com.eps.portal.entity.Paciente;
import com.eps.portal.entity.Especialidad;
import com.eps.portal.entity.OrdenEspecialidad;
import com.eps.portal.repository.CitaRepository;
import com.eps.portal.repository.EspecialidadRepository;
import com.eps.portal.repository.MedicoRepository;
import com.eps.portal.repository.OrdenEspecialidadRepository;
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
    private final EspecialidadRepository especialidadRepository;
    private final OrdenEspecialidadRepository ordenEspecialidadRepository;

    @Transactional // Si falla alguna regla, deshace cualquier cambio en BD
    public CitaResponse agendarCita(CitaRequest request, String rolUsuarioLogueado) {

        // 1. Validar que existan el paciente y el médico
        Paciente paciente = pacienteRepository.findById(request.getPacienteId())
                .orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        Especialidad especialidad = especialidadRepository.findById(request.getEspecialidadId())
                .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));

        LocalDateTime fechaHoraCita = request.getFechaHora();

        // Horario EPS
        int horaCita = fechaHoraCita.getHour();
        int diaSemana = fechaHoraCita.getDayOfWeek().getValue();
        if (diaSemana > 5) {
            throw new RuntimeException("La EPS no atiende fines de semana.");
        }
        if (horaCita < 8 || horaCita >= 18) {
            throw new RuntimeException("La cita está fuera del horario de atención de la EPS (8:00 AM - 6:00 PM).");
        }
        if (fechaHoraCita.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("No se pueden agendar citas en el pasado.");
        }

        boolean esAdulto = !"NINO".equalsIgnoreCase(paciente.getTipoPaciente());
        boolean esPediatria = "PEDIATRIA".equalsIgnoreCase(especialidad.getNombre());
        boolean esMedGeneral = "MEDICINA_GENERAL".equalsIgnoreCase(especialidad.getNombre());
        
        // Validación de especialidad base
        if (!esAdulto && esMedGeneral) {
            throw new RuntimeException("Los pacientes infantiles deben agendar en PEDIATRÍA, no en Medicina General.");
        }
        if (esAdulto && esPediatria) {
            throw new RuntimeException("Los pacientes adultos no pueden agendar en PEDIATRÍA.");
        }

        boolean esEspecializada = !esMedGeneral && !esPediatria;
        OrdenEspecialidad ordenUsar = null;

        if (esEspecializada && "ROLE_PACIENTE".equals(rolUsuarioLogueado)) {
            java.util.List<OrdenEspecialidad> ordenes = ordenEspecialidadRepository.findByPacienteUsuarioIdAndUsadaFalse(paciente.getUsuarioId());
            ordenUsar = ordenes.stream()
                    .filter(o -> o.getEspecialidad().getId().equals(especialidad.getId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No tienes una orden activa para agendar en esta especialidad (" + especialidad.getNombre() + ")."));
        }

        // Asignación automática de médico
        java.util.List<Medico> medicosEspecialidad = medicoRepository.findAll().stream()
                .filter(m -> m.getEspecialidad().getId().equals(especialidad.getId()))
                .toList();

        if (medicosEspecialidad.isEmpty()) {
            throw new RuntimeException("No hay médicos registrados para la especialidad " + especialidad.getNombre());
        }

        Medico medicoAsignado = null;
        for (Medico m : medicosEspecialidad) {
            boolean ocupado = citaRepository.existsByMedicoUsuarioIdAndFechaHora(m.getUsuarioId(), fechaHoraCita);
            if (!ocupado) {
                medicoAsignado = m;
                break;
            }
        }

        if (medicoAsignado == null) {
            throw new RuntimeException("No hay médicos disponibles para la fecha y hora seleccionadas en esa especialidad.");
        }

        // ====================================================================
        // Si pasa todas las validaciones, creamos la Cita
        // ====================================================================
        Cita nuevaCita = new Cita();
        nuevaCita.setPaciente(paciente);
        nuevaCita.setMedico(medicoAsignado);
        nuevaCita.setFechaHora(fechaHoraCita);
        nuevaCita.setEstado("PROGRAMADA");
        nuevaCita.setEsEspecializada(esEspecializada);

        Cita citaGuardada = citaRepository.save(nuevaCita);
        
        if (ordenUsar != null) {
            ordenUsar.setUsada(true);
            ordenEspecialidadRepository.save(ordenUsar);
        }

        return CitaResponse.builder()
                .idCita(citaGuardada.getId())
                .nombrePaciente(paciente.getNombres() + " " + paciente.getApellidos())
                .nombreMedico("Dr. " + medicoAsignado.getNombres() + " " + medicoAsignado.getApellidos())
                .especialidad(medicoAsignado.getEspecialidad().getNombre())
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

    @Transactional
    public void cancelarCita(Long idCita, String email) {
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));
        
        if (!cita.getPaciente().getUsuario().getEmail().equals(email)) {
            throw new RuntimeException("No tienes permiso para cancelar esta cita.");
        }
        
        if (!"PROGRAMADA".equals(cita.getEstado())) {
            throw new RuntimeException("Solo se pueden cancelar citas en estado PROGRAMADA.");
        }
        
        cita.setEstado("CANCELADA");
        citaRepository.save(cita);
    }
}