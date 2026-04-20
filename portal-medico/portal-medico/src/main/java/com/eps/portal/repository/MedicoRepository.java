package com.eps.portal.repository;

import com.eps.portal.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicoRepository extends JpaRepository<Medico, Long> {
    // Usado por MedicoService para filtrar el directorio por especialidad
    List<Medico> findByEspecialidadId(Integer especialidadId);
}