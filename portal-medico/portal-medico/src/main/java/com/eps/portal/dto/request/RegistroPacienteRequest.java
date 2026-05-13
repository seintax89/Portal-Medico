package com.eps.portal.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RegistroPacienteRequest {
    @NotBlank @Email String email;
    @NotBlank @Size(min = 6) String password;
    @NotBlank String nombres;
    @NotBlank String apellidos;
    @NotBlank String tipoDocumento;
    @NotBlank String numeroDocumento;
    @NotNull LocalDate fechaNacimiento;
}