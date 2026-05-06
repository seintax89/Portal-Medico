package com.eps.portal.service;

import com.eps.portal.dto.request.HistorialClinicoRequest;
import com.eps.portal.dto.response.HistorialClinicoResponse;
import com.eps.portal.entity.*;
import com.eps.portal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistorialClinicoService {

    private final HistorialClinicoRepository historialRepository;
    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public void registrarEvolucion(HistorialClinicoRequest request, String emailMedico) {
        Usuario usuarioMedico = usuarioRepository.findByEmail(emailMedico)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado"));

        Cita cita = citaRepository.findById(request.getCitaId())
                .orElseThrow(() -> new RuntimeException("La cita especificada no existe"));

        // REGLA DE NEGOCIO: Seguridad de turnos
        if (!cita.getMedico().getUsuarioId().equals(usuarioMedico.getId())) {
            throw new RuntimeException("Alerta de Seguridad: No puede registrar una evolución en una cita asignada a otro colega.");
        }

        // REGLA DE NEGOCIO: Unidad de historia (1 cita = 1 evolución)
        if (historialRepository.existsByCitaId(cita.getId())) {
            throw new RuntimeException("Esta cita ya fue atendida y tiene una evolución registrada.");
        }

        // 1. Guardamos la evolución médica
        HistorialClinico historial = new HistorialClinico();
        historial.setCita(cita);
        historial.setPaciente(cita.getPaciente());
        historial.setMedico(cita.getMedico());
        historial.setDiagnostico(request.getDiagnostico());
        historial.setObservaciones(request.getObservaciones());
        historial.setFechaRegistro(LocalDateTime.now());

        historialRepository.save(historial);

        // 2. Actualizamos la Cita de "PROGRAMADA" a "COMPLETADA"
        cita.setEstado("COMPLETADA");
        citaRepository.save(cita);
    }

    public List<HistorialClinicoResponse> obtenerHistorial(Long pacienteId, Authentication auth) {
        // En un sistema real aquí validaríamos si 'auth' es el mismo paciente o su médico tratante

        // Obtenemos los registros ordenados del más reciente al más antiguo
        List<HistorialClinico> registros = historialRepository.findByPacienteUsuarioIdOrderByFechaRegistroDesc(pacienteId);

        return registros.stream().map(h -> HistorialClinicoResponse.builder()
                .idRegistro(h.getId())
                .fechaAtencion(h.getFechaRegistro())
                .doctorQueAtendio("Dr. " + h.getMedico().getNombres() + " " + h.getMedico().getApellidos())
                .especialidad(h.getMedico().getEspecialidad().getNombre())
                .diagnostico(h.getDiagnostico())
                .observaciones(h.getObservaciones())
                .build()
        ).collect(Collectors.toList());
    }
}