package com.eps.portal.modelo.repository;

import com.eps.portal.modelo.entity.HistorialClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface  HistorialClinicoRepository extends JpaRepository<HistorialClinico, Long> {
    // Regla de Negocio: Verifica que una cita no tenga mÃ¡s de una evoluciÃ³n
    boolean existsByCitaId(Long citaId);

    // Para listar el historial del paciente, ordenado de la visita mÃ¡s reciente a la mÃ¡s antigua
    List<HistorialClinico> findByPacienteUsuarioIdOrderByFechaRegistroDesc(Long pacienteId);
}
