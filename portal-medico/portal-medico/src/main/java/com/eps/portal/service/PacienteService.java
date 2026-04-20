package com.eps.portal.service;

import com.eps.portal.dto.response.PacienteResponse;
import com.eps.portal.entity.Paciente;
import com.eps.portal.entity.Usuario;
import com.eps.portal.repository.PacienteRepository;
import com.eps.portal.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;

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
}