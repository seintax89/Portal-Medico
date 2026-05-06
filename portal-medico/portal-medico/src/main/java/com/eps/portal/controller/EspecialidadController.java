package com.eps.portal.controller;

import com.eps.portal.entity.Especialidad;
import com.eps.portal.entity.OrdenEspecialidad;
import com.eps.portal.entity.Paciente;
import com.eps.portal.entity.Usuario;
import com.eps.portal.repository.EspecialidadRepository;
import com.eps.portal.repository.OrdenEspecialidadRepository;
import com.eps.portal.repository.PacienteRepository;
import com.eps.portal.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/especialidades")
@RequiredArgsConstructor
public class EspecialidadController {

    // Nota: Como es un catálogo simple (solo lectura), es aceptable usar el Repository
    // directamente aquí sin pasar por un Service.
    private final EspecialidadRepository especialidadRepository;
    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final OrdenEspecialidadRepository ordenEspecialidadRepository;

    @GetMapping
    public ResponseEntity<List<Especialidad>> listarEspecialidades() {
        return ResponseEntity.ok(especialidadRepository.findAll());
    }

    @GetMapping("/disponibles")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<List<Especialidad>> listarEspecialidadesDisponibles(Authentication authentication) {
        String email = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Paciente paciente = pacienteRepository.findById(usuario.getId()).orElseThrow(() -> new RuntimeException("Paciente no encontrado"));

        boolean esAdulto = !"NINO".equalsIgnoreCase(paciente.getTipoPaciente());

        List<Especialidad> todas = especialidadRepository.findAll();
        List<Especialidad> disponibles = new ArrayList<>();

        for (Especialidad e : todas) {
            if ("MEDICINA_GENERAL".equalsIgnoreCase(e.getNombre()) && esAdulto) {
                disponibles.add(e);
            } else if ("PEDIATRIA".equalsIgnoreCase(e.getNombre()) && !esAdulto) {
                disponibles.add(e);
            }
        }

        List<OrdenEspecialidad> ordenes = ordenEspecialidadRepository.findByPacienteUsuarioIdAndUsadaFalse(paciente.getUsuarioId());
        for (OrdenEspecialidad orden : ordenes) {
            if (!disponibles.contains(orden.getEspecialidad())) {
                disponibles.add(orden.getEspecialidad());
            }
        }

        return ResponseEntity.ok(disponibles);
    }
}