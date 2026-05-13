package com.eps.portal.modelo.repository;

import com.eps.portal.modelo.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    // Al extender de JpaRepository tenemos save() y findById() gratis.
}
