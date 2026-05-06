package com.eps.portal.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "pacientes")
@Data
@NoArgsConstructor
public class Paciente {

    @Id
    private Long usuarioId;  // mismo nombre que la columna PK en la tabla

    @OneToOne
    @MapsId                          // ← esto es lo clave
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(name = "tipo_documento", nullable = false, length = 20)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, unique = true, length = 50)
    private String numeroDocumento;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "tipo_paciente", nullable = false, length = 20)
    private String tipoPaciente; // NINO, ADULTO, ADULTO_MAYOR
}