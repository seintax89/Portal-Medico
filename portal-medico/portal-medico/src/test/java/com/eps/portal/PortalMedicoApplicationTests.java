package com.eps.portal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifica que el contexto de Spring Boot carga correctamente con H2.
 * Usa el perfil "test" para no conectar a la BD de producción (Supabase).
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Prueba de Contexto - Spring Boot Application")
class PortalMedicoApplicationTests {

	@Test
	@DisplayName("El contexto de la aplicación carga sin errores")
	void contextLoads() {
		// Si este test pasa, la configuración completa de Spring (beans, seguridad, JPA) es válida
	}

}
