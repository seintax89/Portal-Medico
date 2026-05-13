package com.eps.portal.repository;

import com.eps.portal.entity.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Integer> {
    // JpaRepository ya incluye findAll() para listar el catálogo
}