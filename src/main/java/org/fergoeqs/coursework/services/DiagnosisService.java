package org.fergoeqs.coursework.services;

import org.fergoeqs.coursework.dto.DiagnosisDTO;
import org.fergoeqs.coursework.exception.ResourceNotFoundException;
import org.fergoeqs.coursework.models.Diagnosis;
import org.fergoeqs.coursework.models.RecommendedDiagnosis;
import org.fergoeqs.coursework.models.Symptom;
import org.fergoeqs.coursework.repositories.DiagnosisRepository;
import org.fergoeqs.coursework.repositories.SymptomsRepository;
import org.fergoeqs.coursework.utils.Mappers.DiagnosisMapper;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
public class DiagnosisService {
    private final DiagnosisRepository diagnosisRepository;
    private final AnamnesisService anamnesisService;
    private final DiagnosisMapper diagnosisMapper;
    private final RecommendedDiagnosisService recommendedDiagnosisService;
    private final SymptomsRepository symptomsRepository;

    public DiagnosisService(DiagnosisRepository diagnosisRepository, AnamnesisService anamnesisService, SymptomsRepository symptomsRepository,
                            DiagnosisMapper diagnosisMapper, RecommendedDiagnosisService recommendedDiagnosisService) {
        this.diagnosisRepository = diagnosisRepository;
        this.anamnesisService = anamnesisService;
        this.diagnosisMapper = diagnosisMapper;
        this.recommendedDiagnosisService = recommendedDiagnosisService;
        this.symptomsRepository = symptomsRepository;
    }

    public Diagnosis getDiagnosisById(Long id) {
        return diagnosisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnosis not found with id: " + id));
    }

    public List<Diagnosis> getDiagnosesByAnamnesisId(Long anamnesisId) {
        return diagnosisRepository.findByAnamnesisId(anamnesisId);
    }

    public List<Diagnosis> diagnosesExceptFirstByAnamnesisId(Long anamnesisId) {
        return diagnosisRepository.findAllExceptFirstDiagnosisByAnamnesisId(anamnesisId);
    }

    public Diagnosis getFirstByAnamnesisId(Long anamnesisId) {
        return diagnosisRepository.findFirstByAnamnesisIdOrderByDateAsc(anamnesisId)
                .orElseThrow(() -> new ResourceNotFoundException("No diagnosis found for anamnesis with id: " + anamnesisId));
    }

    @Transactional
    public Diagnosis saveDiagnosis(DiagnosisDTO diagnosisDTO){
        Diagnosis diagnosis = diagnosisMapper.fromDTO(diagnosisDTO);
        diagnosis.setAnamnesis(anamnesisService.findAnamnesisById(diagnosisDTO.anamnesis()));
        diagnosis.setDate(LocalDateTime.now());
        List<Symptom> symptoms = symptomsRepository.findAllById(diagnosisDTO.symptoms());
        diagnosis.setSymptoms(symptoms);
        List<Long> symptomIds = diagnosis.getSymptoms().stream()
                .map(Symptom::getId)
                .toList();
        if (!recommendedDiagnosisService.existsByNameAndBodyPartAndSymptoms(diagnosis.getName(), diagnosis.getBodyPart(), symptomIds, (long) symptomIds.size())){
            RecommendedDiagnosis newRecommendedDiagnosis = diagnosisMapper.toRecommendedDiagnosis(diagnosis);
            recommendedDiagnosisService.saveRaw(newRecommendedDiagnosis);
        }

        return diagnosisRepository.save(diagnosis);
    }

    @Transactional
    public Diagnosis updateDiagnosis(Long id, DiagnosisDTO diagnosisDTO){
        Diagnosis diagnosis = diagnosisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnosis not found with id: " + id));
        diagnosisMapper.updateDiagnosisFromDTO(diagnosisDTO, diagnosis);
        return diagnosisRepository.save(diagnosis);
    }

    @Transactional
    public Diagnosis RecommendedToClinical(Long rdId, Long anamnesisId){
        RecommendedDiagnosis rd = recommendedDiagnosisService.findById(rdId);
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setName(rd.getName());
        diagnosis.setBodyPart(rd.getBodyPart());
        diagnosis.setDescription(rd.getDescription());
        diagnosis.setContagious(rd.getContagious());
        diagnosis.setAnamnesis(anamnesisService.findAnamnesisById(anamnesisId));
        diagnosis.setDate(LocalDateTime.now());
        return diagnosisRepository.save(diagnosis);
    }
}
