package com.eps.portal.service;

import com.eps.portal.dto.request.RegistroMedicoRequest;
import com.eps.portal.dto.request.RegistroPacienteRequest;
import com.eps.portal.entity.*;
import com.eps.portal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final RoleRepository roleRepository;
    private final EspecialidadRepository especialidadRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registra un nuevo Paciente en el sistema.
     * Crea tanto las credenciales (Usuario) como el historial demográfico (Paciente).
     */
    @Transactional
    public void registrarPaciente(RegistroPacienteRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El correo electrónico ya está registrado en el sistema.");
        }

        // 1. Crear Credenciales de Acceso (Tabla usuarios)
        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword())); // ¡Encriptación obligatoria!

        Role rolPaciente = roleRepository.findByNombre("ROLE_PACIENTE")
                .orElseThrow(() -> new RuntimeException("Error interno: El rol PACIENTE no existe en la base de datos."));
        usuario.setRol(rolPaciente);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // 2. Crear Perfil Clínico (Tabla pacientes)
        Paciente paciente = new Paciente();
        // El ID del paciente DEBE ser exactamente el mismo ID del usuario (Relación 1:1)
        paciente.setUsuario(usuarioGuardado);
        paciente.setNombres(request.getNombres());
        paciente.setApellidos(request.getApellidos());
        paciente.setTipoDocumento(request.getTipoDocumento());
        paciente.setNumeroDocumento(request.getNumeroDocumento());
        paciente.setFechaNacimiento(request.getFechaNacimiento());

        // LÓGICA DE NEGOCIO: Clasificación automática por edad
        int edad = LocalDate.now().getYear() - request.getFechaNacimiento().getYear();
        if (edad < 14) {
            paciente.setTipoPaciente("NINO");
        } else if (edad >= 60) {
            paciente.setTipoPaciente("ADULTO_MAYOR");
        } else {
            paciente.setTipoPaciente("ADULTO");
        }

        pacienteRepository.save(paciente);
    }

    /**
     * Registra un nuevo Médico en el sistema (Normalmente usado por un Administrador).
     */
    @Transactional
    public void registrarMedico(RegistroMedicoRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El correo electrónico ya está registrado en el sistema.");
        }

        // 1. Crear Credenciales de Acceso
        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));

        Role rolMedico = roleRepository.findByNombre("ROLE_MEDICO")
                .orElseThrow(() -> new RuntimeException("Error interno: El rol MEDICO no existe en la base de datos."));
        usuario.setRol(rolMedico);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // 2. Validar que la especialidad exista en el catálogo
        Especialidad especialidad = especialidadRepository.findById(request.getEspecialidadId())
                .orElseThrow(() -> new RuntimeException("La especialidad médica seleccionada no es válida."));

        // 3. Crear Perfil Profesional (Tabla medicos)
        Medico medico = new Medico();
        medico.setUsuario(usuarioGuardado);
        medico.setNombres(request.getNombres());
        medico.setApellidos(request.getApellidos());
        medico.setRegistroMedico(request.getRegistroMedico());
        medico.setEspecialidad(especialidad);

        medicoRepository.save(medico);
    }
}