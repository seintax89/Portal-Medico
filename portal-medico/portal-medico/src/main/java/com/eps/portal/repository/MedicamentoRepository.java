package com.eps.portal.repository;

import com.eps.portal.entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
    // JpaRepository maneja el CRUD básico del catálogo de medicamentos
}