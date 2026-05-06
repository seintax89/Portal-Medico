package com.eps.portal.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "medicamentos")
@Data
@NoArgsConstructor
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_generico", nullable = false, length = 150)
    private String nombreGenerico;

    @Column(length = 100)
    private String concentracion;

    @Column(name = "forma_farmaceutica", length = 100)
    private String formaFarmaceutica;
}