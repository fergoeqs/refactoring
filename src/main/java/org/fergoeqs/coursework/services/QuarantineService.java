package org.fergoeqs.coursework.services;

import org.fergoeqs.coursework.dto.QuarantineDTO;
import org.fergoeqs.coursework.exception.ResourceNotFoundException;
import org.fergoeqs.coursework.exception.ValidationException;
import org.fergoeqs.coursework.models.AppUser;
import org.fergoeqs.coursework.models.Quarantine;
import org.fergoeqs.coursework.models.enums.QuarantineStatus;
import org.fergoeqs.coursework.repositories.QuarantineRepository;
import org.fergoeqs.coursework.utils.Mappers.QuarantineMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class QuarantineService {
    private final QuarantineRepository quarantineRepository;
    private final SectorsService sectorsService;
    private final PetsService petsService;
    private final NotificationService notificationService;
    private final QuarantineMapper quarantineMapper;

    public QuarantineService(QuarantineRepository quarantineRepository, SectorsService sectorsService, PetsService petsService,
                             NotificationService notificationService, QuarantineMapper quarantineMapper) {
        this.quarantineRepository = quarantineRepository;
        this.sectorsService = sectorsService;
        this.petsService = petsService;
        this.notificationService = notificationService;
        this.quarantineMapper = quarantineMapper;
    }

    public Quarantine findQuarantineById(Long id) {
        return quarantineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quarantine not found with id: " + id));
    }

    public List<Quarantine> findQuarantinesBySector(Long sectorId) {
        return quarantineRepository.findQuarantinesBySectorId(sectorId);
    }

    public Page<Quarantine> findQuarantinesBySectorAndStatus(Long sectorId, QuarantineStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return quarantineRepository.findQuarantinesBySectorIdAndStatus(sectorId, status, pageable);
    }

    public List<Quarantine> findQuarantinesByPet(Long petId) {
        return quarantineRepository.findQuarantinesByPetId(petId);
    }

    public List<Quarantine> findAllQuarantines() {
        return quarantineRepository.findAll();
    }

    public List<String> findDistinctReason(Long sectorId) {
        return quarantineRepository.findDistinctReasonsByCurrentStatus(sectorId);
    }

    @Transactional
    public Quarantine save(QuarantineDTO quarantineDTO, AppUser appUser) {
        Quarantine quarantine = quarantineMapper.fromDTO(quarantineDTO);
        if (quarantine.getStartDate().isAfter(quarantine.getEndDate())) {
            throw new ValidationException("Start date must be before end date");
        }
        quarantine.setVet(appUser);
        quarantine.setStatus(QuarantineStatus.CURRENT);
        return quarantineRepository.save(setRelativeFields(quarantine, quarantineDTO));
    }

    @Transactional
    public void deleteQuarantineById(Long id) {
        quarantineRepository.deleteById(id);
    }

    @Scheduled(cron = "0 * * * * *")
    public void updateExpiredQuarantines() { //не самое адекватное время, но для теста пойдет
        LocalDateTime now = LocalDateTime.now();

        List<Quarantine> quarantines = quarantineRepository.findByEndDateBeforeAndStatusNot(now, QuarantineStatus.DONE);

        for (Quarantine quarantine : quarantines) {
            quarantine.setStatus(QuarantineStatus.DONE);
            quarantineRepository.save(quarantine);
            String petName = quarantine.getPet().getName();
            String message = "Quarantine for pet " + petName + " has been completed!";
            notificationService.sendNotification(quarantine.getVet().getId(), message, quarantine.getVet().getEmail());
        }

    }

    private Quarantine setRelativeFields(Quarantine quarantine, QuarantineDTO quarantineDTO) {
        quarantine.setSector(sectorsService.findSectorById(quarantineDTO.sector()));
        quarantine.setPet(petsService.findPetById(quarantineDTO.pet()));
        return quarantine;
    }
}
