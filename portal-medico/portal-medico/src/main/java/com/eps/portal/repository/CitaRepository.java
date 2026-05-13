package com.eps.portal.repository;

import com.eps.portal.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Regla de Negocio RN-03: ¿El médico ya tiene una cita exacta a esa hora?
    boolean existsByMedicoUsuarioIdAndFechaHora(Long medicoId, LocalDateTime fechaHora);

    // Para ver la agenda del médico en un rango de fechas
    @Query("SELECT c FROM Cita c WHERE c.medico.usuarioId = :medicoId AND c.fechaHora BETWEEN :inicio AND :fin AND c.estado = 'PROGRAMADA'")
    List<Cita> findCitasProgramadasByMedicoRango(
            @Param("medicoId") Long medicoId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

    // Obtener todas las citas de un paciente ordenadas por fecha (más recientes primero)
    List<Cita> findByPacienteUsuarioEmailOrderByFechaHoraDesc(String email);
}
