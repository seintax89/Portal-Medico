package com.eps.portal.modelo.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MedicoResponse {
    private Long id;
    private String nombreCompleto; // Uniremos nombre y apellido en el backend
    private String especialidad;
    private String registroMedico;
}
