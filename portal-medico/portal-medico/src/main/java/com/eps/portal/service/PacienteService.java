package com.eps.portal.service;

import com.eps.portal.dto.response.PacienteResponse;
import com.eps.portal.dto.request.ActualizarPerfilRequest;
import com.eps.portal.dto.response.MedicamentoRecetadoResponse;
import com.eps.portal.entity.Paciente;
import com.eps.portal.entity.Usuario;
import com.eps.portal.repository.FormulaMedicaRepository;
import com.eps.portal.repository.PacienteRepository;
import com.eps.portal.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final FormulaMedicaRepository formulaMedicaRepository;

    public PacienteResponse obtenerPerfilPorEmail(String email) {
        // 1. Buscamos al usuario base
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en el sistema"));

        // 2. Buscamos sus datos clínicos/personales
        Paciente paciente = pacienteRepository.findById(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Perfil de paciente no encontrado"));

        // 3. Mapeamos a la respuesta segura (sin passwords)
        return PacienteResponse.builder()
                .id(paciente.getUsuarioId())
                .email(usuario.getEmail())
                .nombres(paciente.getNombres())
                .apellidos(paciente.getApellidos())
                .numeroDocumento(paciente.getNumeroDocumento())
                .fechaNacimiento(paciente.getFechaNacimiento())
                .tipoPaciente(paciente.getTipoPaciente())
                .build();
    }

    public PacienteResponse actualizarPerfil(String email, ActualizarPerfilRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Paciente paciente = pacienteRepository.findById(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Perfil de paciente no encontrado"));

        paciente.setNombres(request.getNombres());
        paciente.setApellidos(request.getApellidos());
        paciente.setNumeroDocumento(request.getNumeroDocumento());
        if(request.getFechaNacimiento() != null) {
            paciente.setFechaNacimiento(request.getFechaNacimiento());
        }

        Paciente pacienteActualizado = pacienteRepository.save(paciente);

        return PacienteResponse.builder()
                .id(pacienteActualizado.getUsuarioId())
                .email(usuario.getEmail())
                .nombres(pacienteActualizado.getNombres())
                .apellidos(pacienteActualizado.getApellidos())
                .numeroDocumento(pacienteActualizado.getNumeroDocumento())
                .fechaNacimiento(pacienteActualizado.getFechaNacimiento())
                .tipoPaciente(pacienteActualizado.getTipoPaciente())
                .build();
    }

    public java.util.List<MedicamentoRecetadoResponse> obtenerMisMedicamentos(String email) {
        return formulaMedicaRepository.findByHistorialClinicoPacienteUsuarioEmailOrderByHistorialClinicoFechaRegistroDesc(email)
                .stream()
                .map(formula -> MedicamentoRecetadoResponse.builder()
                        .medicamentoNombre(formula.getMedicamento().getNombre())
                        .dosis(formula.getDosis())
                        .frecuencia(formula.getFrecuencia())
                        .duracionDias(formula.getDuracionDias())
                        .medicoNombre("Dr. " + formula.getHistorialClinico().getMedico().getNombres() + " " + formula.getHistorialClinico().getMedico().getApellidos())
                        .fechaReceta(formula.getHistorialClinico().getFechaRegistro())
                        .build())
                .toList();
    }
}