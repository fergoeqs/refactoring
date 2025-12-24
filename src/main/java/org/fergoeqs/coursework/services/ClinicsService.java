package org.fergoeqs.coursework.services;

import org.fergoeqs.coursework.dto.ClinicDTO;
import org.fergoeqs.coursework.exception.ResourceNotFoundException;
import org.fergoeqs.coursework.models.Clinic;
import org.fergoeqs.coursework.repositories.ClinicsRepository;
import org.fergoeqs.coursework.utils.Mappers.ClinicMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@Service
public class ClinicsService {
    private final ClinicsRepository clinicsRepository;
    private final ClinicMapper clinicMapper;

    public ClinicsService(ClinicsRepository clinicsRepository, ClinicMapper clinicMapper) {
        this.clinicsRepository = clinicsRepository;
        this.clinicMapper = clinicMapper;
    }
    public Clinic findById(Long clinicId) {
        return clinicsRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic not found with id: " + clinicId));
    }

    public List<Clinic> findAll() {
        return clinicsRepository.findAll();
    }

    @Transactional
    public Clinic save(ClinicDTO clinicDTO) {
        return clinicsRepository.save(clinicMapper.clinicDTOToClinic(clinicDTO));
    }

    @Transactional
    public void delete(Long clinicId) {
        clinicsRepository.deleteById(clinicId);
    }

    @Transactional
    public void update(ClinicDTO clinicDTO) {
        clinicsRepository.save(clinicMapper.clinicDTOToClinic(clinicDTO));
    }

}
