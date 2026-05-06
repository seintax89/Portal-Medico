package com.eps.portal.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CitaRequest {
    @NotNull(message = "El ID del paciente es obligatorio")
    private Long pacienteId;

<<<<<<< HEAD
    @NotNull(message = "El ID de la especialidad es obligatorio")
    private Long especialidadId;
=======
    @NotNull(message = "El ID del médico es obligatorio")
    private Long medicoId;
>>>>>>> 8d309450ee6cafb15dbbb0fd50fd29c0420e8f99

    @NotNull(message = "La fecha y hora son obligatorias")
    @Future(message = "La cita debe ser en una fecha futura") // RN-02 implícita
    private LocalDateTime fechaHora;
}