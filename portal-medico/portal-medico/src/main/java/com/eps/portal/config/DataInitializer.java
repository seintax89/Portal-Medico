package com.eps.portal.config;

import com.eps.portal.entity.Especialidad;
import com.eps.portal.entity.Role;
import com.eps.portal.entity.Usuario;
<<<<<<< HEAD
import com.eps.portal.entity.OrdenEspecialidad;
import com.eps.portal.entity.Paciente;
import com.eps.portal.repository.EspecialidadRepository;
import com.eps.portal.repository.OrdenEspecialidadRepository;
import com.eps.portal.repository.PacienteRepository;
=======
import com.eps.portal.repository.EspecialidadRepository;
>>>>>>> 8d309450ee6cafb15dbbb0fd50fd29c0420e8f99
import com.eps.portal.repository.RoleRepository;
import com.eps.portal.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

<<<<<<< HEAD
import java.time.LocalDateTime;
=======
>>>>>>> 8d309450ee6cafb15dbbb0fd50fd29c0420e8f99
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UsuarioRepository usuarioRepository;
    private final EspecialidadRepository especialidadRepository;
<<<<<<< HEAD
    private final PacienteRepository pacienteRepository;
    private final OrdenEspecialidadRepository ordenEspecialidadRepository;
=======
>>>>>>> 8d309450ee6cafb15dbbb0fd50fd29c0420e8f99
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
<<<<<<< HEAD
        
        // 4. Crear orden de prueba si hay pacientes
        if (pacienteRepository.count() > 0 && ordenEspecialidadRepository.count() == 0) {
            Paciente p = pacienteRepository.findAll().get(0);
            Especialidad cardiologia = especialidadRepository.findAll().stream()
                    .filter(e -> e.getNombre().equalsIgnoreCase("Cardiología"))
                    .findFirst().orElse(null);
            
            if (cardiologia != null) {
                OrdenEspecialidad orden = new OrdenEspecialidad();
                orden.setPaciente(p);
                orden.setEspecialidad(cardiologia);
                orden.setFechaEmision(LocalDateTime.now());
                orden.setUsada(false);
                ordenEspecialidadRepository.save(orden);
                System.out.println("Orden de Cardiología creada para el paciente: " + p.getNombres());
            }
        }
=======
>>>>>>> 8d309450ee6cafb15dbbb0fd50fd29c0420e8f99
    }
}