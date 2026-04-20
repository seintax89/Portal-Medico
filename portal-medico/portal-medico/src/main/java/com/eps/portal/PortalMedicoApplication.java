package com.eps.portal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
public class PortalMedicoApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortalMedicoApplication.class, args);
	}

	@Bean
	public CommandLineRunner probarConexion(DataSource dataSource) {
		return args -> {
			try (Connection conn = dataSource.getConnection()) {
				System.out.println("\n=======================================================");
				System.out.println("✅ ¡ÉXITO! CONEXIÓN ESTABLECIDA CON SUPABASE POSTGRESQL");
				System.out.println("=======================================================\n");
			} catch (Exception e) {
				System.out.println("\n❌ ERROR AL CONECTAR CON LA BASE DE DATOS: " + e.getMessage() + "\n");
			}
		};
	}


}
