package com.eps.portal.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActualizarPerfilRequest {
    @NotBlank(message = "El correo no puede estar vacío")
    @Email(message = "Formato de correo inválido")
    private String email;

    // La contraseña es opcional, si viene vacía no se actualiza
    private String password;
}
