package com.eps.portal.modelo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FormulaMedicaRequest {
    @NotNull private Long historialId;
    @NotNull private Long medicamentoId;

    @NotBlank(message = "Debe indicar la dosis (ej. 500mg)")
    private String dosis;

    @NotBlank(message = "Debe indicar la frecuencia (ej. Cada 8 horas)")
    private String frecuencia;

    @NotNull(message = "Debe indicar los dÃ­as de tratamiento")
    private Integer duracionDias;
}
