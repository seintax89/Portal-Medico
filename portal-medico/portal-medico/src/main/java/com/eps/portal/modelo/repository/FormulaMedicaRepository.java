package com.eps.portal.modelo.repository;

import com.eps.portal.modelo.entity.FormulaMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FormulaMedicaRepository extends JpaRepository<FormulaMedica, Long> {
    // Ãštil si en el futuro queremos ver quÃ© medicinas le mandaron en una consulta especÃ­fica
    List<FormulaMedica> findByHistorialClinicoId(Long historialClinicoId);

    // Obtener medicinas del paciente
    List<FormulaMedica> findByHistorialClinicoPacienteUsuarioEmailOrderByHistorialClinicoFechaRegistroDesc(String email);
}

