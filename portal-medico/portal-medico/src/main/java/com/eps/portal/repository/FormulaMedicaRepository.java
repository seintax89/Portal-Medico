package com.eps.portal.repository;

import com.eps.portal.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {
    // Útil si en el futuro queremos ver qué medicinas le mandaron en una consulta específica
    List<FormulaMedica> findByHistorialClinicoId(Long historialClinicoId);

    // Obtener medicinas del paciente
    List<FormulaMedica> findByHistorialClinicoPacienteUsuarioEmailOrderByHistorialClinicoFechaRegistroDesc(String email);
}