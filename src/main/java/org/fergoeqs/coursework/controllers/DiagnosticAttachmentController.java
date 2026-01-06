package org.fergoeqs.coursework.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.coyote.BadRequestException;
import org.fergoeqs.coursework.dto.DiagnosticAttachmentDTO;
import org.fergoeqs.coursework.models.DiagnosticAttachment;
import org.fergoeqs.coursework.services.AnamnesisService;
import org.fergoeqs.coursework.services.DiagnosticAttachmentService;
import org.fergoeqs.coursework.services.UserService;
import org.fergoeqs.coursework.utils.Mappers.DiagnosticAttachmentMapper;
import org.fergoeqs.coursework.utils.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;

@Tag(name = "Diagnostic Attachments", description = "API для управления диагностическими вложениями")
@RestController
@RequestMapping("/api/diagnostic-attachment")
public class DiagnosticAttachmentController {
    private final DiagnosticAttachmentService diagnosticAttachmentService;
    private final DiagnosticAttachmentMapper diagnosticAttachmentMapper;
    private final UserService userService;
    private final AnamnesisService anamnesisService;

    public DiagnosticAttachmentController(DiagnosticAttachmentService diagnosticAttachmentService, DiagnosticAttachmentMapper diagnosticAttachmentMapper,
                                          UserService userService, AnamnesisService anamnesisService) {
        this.diagnosticAttachmentService = diagnosticAttachmentService;
        this.diagnosticAttachmentMapper = diagnosticAttachmentMapper;
        this.userService = userService;
        this.anamnesisService = anamnesisService;
    }

    @GetMapping("/all-by-anamnesis/{anamnesisId}")
    public ResponseEntity<?> getAllAttachmentsByAnamnesis(@PathVariable Long anamnesisId) throws BadRequestException {
        org.fergoeqs.coursework.models.Anamnesis anamnesis = anamnesisService.findAnamnesisById(anamnesisId);
        org.fergoeqs.coursework.models.AppUser currentUser = userService.getAuthenticatedUser();
        if (anamnesis.getPet() != null) {
            SecurityUtils.checkResourceAccessThroughPet(currentUser, anamnesis.getPet(), false);
        }
        return ResponseEntity.ok(diagnosticAttachmentMapper.toDTOs(
                diagnosticAttachmentService.findByAnamnesis(anamnesisId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAttachment(@PathVariable Long id) throws BadRequestException {
        DiagnosticAttachment attachment = diagnosticAttachmentService.findById(id);
        org.fergoeqs.coursework.models.AppUser currentUser = userService.getAuthenticatedUser();
        if (attachment.getAnamnesis() != null && attachment.getAnamnesis().getPet() != null) {
            SecurityUtils.checkResourceAccessThroughPet(currentUser, attachment.getAnamnesis().getPet(), false);
        }
        return ResponseEntity.ok(diagnosticAttachmentMapper.toDTO(attachment));
    }

    @GetMapping("/url/{id}")
    public ResponseEntity<?> getAttachmentUrl(@PathVariable Long id) throws BadRequestException {
        DiagnosticAttachment attachment = diagnosticAttachmentService.findById(id);
        org.fergoeqs.coursework.models.AppUser currentUser = userService.getAuthenticatedUser();
        if (attachment.getAnamnesis() != null && attachment.getAnamnesis().getPet() != null) {
            SecurityUtils.checkResourceAccessThroughPet(currentUser, attachment.getAnamnesis().getPet(), false);
        }
        return ResponseEntity.ok(diagnosticAttachmentService.getAttachmentUrl(id));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_VET')")
    @PostMapping("/new")
    public ResponseEntity<?> saveAttachment(@RequestParam("diagnosticAttachmentDTO") String diagnosticAttachmentDTOJson,
                                            @RequestParam("file") MultipartFile file) throws IOException, BadRequestException {
        ObjectMapper objectMapper = new ObjectMapper();
        DiagnosticAttachmentDTO diagnosticAttachmentDTO = objectMapper.readValue(diagnosticAttachmentDTOJson, DiagnosticAttachmentDTO.class);
        return ResponseEntity.ok(diagnosticAttachmentMapper.toDTO(diagnosticAttachmentService.save(diagnosticAttachmentDTO, file)));
    }

}
