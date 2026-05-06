package com.eps.portal.repository;

import com.eps.portal.entity.OrdenEspecialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenEspecialidadRepository extends JpaRepository<OrdenEspecialidad, Long> {
    List<OrdenEspecialidad> findByPacienteUsuarioIdAndUsadaFalse(Long pacienteId);
    List<OrdenEspecialidad> findByPacienteUsuarioEmailAndUsadaFalse(String email);
}
