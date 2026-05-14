package com.eps.portal.modelo.repository;

import com.eps.portal.modelo.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Fundamental para el login: buscar al usuario por su correo
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);
}
