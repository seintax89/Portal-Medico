package com.eps.portal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HistorialClinicoRequest {
    @NotNull(message = "El ID de la cita es obligatorio")
    private Long citaId;

    @NotBlank(message = "El diagnóstico no puede estar vacío")
    private String diagnostico;

    private String observaciones; // Opcional, por eso no lleva @NotBlank
}