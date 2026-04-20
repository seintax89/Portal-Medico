package com.eps.portal.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CitaResponse {
    private Long idCita;
    private String nombrePaciente;
    private String nombreMedico;
    private String especialidad;
    private LocalDateTime fechaHora;
    private String estado;
}