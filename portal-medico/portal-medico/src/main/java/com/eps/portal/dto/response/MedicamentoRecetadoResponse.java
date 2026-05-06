package com.eps.portal.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MedicamentoRecetadoResponse {
    private String medicamentoNombre;
    private String dosis;
    private String frecuencia;
    private Integer duracionDias;
    private String medicoNombre;
    private LocalDateTime fechaReceta;
}
