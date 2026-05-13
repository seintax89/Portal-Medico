package com.eps.portal.modelo.service;

import com.eps.portal.modelo.dto.request.FormulaMedicaRequest;
import com.eps.portal.modelo.dto.response.MensajeResponse;
import com.eps.portal.modelo.entity.FormulaMedica;
import com.eps.portal.modelo.entity.HistorialClinico;
import com.eps.portal.modelo.entity.Medicamento;
import com.eps.portal.modelo.entity.Usuario;
import com.eps.portal.modelo.repository.FormulaMedicaRepository;
import com.eps.portal.modelo.repository.HistorialClinicoRepository;
import com.eps.portal.modelo.repository.MedicamentoRepository;
import com.eps.portal.modelo.repository.UsuarioRepository;
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

        // 1. Validar al mÃ©dico logueado
        Usuario usuarioMedico = usuarioRepository.findByEmail(emailMedico)
                .orElseThrow(() -> new RuntimeException("MÃ©dico no encontrado en sesiÃ³n."));

        // 2. Validar que el historial clÃ­nico exista
        HistorialClinico historial = historialRepository.findById(request.getHistorialId())
                .orElseThrow(() -> new RuntimeException("El historial clÃ­nico especificado no existe."));

        // REGLA DE NEGOCIO: Validar que el mÃ©dico que receta es el mismo que atendiÃ³ la cita
        if (!historial.getMedico().getUsuarioId().equals(usuarioMedico.getId())) {
            throw new RuntimeException("Alerta de Seguridad: No tiene permisos para formular medicamentos en un historial clÃ­nico de otro mÃ©dico.");
        }

        // 3. Validar que el medicamento exista en el catÃ¡logo de la EPS
        Medicamento medicamento = medicamentoRepository.findById(request.getMedicamentoId())
                .orElseThrow(() -> new RuntimeException("El medicamento seleccionado no estÃ¡ en el catÃ¡logo."));

        // 4. Crear y guardar la prescripciÃ³n
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
