package com.eps.portal.modelo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para capturar las credenciales de inicio de sesiÃ³n.
 */
@Data // Genera getters, setters, equals, canEqual, hashCode y toString automÃ¡ticamente con Lombok
public class LoginRequest {

    @NotBlank(message = "El correo electrÃ³nico es obligatorio")
    @Email(message = "Debe proporcionar un formato de correo vÃ¡lido")
    private String email;

    @NotBlank(message = "La contraseÃ±a no puede estar vacÃ­a")
    private String password;
}
