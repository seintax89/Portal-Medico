package com.eps.portal.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO para capturar las credenciales de inicio de sesión.
 */
@Data // Genera getters, setters, equals, canEqual, hashCode y toString automáticamente con Lombok
public class LoginRequest {

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Debe proporcionar un formato de correo válido")
    private String email;

    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;
}