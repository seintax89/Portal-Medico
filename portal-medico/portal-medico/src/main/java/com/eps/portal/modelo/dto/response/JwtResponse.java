package com.eps.portal.modelo.dto.response;

import lombok.Data;
import java.util.List;

/**
 * Objeto enviado al cliente tras una autenticaciÃ³n exitosa.
 */
@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String rol;

    // Constructor para inicializar los datos bÃ¡sicos tras el login
    public JwtResponse(String accessToken, Long id, String email, String rol) {
        this.token = accessToken;
        this.id = id;
        this.email = email;
        this.rol = rol;
    }
}
