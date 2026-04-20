package com.eps.portal.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class HistorialClinicoResponse {
    private Long idRegistro;
    private LocalDateTime fechaAtencion;
    private String doctorQueAtendio;
    private String especialidad;
    private String diagnostico;
    private String observaciones;
}