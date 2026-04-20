package com.eps.portal.controller;

import com.eps.portal.entity.Especialidad;
import com.eps.portal.repository.EspecialidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/especialidades")
@RequiredArgsConstructor
public class EspecialidadController {

    // Nota: Como es un catálogo simple (solo lectura), es aceptable usar el Repository
    // directamente aquí sin pasar por un Service.
    private final EspecialidadRepository especialidadRepository;

    @GetMapping
    public ResponseEntity<List<Especialidad>> listarEspecialidades() {
        return ResponseEntity.ok(especialidadRepository.findAll());
    }
}