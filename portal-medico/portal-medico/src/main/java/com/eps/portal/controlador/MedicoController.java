package com.eps.portal.controlador;

import com.eps.portal.modelo.dto.response.MedicoResponse;
import com.eps.portal.modelo.service.MedicoService; // DeberÃ¡s crear este servicio
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicos")
@RequiredArgsConstructor
public class MedicoController {

    private final MedicoService medicoService;

    // Cualquier usuario logueado (MÃ©dico o Paciente) puede ver el directorio
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MedicoResponse>> listarMedicos(
            // ParÃ¡metro opcional para filtrar: /api/medicos?especialidadId=2
            @RequestParam(required = false) Integer especialidadId) {

        List<MedicoResponse> medicos = medicoService.obtenerDirectorio(especialidadId);
        return ResponseEntity.ok(medicos);
    }
}
