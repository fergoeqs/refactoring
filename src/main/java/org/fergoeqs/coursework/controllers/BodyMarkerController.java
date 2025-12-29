package org.fergoeqs.coursework.controllers;

import org.fergoeqs.coursework.dto.BodyMarkerDTO;
import org.fergoeqs.coursework.services.BodyMarkersService;
import org.fergoeqs.coursework.utils.Mappers.BodyMarkerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/body-marker")
public class BodyMarkerController {
    private final BodyMarkersService bodyMarkersService;
    private final BodyMarkerMapper bodyMarkerMapper;
    private static final Logger logger = LoggerFactory.getLogger(BodyMarkerController.class);

    public BodyMarkerController(BodyMarkersService bodyMarkersService, BodyMarkerMapper bodyMarkerMapper) {
        this.bodyMarkersService = bodyMarkersService;
        this.bodyMarkerMapper = bodyMarkerMapper;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBodyMarkerById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(bodyMarkerMapper.toDTO(bodyMarkersService.findById(id)));
        } catch (Exception e) {
            logger.error("Error while fetching body marker with id: {}", id, e);
            throw e;
        }
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<?> getByAppointment(@PathVariable Long appointmentId) {
        try {
            return bodyMarkersService.findByAppointmentId(appointmentId)
                    .map(bodyMarkerMapper::toDTO)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error fetching marker for appointment: {}", appointmentId, e);
            throw e;
        }
    }
    @PostMapping("/save")
    public ResponseEntity<?> saveBodyMarker(@RequestBody BodyMarkerDTO bodyMarkerDTO) {
        try {
            return ResponseEntity.ok(bodyMarkerMapper.toDTO(bodyMarkersService.save(bodyMarkerDTO)));
        } catch (Exception e) {
            logger.error("Error while saving body marker: {}", bodyMarkerDTO, e);
            throw e;
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateBodyMarker(@PathVariable Long id, @RequestBody BodyMarkerDTO bodyMarkerDTO) {
        try {
            return ResponseEntity.ok(bodyMarkerMapper.toDTO(bodyMarkersService.update(id, bodyMarkerDTO)));
        } catch (Exception e) {
            logger.error("Error while updating body marker: {}", bodyMarkerDTO, e);
            throw e;
        }
    }


}
