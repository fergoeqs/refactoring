package org.fergoeqs.coursework.services;

import org.fergoeqs.coursework.dto.AppointmentDTO;
import org.fergoeqs.coursework.exception.ResourceNotFoundException;
import org.fergoeqs.coursework.models.AppUser;
import org.fergoeqs.coursework.models.Appointment;
import org.fergoeqs.coursework.models.Slot;
import org.fergoeqs.coursework.models.Pet;
import org.fergoeqs.coursework.repositories.AppointmentsRepository;
import org.fergoeqs.coursework.repositories.SlotsRepository;
import org.fergoeqs.coursework.repositories.PetsRepository;
import org.fergoeqs.coursework.utils.Mappers.AppointmentMapper;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Transactional(readOnly = true)
@Service
public class AppointmentsService {
    private final AppointmentsRepository appointmentsRepository;
    private final SlotsRepository availableSlotsRepository;
    private final PetsRepository petsRepository;
    private final HealthUpdatesService healthUpdatesService;
    private final NotificationService notificationService;
    private final AppointmentMapper appointmentMapper;

    public AppointmentsService(AppointmentsRepository appointmentsRepository, SlotsRepository availableSlotsRepository,
                               PetsRepository petsRepository, AppointmentMapper appointmentMapper,
                               HealthUpdatesService healthUpdatesService, NotificationService notificationService) {
        this.appointmentsRepository = appointmentsRepository;
        this.availableSlotsRepository = availableSlotsRepository;
        this.petsRepository = petsRepository;
        this.healthUpdatesService = healthUpdatesService;
        this.notificationService = notificationService;
        this.appointmentMapper = appointmentMapper;
    }

    public List<Appointment> findAll() {
        return appointmentsRepository.findAll();
    }

    public Appointment findById(Long id) {
        return appointmentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
    }

    public List<Appointment> findAppointmentsWithoutAnamnesis() {
        return appointmentsRepository.findAppointmentsWithoutAnamnesis();
    }

    public List<Appointment> findByVetId(Long vetId) {
        return appointmentsRepository.findBySlot_VetId(vetId);
    }

    public List<Appointment> getUpcomingVetAppointments(Long vetId) {
        return appointmentsRepository.findBySlotVetIdAndSlotDateGreaterThanEqual(vetId, LocalDate.now());
    }
    public List<Appointment> getUpcomingPetAppointments(Long petId) {
        return appointmentsRepository.findByPetIdAndSlotDateGreaterThanEqual(petId, LocalDate.now());
    }

    public boolean existsByOwnerAndVet(Long ownerId, Long vetId) {
        return appointmentsRepository.existsByOwnerAndVet(ownerId, vetId);
    }

    @Transactional
    public Appointment create(AppointmentDTO appointmentDTO) {
        Pet pet = petsRepository.findById(appointmentDTO.petId())
                .orElseThrow(() -> new ResourceNotFoundException("Pet not found with id: " + appointmentDTO.petId()));
        healthUpdatesService.saveWithAppointment(pet, appointmentDTO.description());
        return appointmentsRepository.save(appointmentMapper.appointmentDTOToAppointment(appointmentDTO));
    }

    @Transactional
    public Appointment update(Long appointmentId, Long slotId) {
        Appointment appointment = appointmentsRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));
        Slot slot = availableSlotsRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + slotId));
        appointment.setSlot(slot);
        return appointmentsRepository.save(appointment);
    }

    @Transactional
    public void delete(Long id, String reason) {
        Appointment appointment = appointmentsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        sendNotification(appointment, "Your appointment has been cancelled by vet cause: " + reason, false);
        appointmentsRepository.deleteById(id);
    }

    @Transactional
//    @Scheduled(cron = "0 0 9 * * *")
    @Scheduled(cron = "0 0 9 * * *")
    public void sendNotification() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Appointment> upcomingAppointments = appointmentsRepository.findAppointmentsBySlotDate(tomorrow);

        for (Appointment appointment : upcomingAppointments) {
            String message = "Pet " + appointment.getPet().getName() +  " has an appointment scheduled for tomorrow at " + appointment.getSlot().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            sendNotification(appointment, message, true);
        }
    }

    private void sendNotification(Appointment appointment, String message, Boolean dublicateVet) {
        AppUser owner = appointment.getPet().getOwner();
        notificationService.sendNotification(owner.getId(), message, owner.getEmail());
        if (dublicateVet) {
            notificationService.sendNotification(appointment.getSlot().getVet().getId(), message, appointment.getSlot().getVet().getEmail());
        }
    }


}
