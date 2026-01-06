package org.fergoeqs.coursework.services;

import org.fergoeqs.coursework.dto.MedicalProcedureDTO;
import org.fergoeqs.coursework.exception.ResourceNotFoundException;
import org.fergoeqs.coursework.models.MedicalProcedure;
import org.fergoeqs.coursework.repositories.MedicalProcedureRepository;
import org.fergoeqs.coursework.utils.Mappers.MedicalProcedureMapper;
import org.fergoeqs.coursework.utils.ReportGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MedicalProcedureService {
    private final MedicalProcedureRepository medicalProcedureRepository;
    private final AnamnesisService anamnesisService;
    private final PetsService petsService;
    private final UserService userService;
    private final ReportGenerator reportGenerator;
    private final MedicalProcedureMapper mpMapper;

    public MedicalProcedureService(MedicalProcedureRepository medicalProcedureRepository, ReportGenerator reportGenerator,
                                   AnamnesisService anamnesisService, PetsService petsService, UserService userService,
                                   MedicalProcedureMapper mpMapper) { //почему тут @Qualifier понадобился вдруг
        this.medicalProcedureRepository = medicalProcedureRepository;
        this.reportGenerator = reportGenerator;
        this.mpMapper = mpMapper;
        this.anamnesisService = anamnesisService;
        this.petsService = petsService;
        this.userService = userService;
    }

    public MedicalProcedure findMedicalProcedureById(Long id) {
        return medicalProcedureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical procedure not found with id: " + id));
    }

    public List<MedicalProcedure> findByAnamnesis(Long anamnesisId) {
        return medicalProcedureRepository.findAllByAnamnesisId(anamnesisId);
    }

    public List<MedicalProcedure> findByPetId(Long petId) {
        return medicalProcedureRepository.findAllByPetId(petId);
    }

    public String getReportUrl(Long id) {
        MedicalProcedure mp = medicalProcedureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical procedure not found with id: " + id));
        return reportGenerator.generateReportUrl(mp.getReportUrl());
    }


    @Transactional
    public MedicalProcedure save(MedicalProcedureDTO mpDTO) throws IOException, URISyntaxException {
        MedicalProcedure mp = setRelativeFields(mpMapper.fromDTO(mpDTO), mpDTO);

        mp.setReportUrl("PENDING");
        mp = medicalProcedureRepository.save(mp);  

        String objectName = reportGenerator.generateProcedureReport(mp);
        mp.setReportUrl(objectName);

        return medicalProcedureRepository.save(mp);
    }

    private MedicalProcedure setRelativeFields(MedicalProcedure mp, MedicalProcedureDTO mpDTO) {
        mp.setAnamnesis(anamnesisService.findAnamnesisById(mpDTO.anamnesis()));
        mp.setVet(userService.findById(mpDTO.vet())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + mpDTO.vet())));
        mp.setPet(petsService.findPetById(mpDTO.pet()));
        return mp;
    }
}
