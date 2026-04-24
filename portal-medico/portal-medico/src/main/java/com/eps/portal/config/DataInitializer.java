package com.eps.portal.config;

import com.eps.portal.entity.Especialidad;
import com.eps.portal.entity.Role;
import com.eps.portal.entity.Usuario;
import com.eps.portal.repository.EspecialidadRepository;
import com.eps.portal.repository.RoleRepository;
import com.eps.portal.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UsuarioRepository usuarioRepository;
    private final EspecialidadRepository especialidadRepository;
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

        // Crear ROLE_MEDICO si no existe
        roleRepository.findByNombre("ROLE_MEDICO")
                .orElseGet(() -> {
                    Role nuevoRol = new Role();
                    nuevoRol.setNombre("ROLE_MEDICO");
                    return roleRepository.save(nuevoRol);
                });

        // Crear ROLE_PACIENTE si no existe
        roleRepository.findByNombre("ROLE_PACIENTE")
                .orElseGet(() -> {
                    Role nuevoRol = new Role();
                    nuevoRol.setNombre("ROLE_PACIENTE");
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

        // 3. Crear especialidades por defecto si la tabla está vacía
        if (especialidadRepository.count() == 0) {
            List<String> especialidadesNombres = List.of(
                    "Medicina General",
                    "Cardiología",
                    "Pediatría",
                    "Dermatología",
                    "Ginecología");

            for (String nombre : especialidadesNombres) {
                Especialidad esp = new Especialidad();
                esp.setNombre(nombre);
                especialidadRepository.save(esp);
            }
            System.out.println("Especialidades por defecto creadas.");
        }
    }
}