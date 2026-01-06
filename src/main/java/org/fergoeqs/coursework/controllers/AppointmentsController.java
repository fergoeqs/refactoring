package org.fergoeqs.coursework.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.coyote.BadRequestException;
import org.fergoeqs.coursework.dto.AppointmentDTO;
import org.fergoeqs.coursework.models.Appointment;
import org.fergoeqs.coursework.services.AppointmentsService;
import org.fergoeqs.coursework.services.UserService;
import org.fergoeqs.coursework.utils.Mappers.AppointmentMapper;
import org.fergoeqs.coursework.utils.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Appointments", description = "API для управления записями на прием")
@RestController
@RequestMapping("/api/appointments")
public class AppointmentsController {

    private final AppointmentsService appointmentsService;
    private final AppointmentMapper appointmentMapper;
    private final UserService userService;

    public AppointmentsController(AppointmentsService appointmentsService, AppointmentMapper appointmentMapper, UserService userService) {
        this.appointmentsService = appointmentsService;
        this.appointmentMapper = appointmentMapper;
        this.userService = userService;
    }

    @GetMapping("/appointment/{id}")
    public ResponseEntity<?> getAppointment(@PathVariable Long id) throws BadRequestException {
        Appointment appointment = appointmentsService.findById(id);
        org.fergoeqs.coursework.models.AppUser currentUser = userService.getAuthenticatedUser();
        SecurityUtils.checkAppointmentAccess(currentUser, appointment, false);
        return ResponseEntity.ok(appointmentMapper.appointmentToAppointmentDTO(appointment));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllAppointments() throws BadRequestException {
        return ResponseEntity.ok(appointmentMapper.appointmentsToAppointmentDTOs(appointmentsService.findAll()));
    }

    @GetMapping("/without-anamnesis")
    public ResponseEntity<?> getAppointmentsWithoutAnamnesis() throws BadRequestException {
        return ResponseEntity.ok(appointmentMapper.appointmentsToAppointmentDTOs(appointmentsService.findAppointmentsWithoutAnamnesis()));
    }

    @GetMapping("/vet-appointments/{vetId}")
    public ResponseEntity<?> getVetAppointments(@PathVariable Long vetId) throws BadRequestException {
        List<Appointment> appointments = appointmentsService.findByVetId(vetId);
        return ResponseEntity.ok(appointmentMapper.appointmentsToAppointmentDTOs(appointments));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")
    @GetMapping("upcoming-vet/{vetId}")
    public ResponseEntity<?> getUpcomingVetAppointments(@PathVariable Long vetId) throws BadRequestException {
        List<Appointment> appointments = appointmentsService.getUpcomingVetAppointments(vetId);
        return ResponseEntity.ok(appointmentMapper.appointmentsToAppointmentDTOs(appointments));
    }

    @GetMapping("upcoming-pet/{petId}")
    public ResponseEntity<?> getUpcomingPetAppointments(@PathVariable Long petId) throws BadRequestException {
        List<Appointment> appointments = appointmentsService.getUpcomingPetAppointments(petId);
        return ResponseEntity.ok(appointmentMapper.appointmentsToAppointmentDTOs(appointments));
    }

    @PostMapping("/new-appointment")
    public ResponseEntity<?> createAppointment(@RequestBody AppointmentDTO appointmentDTO) throws BadRequestException {
        if (appointmentDTO == null) {
            throw new BadRequestException("Appointment data is invalid.");
        }
        return ResponseEntity.ok(appointmentMapper.appointmentToAppointmentDTO(appointmentsService.create(appointmentDTO)));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET') or hasRole('ROLE_OWNER')")
    @PutMapping("/update-appointment/{id}")
    public ResponseEntity<?> updateAppointment(@PathVariable Long id, @RequestParam Long slotId) throws BadRequestException {
        Appointment appointment = appointmentsService.findById(id);
        org.fergoeqs.coursework.models.AppUser currentUser = userService.getAuthenticatedUser();
        SecurityUtils.checkAppointmentAccess(currentUser, appointment, true);
        return ResponseEntity.ok(appointmentMapper.appointmentToAppointmentDTO(appointmentsService.update(id, slotId)));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")
    @DeleteMapping("/cancel-appointment/{id}")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id, @RequestBody String reason) throws BadRequestException {
        appointmentsService.delete(id, reason);
        return ResponseEntity.ok("Appointment " + id + " canceled");
    }
}