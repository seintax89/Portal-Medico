package com.eps.portal.modelo.service;

import com.eps.portal.modelo.dto.request.RegistroMedicoRequest;
import com.eps.portal.modelo.dto.request.RegistroPacienteRequest;
import com.eps.portal.modelo.entity.*;
import com.eps.portal.modelo.repository.*;
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
     * Crea tanto las credenciales (Usuario) como el historial demogrÃ¡fico (Paciente).
     */
    @Transactional
    public void registrarPaciente(RegistroPacienteRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El correo electrÃ³nico ya estÃ¡ registrado en el sistema.");
        }

        // 1. Crear Credenciales de Acceso (Tabla usuarios)
        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword())); // Â¡EncriptaciÃ³n obligatoria!

        Role rolPaciente = roleRepository.findByNombre("ROLE_PACIENTE")
                .orElseThrow(() -> new RuntimeException("Error interno: El rol PACIENTE no existe en la base de datos."));
        usuario.setRol(rolPaciente);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // 2. Crear Perfil ClÃ­nico (Tabla pacientes)
        Paciente paciente = new Paciente();
        // El ID del paciente DEBE ser exactamente el mismo ID del usuario (RelaciÃ³n 1:1)
        paciente.setUsuario(usuarioGuardado);
        paciente.setNombres(request.getNombres());
        paciente.setApellidos(request.getApellidos());
        paciente.setTipoDocumento(request.getTipoDocumento());
        paciente.setNumeroDocumento(request.getNumeroDocumento());
        paciente.setFechaNacimiento(request.getFechaNacimiento());

        // LÃ“GICA DE NEGOCIO: ClasificaciÃ³n automÃ¡tica por edad
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
     * Registra un nuevo MÃ©dico en el sistema (Normalmente usado por un Administrador).
     */
    @Transactional
    public void registrarMedico(RegistroMedicoRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El correo electrÃ³nico ya estÃ¡ registrado en el sistema.");
        }

        // 1. Crear Credenciales de Acceso
        Usuario usuario = new Usuario();
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));

        Role rolMedico = roleRepository.findByNombre("ROLE_MEDICO")
                .orElseThrow(() -> new RuntimeException("Error interno: El rol MEDICO no existe en la base de datos."));
        usuario.setRol(rolMedico);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // 2. Validar que la especialidad exista en el catÃ¡logo
        Especialidad especialidad = especialidadRepository.findById(request.getEspecialidadId())
                .orElseThrow(() -> new RuntimeException("La especialidad mÃ©dica seleccionada no es vÃ¡lida."));

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
