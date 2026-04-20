package com.eps.portal.config;

import com.eps.portal.entity.Role;
import com.eps.portal.entity.Usuario;
import com.eps.portal.repository.RoleRepository;
import com.eps.portal.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // 1. Crear ROLE_ADMIN si no existe
        Role roleAdmin = roleRepository.findByNombre("ROLE_ADMIN")
                .orElseGet(() -> {
                    Role nuevoRol = new Role();
                    nuevoRol.setNombre("ROLE_ADMIN");
                    return roleRepository.save(nuevoRol);
                });

        // 2. Crear o reparar el usuario admin
        Usuario admin = usuarioRepository.findByEmail("admin@eps.com")
                .orElseGet(Usuario::new);

        admin.setEmail("admin@eps.com");
        admin.setPassword(passwordEncoder.encode("Admin123*"));
        admin.setRol(roleAdmin);
        admin.setActivo(true);

        usuarioRepository.save(admin);

        System.out.println("Admin listo: admin@eps.com / Admin123*");
    }
}