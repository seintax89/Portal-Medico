package com.eps.portal.controller;

import com.eps.portal.dto.request.HistorialClinicoRequest;
import com.eps.portal.dto.response.HistorialClinicoResponse;
import com.eps.portal.dto.response.MensajeResponse;
import com.eps.portal.service.HistorialClinicoService; // Deberás crear este servicio
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/historial")
@RequiredArgsConstructor
public class HistorialClinicoController {

    private final HistorialClinicoService historialService;

    // Solo un médico puede redactar una evolución clínica
    @PostMapping
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<MensajeResponse> registrarAtencion(
            @Valid @RequestBody HistorialClinicoRequest request,
            Authentication authentication) {

        String emailMedico = authentication.getName();
        historialService.registrarEvolucion(request, emailMedico);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MensajeResponse("Evolución clínica guardada exitosamente."));
    }

    // Un paciente puede ver su propio historial, un médico puede ver el de sus pacientes
    @GetMapping("/paciente/{pacienteId}")
    @PreAuthorize("hasRole('PACIENTE') or hasRole('MEDICO')")
    public ResponseEntity<List<HistorialClinicoResponse>> verHistorial(
            @PathVariable Long pacienteId,
            Authentication authentication) {

        // El Service se encargará de validar si el Paciente que hace la petición
        // es el mismo dueño del pacienteId, para evitar fugas de datos.
        List<HistorialClinicoResponse> historial = historialService.obtenerHistorial(pacienteId, authentication);
        return ResponseEntity.ok(historial);
    }
}