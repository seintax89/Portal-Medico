package com.eps.portal.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "formulas_medicas")
@Data
@NoArgsConstructor
public class FormulaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con el Historial donde se recetó
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "historial_id", nullable = false)
    private HistorialClinico historialClinico;

    // Relación con el Medicamento recetado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Column(nullable = false, length = 100)
    private String dosis;

    @Column(nullable = false, length = 100)
    private String frecuencia;

    @Column(name = "duracion_dias", nullable = false)
    private Integer duracionDias;
}