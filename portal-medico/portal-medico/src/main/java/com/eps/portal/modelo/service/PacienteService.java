package com.eps.portal.modelo.service;

import com.eps.portal.modelo.dto.response.PacienteResponse;
import com.eps.portal.modelo.dto.request.ActualizarPerfilRequest;
import com.eps.portal.modelo.dto.response.MedicamentoRecetadoResponse;
import com.eps.portal.modelo.entity.Paciente;
import com.eps.portal.modelo.entity.Usuario;
import com.eps.portal.modelo.repository.FormulaMedicaRepository;
import com.eps.portal.modelo.repository.PacienteRepository;
import com.eps.portal.modelo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final FormulaMedicaRepository formulaMedicaRepository;
    private final PasswordEncoder passwordEncoder;

    public PacienteResponse obtenerPerfilPorEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado en el sistema"));

        Paciente paciente = pacienteRepository.findById(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Perfil de paciente no encontrado"));

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

    @Transactional
    public PacienteResponse actualizarPerfil(String email, ActualizarPerfilRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Paciente paciente = pacienteRepository.findById(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Perfil de paciente no encontrado"));

        // Verificar si el nuevo correo ya estÃ¡ en uso por otro usuario
        if (!usuario.getEmail().equals(request.getEmail()) && usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El correo ya estÃ¡ en uso por otro usuario.");
        }

        usuario.setEmail(request.getEmail());
        
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        usuarioRepository.save(usuario);

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

    public java.util.List<MedicamentoRecetadoResponse> obtenerMisMedicamentos(String email) {
        return formulaMedicaRepository.findByHistorialClinicoPacienteUsuarioEmailOrderByHistorialClinicoFechaRegistroDesc(email)
                .stream()
                .map(formula -> MedicamentoRecetadoResponse.builder()
                        .medicamentoNombre(formula.getMedicamento().getNombreGenerico())
                        .dosis(formula.getDosis())
                        .frecuencia(formula.getFrecuencia())
                        .duracionDias(formula.getDuracionDias())
                        .medicoNombre("Dr. " + formula.getHistorialClinico().getMedico().getNombres() + " " + formula.getHistorialClinico().getMedico().getApellidos())
                        .fechaReceta(formula.getHistorialClinico().getFechaRegistro())
                        .build())
                .collect(Collectors.toList());
    }
}
