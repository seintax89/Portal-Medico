package com.eps.portal.controller;

import com.eps.portal.dto.request.CitaRequest;
import com.eps.portal.dto.response.CitaResponse;
import com.eps.portal.service.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private final CitaService citaService;

    // Solo Pacientes o Médicos logueados pueden crear citas
    @PostMapping
    @PreAuthorize("hasRole('PACIENTE') or hasRole('MEDICO')")
    public ResponseEntity<?> agendarCita(@Valid @RequestBody CitaRequest request, Authentication authentication) {
        try {
            // Extraemos el rol del usuario que está haciendo la petición desde el Token JWT
            String rolUsuario = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("");

            CitaResponse response = citaService.agendarCita(request, rolUsuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            // Manejo básico de errores (Fase 11 inicial): Devolvemos un HTTP 400 con el mensaje de la regla rota
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}