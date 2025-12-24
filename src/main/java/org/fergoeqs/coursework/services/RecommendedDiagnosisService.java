package org.fergoeqs.coursework.services;

import org.fergoeqs.coursework.dto.RecommendedDiagnosisDTO;
import org.fergoeqs.coursework.exception.ResourceNotFoundException;
import org.fergoeqs.coursework.models.RecommendedDiagnosis;
import org.fergoeqs.coursework.models.Symptom;
import org.fergoeqs.coursework.models.enums.BodyPart;
import org.fergoeqs.coursework.repositories.RecommendedDiagnosisRepository;
import org.fergoeqs.coursework.repositories.SymptomsRepository;
import org.fergoeqs.coursework.utils.Mappers.RecommendedDiagnosisMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendedDiagnosisService {
    private final RecommendedDiagnosisRepository recommendedDiagnosisRepository;
    private final RecommendedDiagnosisMapper recommendedDiagnosisMapper;
    private final SymptomsRepository symptomsRepository;

    public RecommendedDiagnosisService(RecommendedDiagnosisRepository recommendedDiagnosisRepository,
                                       RecommendedDiagnosisMapper recommendedDiagnosisMapper, SymptomsRepository symptomsRepository) {
        this.recommendedDiagnosisRepository = recommendedDiagnosisRepository;
        this.recommendedDiagnosisMapper = recommendedDiagnosisMapper;
        this.symptomsRepository = symptomsRepository;

    }

    public RecommendedDiagnosis findById(Long id) {
        return recommendedDiagnosisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recommended diagnosis not found with id: " + id));
    }

    public List<RecommendedDiagnosis> findAll() {
        return recommendedDiagnosisRepository.findAll();
    }

    public List<RecommendedDiagnosis> findBySymptomsAndBodyPart(List<Long> symptoms, BodyPart bodyPart) {
        return recommendedDiagnosisRepository.findBySymptomsAndBodyPart(symptoms, bodyPart);
    }

    public boolean existsByNameAndBodyPartAndSymptoms(String name, BodyPart bodyPart, List<Long> symptomIds, Long symptomCount){
        return recommendedDiagnosisRepository.existsByNameAndBodyPartAndSymptoms(name, bodyPart, symptomIds, symptomCount);
    };

    public RecommendedDiagnosis save(RecommendedDiagnosisDTO recommendedDiagnosisDTO) {
        RecommendedDiagnosis recDiagnosis = recommendedDiagnosisMapper.fromDTO(recommendedDiagnosisDTO);
        List<Symptom> symptoms = symptomsRepository.findAllById(recommendedDiagnosisDTO.symptoms());
        recDiagnosis.setSymptoms(symptoms);
        return recommendedDiagnosisRepository.save(recDiagnosis);
    }

    public void saveRaw(RecommendedDiagnosis recommendedDiagnosis) {
        recommendedDiagnosisRepository.save(recommendedDiagnosis);
    }



}
