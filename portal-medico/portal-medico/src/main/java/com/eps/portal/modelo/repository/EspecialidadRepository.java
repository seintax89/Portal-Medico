package com.eps.portal.modelo.repository;

import com.eps.portal.modelo.entity.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Integer> {
    // JpaRepository ya incluye findAll() para listar el catÃ¡logo
}
