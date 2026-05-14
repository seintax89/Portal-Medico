package com.eps.portal.modelo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActualizarPerfilRequest {
    @NotBlank(message = "El correo no puede estar vacÃ­o")
    @Email(message = "Formato de correo invÃ¡lido")
    private String email;

    // La contraseÃ±a es opcional, si viene vacÃ­a no se actualiza
    private String password;
}

