package com.eps.portal.controller;

import com.eps.portal.dto.response.PacienteResponse;
import com.eps.portal.service.PacienteService; // Deberás crear este servicio
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import com.eps.portal.dto.request.ActualizarPerfilRequest;
import com.eps.portal.dto.response.MedicamentoRecetadoResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    @GetMapping("/mi-perfil")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<PacienteResponse> obtenerMiPerfil(Authentication authentication) {
        // Extraemos el email o ID del token validado por Spring Security
        String emailUsuario = authentication.getName();

        PacienteResponse perfil = pacienteService.obtenerPerfilPorEmail(emailUsuario);
        return ResponseEntity.ok(perfil);
    }

    @PutMapping("/mi-perfil")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<PacienteResponse> actualizarMiPerfil(@Valid @RequestBody ActualizarPerfilRequest request, Authentication authentication) {
        String emailUsuario = authentication.getName();
        PacienteResponse perfil = pacienteService.actualizarPerfil(emailUsuario, request);
        return ResponseEntity.ok(perfil);
    }

    @GetMapping("/mis-medicamentos")
    @PreAuthorize("hasRole('PACIENTE')")
    public ResponseEntity<java.util.List<MedicamentoRecetadoResponse>> obtenerMisMedicamentos(Authentication authentication) {
        String emailUsuario = authentication.getName();
        return ResponseEntity.ok(pacienteService.obtenerMisMedicamentos(emailUsuario));
    }
}
