package com.eps.portal.modelo.repository;

import com.eps.portal.modelo.entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
    // JpaRepository maneja el CRUD bÃ¡sico del catÃ¡logo de medicamentos
}
