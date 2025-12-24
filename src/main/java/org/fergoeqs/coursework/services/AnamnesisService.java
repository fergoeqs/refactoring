package org.fergoeqs.coursework.services;

import org.fergoeqs.coursework.dto.AnamnesisDTO;
import org.fergoeqs.coursework.exception.ResourceNotFoundException;
import org.fergoeqs.coursework.models.Anamnesis;
import org.fergoeqs.coursework.repositories.AnamnesisRepository;
import org.fergoeqs.coursework.utils.Mappers.AnamnesisMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
@Transactional(readOnly = true)
@Service
public class AnamnesisService {
    private final AnamnesisRepository anamnesisRepository;
    private final PetsService petsService;
    private final AppointmentsService appointmentsService;
    private final AnamnesisMapper anamnesisMapper;

    public AnamnesisService(AnamnesisRepository anamnesisRepository, AnamnesisMapper anamnesisMapper,
                            PetsService petsService, AppointmentsService appointmentsService) {
        this.anamnesisRepository = anamnesisRepository;
        this.petsService = petsService;
        this.appointmentsService = appointmentsService;
        this.anamnesisMapper = anamnesisMapper;
    }

    public Anamnesis findAnamnesisById(Long id) {
        return anamnesisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Anamnesis not found with id: " + id));
    }

    public List<Anamnesis> findAllAnamnesesByPet(Long petId) {
        return anamnesisRepository.findByPetId(petId);
    }

    @Transactional
    public Anamnesis saveAnamnesis(AnamnesisDTO anamnesisDTO) {
        Anamnesis anamnesis = anamnesisMapper.fromDTO(anamnesisDTO);
        anamnesis.setPet(petsService.findPetById(anamnesisDTO.pet()));
        anamnesis.setAppointment(appointmentsService.findById(anamnesisDTO.appointment()));
        anamnesis.setDate(LocalDate.now());
        return anamnesisRepository.save(anamnesis);
    }

    @Transactional
    public void deleteAnamnesis(Long id) {
        anamnesisRepository.deleteById(id);
    }
}
