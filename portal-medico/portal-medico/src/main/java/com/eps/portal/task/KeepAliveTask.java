package com.eps.portal.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KeepAliveTask {

    private static final Logger logger = LoggerFactory.getLogger(KeepAliveTask.class);

    // Utilizamos la variable de entorno que Render nos da, si no existe usamos la URL por defecto de tu backend
    @Value("${RENDER_EXTERNAL_URL:https://portal-medico-uicj.onrender.com}")
    private String appUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // Se ejecuta cada 10 minutos (600000 ms) para evitar que Render suspenda la app (el límite es de 15 min de inactividad)
    @Scheduled(fixedRate = 600000)
    public void pingSelf() {
        try {
            // Utilizamos el endpoint de especialidades que ya es público en tu configuración de seguridad
            String pingUrl = appUrl + "/api/especialidades";
            logger.info("Enviando petición Keep-Alive a: {}", pingUrl);
            
            // Hacemos una petición GET a un endpoint público que también consulta la BD
            // Esto previene que la app se duerma en Render y que la conexión de base de datos en Supabase expire
            restTemplate.getForObject(pingUrl, String.class);
            
            logger.info("Petición Keep-Alive exitosa. El servidor sigue despierto.");
        } catch (Exception e) {
            logger.warn("Error durante la petición Keep-Alive: {}", e.getMessage());
        }
    }
}
