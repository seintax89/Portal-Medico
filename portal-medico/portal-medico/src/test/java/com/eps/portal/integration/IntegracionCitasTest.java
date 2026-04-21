package com.eps.portal.integration;

import com.eps.portal.dto.request.CitaRequest;
import com.eps.portal.dto.request.HistorialClinicoRequest;
import com.eps.portal.dto.request.LoginRequest;
import com.eps.portal.entity.*;
import com.eps.portal.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * =====================================================================
 * PRUEBAS DE INTEGRACIÓN — Flujo Completo de Citas + Historial
 * Tipo: @SpringBootTest con H2 en memoria
 * Perfil: "test" → usa application-test.yml
 *
 * Flujo de prueba (simula el ciclo de vida de una consulta médica):
 *   1. Login de médico con datos precargados
 *   2. Login de paciente con datos precargados
 *   3. El médico agenda una cita para el paciente
 *   4. PRUEBA DE ESTADO: La cita recién creada está en estado "PROGRAMADA"
 *   5. El médico registra la evolución clínica (atención)
 *   6. PRUEBA DE ESTADO: La cita pasa de "PROGRAMADA" a "COMPLETADA"
 *   7. PRUEBA DE ESTADO: No se puede registrar 2da evolución en la misma cita
 * =====================================================================
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("Pruebas de Integración + Estado - Flujo Completo de Citas")
class IntegracionCitasTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CitaRepository citaRepository;
    @Autowired HistorialClinicoRepository historialRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired PacienteRepository pacienteRepository;
    @Autowired MedicoRepository medicoRepository;
    @Autowired EspecialidadRepository especialidadRepository;
    @Autowired PasswordEncoder passwordEncoder;

    // Datos compartidos entre tests (se inicializan en @BeforeAll)
    private static String tokenMedico;
    private static String tokenPaciente;
    private static Long idCita;
    private static Long idPaciente;
    private static Long idMedico;
    private static boolean datosInicializados = false;

    /**
     * Inicializa los datos de prueba UNA SOLA VEZ para todos los tests.
     * Verifica que no existan antes de crear (idempotente).
     */
    @BeforeEach
    void inicializarDatos() {
        if (datosInicializados) return;

        // 1. Roles
        Role rolPaciente = roleRepository.findByNombre("ROLE_PACIENTE").orElseGet(() -> {
            Role r = new Role(); r.setNombre("ROLE_PACIENTE"); return roleRepository.save(r);
        });
        Role rolMedico = roleRepository.findByNombre("ROLE_MEDICO").orElseGet(() -> {
            Role r = new Role(); r.setNombre("ROLE_MEDICO"); return roleRepository.save(r);
        });

        // 2. Especialidad
        Especialidad espGeneral = especialidadRepository.findAll().stream()
                .filter(e -> "MEDICINA_GENERAL".equals(e.getNombre()))
                .findFirst()
                .orElseGet(() -> {
                    Especialidad e = new Especialidad();
                    e.setNombre("MEDICINA_GENERAL");
                    return especialidadRepository.save(e);
                });

        // 3. Usuario + Paciente (solo si no existen)
        if (usuarioRepository.findByEmail("intcitas.paciente@test.com").isEmpty()) {
            Usuario usuPac = new Usuario();
            usuPac.setEmail("intcitas.paciente@test.com");
            usuPac.setPassword(passwordEncoder.encode("Password123*"));
            usuPac.setRol(rolPaciente);
            usuPac = usuarioRepository.saveAndFlush(usuPac);
            // Recargamos desde BD para evitar 'detached entity' al hacer @MapsId
            final Long pacUserId = usuPac.getId();
            usuPac = usuarioRepository.findById(pacUserId).orElseThrow();

            Paciente p = new Paciente();
            p.setUsuario(usuPac);
            p.setNombres("Paciente");
            p.setApellidos("Integración");
            p.setTipoDocumento("CC");
            p.setNumeroDocumento("CITA-INT-001");
            p.setFechaNacimiento(LocalDate.of(1988, 3, 10));
            p.setTipoPaciente("ADULTO");
            p = pacienteRepository.saveAndFlush(p);
            idPaciente = p.getUsuarioId();
        } else {
            idPaciente = usuarioRepository.findByEmail("intcitas.paciente@test.com")
                    .map(Usuario::getId).orElse(null);
        }

        // 4. Usuario + Médico (solo si no existen)
        if (usuarioRepository.findByEmail("intcitas.medico@test.com").isEmpty()) {
            Usuario usuMed = new Usuario();
            usuMed.setEmail("intcitas.medico@test.com");
            usuMed.setPassword(passwordEncoder.encode("Password123*"));
            usuMed.setRol(rolMedico);
            usuMed = usuarioRepository.saveAndFlush(usuMed);
            // Recargamos desde BD para evitar 'detached entity' al hacer @MapsId
            final Long medUserId = usuMed.getId();
            usuMed = usuarioRepository.findById(medUserId).orElseThrow();

            Medico m = new Medico();
            m.setUsuario(usuMed);
            m.setNombres("Médico");
            m.setApellidos("Integración");
            m.setRegistroMedico("RM-INT-001");
            m.setEspecialidad(espGeneral);
            m = medicoRepository.saveAndFlush(m);
            idMedico = m.getUsuarioId();
        } else {
            idMedico = usuarioRepository.findByEmail("intcitas.medico@test.com")
                    .map(Usuario::getId).orElse(null);
        }

        datosInicializados = true;
    }

    // ──────────────────────────────────────────────────────────────
    // TEST 1 y 2: Login
    // ──────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("INT-08: El médico hace login → obtiene token JWT válido")
    void paso1_loginMedico_exitoso() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("intcitas.medico@test.com");
        req.setPassword("Password123*");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ROLE_MEDICO"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        tokenMedico = json.get("token").asText();
        assertThat(tokenMedico).isNotBlank();
    }

    @Test
    @Order(2)
    @DisplayName("INT-09: El paciente hace login → obtiene token JWT válido")
    void paso2_loginPaciente_exitoso() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("intcitas.paciente@test.com");
        req.setPassword("Password123*");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ROLE_PACIENTE"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        tokenPaciente = json.get("token").asText();
        assertThat(tokenPaciente).isNotBlank();
    }

    // ──────────────────────────────────────────────────────────────
    // TEST 3: Agendar cita — verificación de estado inicial
    // ──────────────────────────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("INT-10: El médico agenda una cita → 201 CREATED + ESTADO inicial = PROGRAMADA")
    void paso3_agendarCita_estadoInicial_PROGRAMADA() throws Exception {
        Assumptions.assumeTrue(tokenMedico != null, "Token médico no disponible");
        Assumptions.assumeTrue(idPaciente != null && idMedico != null, "IDs no disponibles");

        CitaRequest req = new CitaRequest();
        req.setPacienteId(idPaciente);
        req.setMedicoId(idMedico);
        req.setFechaHora(proximoLunesA(9));

        MvcResult result = mockMvc.perform(post("/api/citas")
                        .header("Authorization", "Bearer " + tokenMedico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());

        // PRUEBA DE ESTADO 1: Estado inicial debe ser PROGRAMADA
        assertThat(json.get("estado").asText())
                .as("La cita recién creada debe tener estado PROGRAMADA")
                .isEqualTo("PROGRAMADA");

        idCita = json.get("idCita").asLong();

        // Verificación directa en BD
        Cita citaEnBD = citaRepository.findById(idCita).orElseThrow();
        assertThat(citaEnBD.getEstado()).isEqualTo("PROGRAMADA");
        assertThat(citaEnBD.getPaciente().getUsuarioId()).isEqualTo(idPaciente);
    }

    // ──────────────────────────────────────────────────────────────
    // TEST 4: Solapamiento
    // ──────────────────────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("INT-11: Segunda cita en misma hora → 400 BAD REQUEST (solapamiento)")
    void paso4_solapamiento_retorna400() throws Exception {
        Assumptions.assumeTrue(tokenMedico != null && idCita != null, "Prerequisitos no disponibles");

        Cita citaExistente = citaRepository.findById(idCita).orElseThrow();

        CitaRequest req = new CitaRequest();
        req.setPacienteId(idPaciente);
        req.setMedicoId(idMedico);
        req.setFechaHora(citaExistente.getFechaHora()); // misma hora

        mockMvc.perform(post("/api/citas")
                        .header("Authorization", "Bearer " + tokenMedico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ya tiene una cita asignada")));
    }

    // ──────────────────────────────────────────────────────────────
    // TEST 5: Registrar evolución → transición de estado
    // ──────────────────────────────────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("INT-12: Médico registra evolución → ESTADO: PROGRAMADA → COMPLETADA")
    void paso5_registrarEvolucion_citaCambiaACompletada() throws Exception {
        Assumptions.assumeTrue(tokenMedico != null && idCita != null, "Prerequisitos no disponibles");

        HistorialClinicoRequest req = new HistorialClinicoRequest();
        req.setCitaId(idCita);
        req.setDiagnostico("Hipertensión arterial leve. Paciente estable.");
        req.setObservaciones("Control en 30 días. Dieta baja en sodio.");

        mockMvc.perform(post("/api/historial")
                        .header("Authorization", "Bearer " + tokenMedico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Evolución clínica guardada exitosamente."));

        // PRUEBA DE ESTADO 2: La cita debe haber cambiado a COMPLETADA
        Cita citaActualizada = citaRepository.findById(idCita).orElseThrow();
        assertThat(citaActualizada.getEstado())
                .as("La cita debe cambiar a COMPLETADA tras registrar la evolución clínica")
                .isEqualTo("COMPLETADA");

        // Verificar que el historial fue persistido
        assertThat(historialRepository.existsByCitaId(idCita))
                .as("Debe existir un registro de historial para esta cita")
                .isTrue();
    }

    // ──────────────────────────────────────────────────────────────
    // TEST 6: Unicidad de evolución (1 cita = 1 evolución)
    // ──────────────────────────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("INT-13: Segunda evolución en la misma cita → error (regla: 1 cita = 1 evolución)")
    void paso6_segundaEvolucion_lanzaError() throws Exception {
        Assumptions.assumeTrue(tokenMedico != null && idCita != null, "Prerequisitos no disponibles");

        HistorialClinicoRequest req = new HistorialClinicoRequest();
        req.setCitaId(idCita);
        req.setDiagnostico("Segundo diagnóstico (no debe permitirse)");

        // Debe fallar porque la cita ya fue atendida
        mockMvc.perform(post("/api/historial")
                        .header("Authorization", "Bearer " + tokenMedico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is5xxServerError());

        // La cita permanece en COMPLETADA (no hubo regresión de estado)
        Cita citaIntacta = citaRepository.findById(idCita).orElseThrow();
        assertThat(citaIntacta.getEstado())
                .as("La cita debe seguir en COMPLETADA después del intento fallido")
                .isEqualTo("COMPLETADA");
    }

    // ──────────────────────────────────────────────────────────────
    // TEST 7: Paciente ve su historial
    // ──────────────────────────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("INT-14: Paciente puede ver su historial clínico → 200 OK con datos")
    void paso7_pacienteVerHistorial_retornaDatos() throws Exception {
        Assumptions.assumeTrue(tokenPaciente != null && idPaciente != null, "Prerequisitos no disponibles");

        mockMvc.perform(get("/api/historial/paciente/" + idPaciente)
                        .header("Authorization", "Bearer " + tokenPaciente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].diagnostico").exists())
                .andExpect(jsonPath("$[0].doctorQueAtendio").value(org.hamcrest.Matchers.startsWith("Dr.")));
    }

    // ─── Helper ─────────────────────────────────────────────────────

    private LocalDateTime proximoLunesA(int hora) {
        LocalDateTime ahora = LocalDateTime.now().plusDays(1);
        while (ahora.getDayOfWeek().getValue() != 1) {
            ahora = ahora.plusDays(1);
        }
        return ahora.withHour(hora).withMinute(0).withSecond(0).withNano(0);
    }
}
