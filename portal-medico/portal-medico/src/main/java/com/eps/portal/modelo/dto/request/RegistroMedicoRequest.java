package com.eps.portal.modelo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegistroMedicoRequest {
    @NotBlank @Email String email;
    @NotBlank String password;
    @NotBlank String nombres;
    @NotBlank String apellidos;
    @NotBlank String registroMedico;
    @NotNull Integer especialidadId; // Solo pedimos el ID, no el objeto completo
}
