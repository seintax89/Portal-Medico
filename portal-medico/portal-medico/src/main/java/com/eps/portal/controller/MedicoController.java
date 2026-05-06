package com.eps.portal.controller;

import com.eps.portal.dto.response.MedicoResponse;
import com.eps.portal.service.MedicoService; // Deberás crear este servicio
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

    // Cualquier usuario logueado (Médico o Paciente) puede ver el directorio
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MedicoResponse>> listarMedicos(
            // Parámetro opcional para filtrar: /api/medicos?especialidadId=2
            @RequestParam(required = false) Integer especialidadId) {

        List<MedicoResponse> medicos = medicoService.obtenerDirectorio(especialidadId);
        return ResponseEntity.ok(medicos);
    }
}