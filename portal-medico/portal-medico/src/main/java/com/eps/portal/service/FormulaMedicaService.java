package com.eps.portal.service;

import com.eps.portal.dto.request.FormulaMedicaRequest;
import com.eps.portal.dto.response.MensajeResponse;
import com.eps.portal.entity.FormulaMedica;
import com.eps.portal.entity.HistorialClinico;
import com.eps.portal.entity.Medicamento;
import com.eps.portal.entity.Usuario;
import com.eps.portal.repository.FormulaMedicaRepository;
import com.eps.portal.repository.HistorialClinicoRepository;
import com.eps.portal.repository.MedicamentoRepository;
import com.eps.portal.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FormulaMedicaService {

    private final FormulaMedicaRepository formulaRepository;
    private final HistorialClinicoRepository historialRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public MensajeResponse prescribirMedicamento(FormulaMedicaRequest request, String emailMedico) {

        // 1. Validar al médico logueado
        Usuario usuarioMedico = usuarioRepository.findByEmail(emailMedico)
                .orElseThrow(() -> new RuntimeException("Médico no encontrado en sesión."));

        // 2. Validar que el historial clínico exista
        HistorialClinico historial = historialRepository.findById(request.getHistorialId())
                .orElseThrow(() -> new RuntimeException("El historial clínico especificado no existe."));

        // REGLA DE NEGOCIO: Validar que el médico que receta es el mismo que atendió la cita
        if (!historial.getMedico().getUsuarioId().equals(usuarioMedico.getId())) {
            throw new RuntimeException("Alerta de Seguridad: No tiene permisos para formular medicamentos en un historial clínico de otro médico.");
        }

        // 3. Validar que el medicamento exista en el catálogo de la EPS
        Medicamento medicamento = medicamentoRepository.findById(request.getMedicamentoId())
                .orElseThrow(() -> new RuntimeException("El medicamento seleccionado no está en el catálogo."));

        // 4. Crear y guardar la prescripción
        FormulaMedica formula = new FormulaMedica();
        formula.setHistorialClinico(historial);
        formula.setMedicamento(medicamento);
        formula.setDosis(request.getDosis());
        formula.setFrecuencia(request.getFrecuencia());
        formula.setDuracionDias(request.getDuracionDias());

        formulaRepository.save(formula);

        return new MensajeResponse("Medicamento '" + medicamento.getNombreGenerico() + "' recetado exitosamente.");
    }
}