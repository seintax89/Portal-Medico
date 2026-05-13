package com.eps.portal.modelo.service;

import com.eps.portal.modelo.dto.response.MedicoResponse;
import com.eps.portal.modelo.entity.Medico;
import com.eps.portal.modelo.repository.MedicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicoService {

    private final MedicoRepository medicoRepository;

    public List<MedicoResponse> obtenerDirectorio(Integer especialidadId) {
        List<Medico> medicos;

        // Si nos envÃ­an un ID de especialidad, filtramos. Si no, mostramos todos.
        if (especialidadId != null) {
            medicos = medicoRepository.findByEspecialidadId(especialidadId);
        } else {
            medicos = medicoRepository.findAll();
        }

        // Convertimos la lista de Entidades a lista de DTOs usando Streams
        return medicos.stream().map(medico -> MedicoResponse.builder()
                .id(medico.getUsuarioId())
                .nombreCompleto("Dr. " + medico.getNombres() + " " + medico.getApellidos())
                .especialidad(medico.getEspecialidad().getNombre())
                .registroMedico(medico.getRegistroMedico())
                .build()
        ).collect(Collectors.toList());
    }
}
