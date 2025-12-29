package org.fergoeqs.coursework.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.fergoeqs.coursework.models.enums.ProcedureType;

import java.time.LocalDateTime;

@Entity
@Table(name = "medical_procedure")
@Getter
@Setter

public class MedicalProcedure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message="Procedure type is required")
    private ProcedureType type;

    @NotNull(message = "Procedure name is required")
    private String name;

    @NotNull(message = "Procedure date is required")
    private LocalDateTime date;

    @Column(length = 2000)
    private String description;
    private String notes;

    @ManyToOne
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @ManyToOne
    @JoinColumn(name = "vet_id")
    private AppUser vet;

    @ManyToOne
    @JoinColumn(name = "anamnesis_id")
    private Anamnesis anamnesis;

    private String reportUrl;
}
