package org.fergoeqs.coursework.services;

import org.fergoeqs.coursework.dto.DiagnosticAttachmentDTO;
import org.fergoeqs.coursework.exception.ResourceNotFoundException;
import org.fergoeqs.coursework.models.DiagnosticAttachment;
import org.fergoeqs.coursework.repositories.DiagnosticAttachmentRepository;
import org.fergoeqs.coursework.utils.Mappers.DiagnosticAttachmentMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DiagnosticAttachmentService{
    private final DiagnosticAttachmentRepository diagnosticAttachmentRepository;
    private final DiagnosticAttachmentMapper diagnosticAttachmentMapper;
    private final AnamnesisService anamnesisService;
    private final DiagnosisService diagnosisService;
    private final StorageService storageService;

    public DiagnosticAttachmentService(DiagnosticAttachmentRepository diagnosticAttachmentRepository, AnamnesisService anamnesisService,
                                       DiagnosticAttachmentMapper diagnosticAttachmentMapper, StorageService storageService,
                                       DiagnosisService diagnosisService) {
        this.diagnosticAttachmentRepository = diagnosticAttachmentRepository;
        this.diagnosticAttachmentMapper = diagnosticAttachmentMapper;
        this.storageService = storageService;
        this.anamnesisService = anamnesisService;
        this.diagnosisService = diagnosisService;

    }

    public DiagnosticAttachment findById(Long id) {
        return diagnosticAttachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnostic attachment not found with id: " + id));
    }

    public List<DiagnosticAttachment> findByAnamnesis(Long anamnesisId){
        return diagnosticAttachmentRepository.findAllByAnamnesisId(anamnesisId);
    }

    public String getAttachmentUrl(Long id) {
        DiagnosticAttachment da = diagnosticAttachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnostic attachment not found with id: " + id));
        return da.getFileUrl();
    }

    public DiagnosticAttachment save(DiagnosticAttachmentDTO daDTO, MultipartFile attachment) throws IOException {
        DiagnosticAttachment diagnosticAttachment = diagnosticAttachmentMapper.fromDTO(daDTO);
        diagnosticAttachment.setAnamnesis(anamnesisService.findAnamnesisById(daDTO.anamnesis()));
        if (daDTO.diagnosis() != null) {
            diagnosticAttachment.setDiagnosis(diagnosisService.getDiagnosisById(daDTO.diagnosis()));
        } else {
            diagnosticAttachment.setDiagnosis(null);
        }
        diagnosticAttachment.setUploadDate(LocalDateTime.now());
        String objectName = "anamnesis" + daDTO.anamnesis() + "/" + attachment.getOriginalFilename();
        storageService.uploadFile("attachment", objectName, attachment.getInputStream(), attachment.getContentType());
        diagnosticAttachment.setFileUrl(storageService.generatePublicUrl("attachment", objectName));
        return diagnosticAttachmentRepository.save(diagnosticAttachment); //TODO: генерировать UUID или оставить оригинальное имя файла
    } //TODO: сделать для врача предупреждение, что файл должен именоваться по категории, номеру анамнеза и имени питомца

    public void delete(Long id) {
        diagnosticAttachmentRepository.deleteById(id);
    }
}
