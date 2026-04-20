package com.eps.portal.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class PacienteResponse {
    private Long id;
    private String email;
    private String nombres;
    private String apellidos;
    private String numeroDocumento;
    private LocalDate fechaNacimiento;
    private String tipoPaciente; // "NINO", "ADULTO", "ADULTO_MAYOR"
}