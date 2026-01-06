package org.fergoeqs.coursework.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.coyote.BadRequestException;
import org.fergoeqs.coursework.dto.SlotDTO;
import org.fergoeqs.coursework.models.Slot;
import org.fergoeqs.coursework.services.SlotsService;
import org.fergoeqs.coursework.utils.Mappers.SlotMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Slots", description = "API для управления слотами времени")
@RestController
@RequestMapping("/api/slots")
public class SlotsController {
    private final SlotsService slotsService;
    private final SlotMapper slotMapper;

    public SlotsController(SlotsService slotsService, SlotMapper slotMapper) {
        this.slotsService = slotsService;
        this.slotMapper = slotMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSlot(@PathVariable Long id) throws BadRequestException {
        Slot slot = slotsService.getSlotById(id);
        return ResponseEntity.ok(slotMapper.slotToSlotDTO(slot));
    }

    @GetMapping("/available-slots")
    public ResponseEntity<?> getAvailableSlots() throws BadRequestException {
        List<Slot> slots = slotsService.getAvailableSlots();
        return ResponseEntity.ok(slotMapper.slotsToSlotDTOs(slots));
    }

    @GetMapping("/available-priority-slots")
    public ResponseEntity<?> getAvailablePrioritySlots() throws BadRequestException {
        List<Slot> slots = slotsService.getAvailablePrioritySlots();
        return ResponseEntity.ok(slotMapper.slotsToSlotDTOs(slots));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getSlots() throws BadRequestException {
        List<Slot> slots = slotsService.getAllSlots();
        return ResponseEntity.ok(slotMapper.slotsToSlotDTOs(slots));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")
    @PostMapping("/add-slot")
    public ResponseEntity<?> addSlot(@RequestBody SlotDTO slotDTO) throws BadRequestException {
        slotsService.addAvailableSlot(slotDTO);
        return ResponseEntity.ok(slotDTO);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET') or hasRole('ROLE_OWNER')")
    @PutMapping("book-slot/{id}")
    public ResponseEntity<?> bookSlot(@PathVariable Long id) throws BadRequestException {
        slotsService.updateSlotStatus(id, false);
        return ResponseEntity.ok("Slot booked successfully");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")
    @PutMapping("release-slot/{id}")
    public ResponseEntity<?> releaseSlot(@PathVariable Long id) throws BadRequestException {
        slotsService.updateSlotStatus(id, true);
        return ResponseEntity.ok("Slot released successfully");
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")
    @DeleteMapping("/delete-slot/{id}")
    public ResponseEntity<?> deleteSlot(@PathVariable Long id) throws BadRequestException {
        slotsService.deleteSlot(id);
        return ResponseEntity.ok("Slot deleted successfully");
    }

}
