package com.eps.portal.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ActualizarPerfilRequest {
    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombres;

    @NotBlank(message = "El apellido no puede estar vacío")
    private String apellidos;

    @NotBlank(message = "El número de documento no puede estar vacío")
    private String numeroDocumento;

    private LocalDate fechaNacimiento;
}
