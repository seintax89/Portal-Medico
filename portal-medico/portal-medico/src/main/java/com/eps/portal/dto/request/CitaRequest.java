package com.eps.portal.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CitaRequest {
    @NotNull(message = "El ID del paciente es obligatorio")
    private Long pacienteId;

    @NotNull(message = "El ID de la especialidad es obligatorio")
    private Long especialidadId;

    @NotNull(message = "La fecha y hora son obligatorias")
    @Future(message = "La cita debe ser en una fecha futura") // RN-02 implícita
    private LocalDateTime fechaHora;
}
