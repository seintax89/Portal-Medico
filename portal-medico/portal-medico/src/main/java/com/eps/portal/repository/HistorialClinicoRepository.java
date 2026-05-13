package com.eps.portal.repository;

import com.eps.portal.entity.HistorialClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface  HistorialClinicoRepository extends JpaRepository<HistorialClinico, Long> {
    // Regla de Negocio: Verifica que una cita no tenga más de una evolución
    boolean existsByCitaId(Long citaId);

    // Para listar el historial del paciente, ordenado de la visita más reciente a la más antigua
    List<HistorialClinico> findByPacienteUsuarioIdOrderByFechaRegistroDesc(Long pacienteId);
}