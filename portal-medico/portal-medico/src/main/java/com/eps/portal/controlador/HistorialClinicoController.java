package com.eps.portal.controlador;

import com.eps.portal.modelo.dto.request.HistorialClinicoRequest;
import com.eps.portal.modelo.dto.response.HistorialClinicoResponse;
import com.eps.portal.modelo.dto.response.MensajeResponse;
import com.eps.portal.modelo.service.HistorialClinicoService; // DeberÃ¡s crear este servicio
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

    // Solo un mÃ©dico puede redactar una evoluciÃ³n clÃ­nica
    @PostMapping
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<MensajeResponse> registrarAtencion(
            @Valid @RequestBody HistorialClinicoRequest request,
            Authentication authentication) {

        String emailMedico = authentication.getName();
        historialService.registrarEvolucion(request, emailMedico);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MensajeResponse("EvoluciÃ³n clÃ­nica guardada exitosamente."));
    }

    // Un paciente puede ver su propio historial, un mÃ©dico puede ver el de sus pacientes
    @GetMapping("/paciente/{pacienteId}")
    @PreAuthorize("hasRole('PACIENTE') or hasRole('MEDICO')")
    public ResponseEntity<List<HistorialClinicoResponse>> verHistorial(
            @PathVariable Long pacienteId,
            Authentication authentication) {

        // El Service se encargarÃ¡ de validar si el Paciente que hace la peticiÃ³n
        // es el mismo dueÃ±o del pacienteId, para evitar fugas de datos.
        List<HistorialClinicoResponse> historial = historialService.obtenerHistorial(pacienteId, authentication);
        return ResponseEntity.ok(historial);
    }
}
