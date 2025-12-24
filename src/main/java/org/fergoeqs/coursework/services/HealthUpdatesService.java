package org.fergoeqs.coursework.services;

import org.fergoeqs.coursework.dto.HealthUpdateDTO;
import org.fergoeqs.coursework.exception.ResourceNotFoundException;
import org.fergoeqs.coursework.models.HealthUpdate;
import org.fergoeqs.coursework.models.Pet;
import org.fergoeqs.coursework.repositories.HealthUpdatesRepository;
import org.fergoeqs.coursework.utils.Mappers.HealthUpdateMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Transactional(readOnly = true)
@Service
public class HealthUpdatesService {
    private final HealthUpdatesRepository healthUpdatesRepository;
    private final HealthUpdateMapper healthUpdateMapper;
    private final PetsService petsService;

    public HealthUpdatesService(HealthUpdatesRepository healthUpdatesRepository, HealthUpdateMapper healthUpdateMapper,
                                PetsService petsService) {
        this.healthUpdatesRepository = healthUpdatesRepository;
        this.healthUpdateMapper = healthUpdateMapper;
        this.petsService = petsService;
    }

    public List<HealthUpdate> findAllByPet(Long petId) {
        return healthUpdatesRepository.findAllByPetId(petId);
    }

    public HealthUpdate findFirstByPet(Long petId) {
        return healthUpdatesRepository.findFirstByPetIdOrderByIdDesc(petId);
    }

    public HealthUpdate findById(Long id) {
        return healthUpdatesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Health update not found with id: " + id));
    }

    @Transactional
    public HealthUpdate save(HealthUpdateDTO healthUpdateDTO) {
        HealthUpdate healthUpdate = healthUpdateMapper.fromDTO(healthUpdateDTO);
        healthUpdate.setPet(petsService.findPetById(healthUpdateDTO.pet()));
        healthUpdate.setDate(LocalDateTime.now());
        return healthUpdatesRepository.save(healthUpdate);
    }

    @Transactional
    public HealthUpdate saveWithAppointment(Pet pet, String symptoms) {
        HealthUpdate healthUpdate = new HealthUpdate();
        healthUpdate.setPet(pet);
        healthUpdate.setSymptoms(symptoms);
        healthUpdate.setDynamics(false);
//        healthUpdate.setDate(LocalDateTime.now().truncatedTo(ChronoUnit.HOURS));
        healthUpdate.setDate(LocalDateTime.now());
        return healthUpdatesRepository.save(healthUpdate);
    }
}
