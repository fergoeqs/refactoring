package org.fergoeqs.coursework.services;

import org.fergoeqs.coursework.dto.BodyMarkerDTO;
import org.fergoeqs.coursework.exception.ResourceNotFoundException;
import org.fergoeqs.coursework.models.BodyMarker;
import org.fergoeqs.coursework.repositories.BodyMarkersRepository;
import org.fergoeqs.coursework.utils.Mappers.BodyMarkerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
@Transactional(readOnly = true)
@Service
public class BodyMarkersService {
    private final BodyMarkersRepository bodyMarkersRepository;
    private final PetsService petsService;
    private final AppointmentsService appointmentsService;
    private final BodyMarkerMapper bodyMarkerMapper;

    public BodyMarkersService(BodyMarkersRepository bodyMarkersRepository, PetsService petsService,
                              AppointmentsService appointmentsService, BodyMarkerMapper bodyMarkerMapper) {
        this.bodyMarkersRepository = bodyMarkersRepository;
        this.petsService = petsService;
        this.appointmentsService = appointmentsService;
        this.bodyMarkerMapper = bodyMarkerMapper;
    }

    public BodyMarker findById(Long id) {
        return bodyMarkersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Body marker not found with id: " + id));
    }

    @Transactional
    public BodyMarker save(BodyMarkerDTO bodyMarkerDTO) {
        BodyMarker bodyMarker = bodyMarkerMapper.fromDTO(bodyMarkerDTO);
        return bodyMarkersRepository.save(setRelativeFields(bodyMarker, bodyMarkerDTO));
    }

    @Transactional
    public BodyMarker update(Long id, BodyMarkerDTO bodyMarkerDTO) {
        BodyMarker bodyMarker = bodyMarkersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Body marker not found with id: " + id));
        bodyMarkerMapper.updateBodyMarkerFromDTO(bodyMarkerDTO, bodyMarker);
        return bodyMarkersRepository.save(setRelativeFields(bodyMarker, bodyMarkerDTO));
    }

    private BodyMarker setRelativeFields(BodyMarker bodyMarker, BodyMarkerDTO bodyMarkerDTO) {
        bodyMarker.setPet(petsService.findPetById(bodyMarkerDTO.pet()));
        bodyMarker.setAppointment(appointmentsService.findById(bodyMarkerDTO.appointment()));
        return bodyMarker;
    }

    public Optional<BodyMarker> findByAppointmentId(Long appointmentId) {
        return bodyMarkersRepository.findByAppointmentId(appointmentId);
    }
}
