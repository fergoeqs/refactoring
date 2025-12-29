package org.fergoeqs.coursework.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.fergoeqs.coursework.dto.ReportDTO;
import org.fergoeqs.coursework.models.*;
import org.fergoeqs.coursework.services.StorageService;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ReportGenerator {
    private final StorageService storageService;
    private final byte[] logoBytes;
    private final byte[] stampBytes;
    private final byte[] fontBytes;

    public ReportGenerator(StorageService storageService) throws IOException {
        this.storageService = storageService;
        try (InputStream logoStream = Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("images/logo_min.png"),
                "Logo image not found")) {
            this.logoBytes = logoStream.readAllBytes();
        }
        try (InputStream stampStream = Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("images/stamp.png"),
                "Stamp image not found")) {
            this.stampBytes = stampStream.readAllBytes();
        }
        try (InputStream fontStream = Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("fonts/times_new_roman.ttf"),
                "Font file not found")) {
            this.fontBytes = fontStream.readAllBytes();
        }
    }

    public String generateProcedureReport(MedicalProcedure procedure) throws IOException, URISyntaxException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        float maxWidth = page.getMediaBox().getWidth() - 60;

        PDType0Font font = PDType0Font.load(document, new ByteArrayInputStream(fontBytes));

        PDImageXObject logo = PDImageXObject.createFromByteArray(document, logoBytes, "logo_min.png");
        PDImageXObject stamp = PDImageXObject.createFromByteArray(document, stampBytes, "stamp.png");


        contentStream.drawImage(logo, 40, 700, 70, 70);
        contentStream.beginText();
        contentStream.setFont(font, 16);
        contentStream.newLineAtOffset(180, 750);
        contentStream.showText("limited liability company «VetCare Clinic»");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 16);
        contentStream.newLineAtOffset(220, 730);
        contentStream.showText("Medical procedure report");
        contentStream.endText();


        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(40, 670);
        contentStream.showText("Pet: " + procedure.getPet().getName());
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText("Procedure Name: " + procedure.getName());
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText("Type: " + procedure.getType());
        contentStream.endText();

        float descriptionY = showTextWithLineBreak(contentStream, "Description: " + procedure.getDescription(), 40, 625, maxWidth, font);

        float notesY = showTextWithLineBreak(contentStream, "Notes: " + procedure.getNotes(), 40, descriptionY, maxWidth, font);



        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(40, 150);
        contentStream.showText(("Date: " + procedure.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText("Veterinarian: " + procedure.getVet().getName() + " " + procedure.getVet().getSurname());
        contentStream.endText();

        contentStream.drawImage(stamp, 450, 40, 100, 100);

        contentStream.close();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        document.close();

        InputStream pdfInputStream = new ByteArrayInputStream(outputStream.toByteArray());

        String objectName = "medical_report_" + procedure.getId() + ".pdf";
        String bucketName = "vetcare";

        storageService.uploadFile(bucketName, objectName, pdfInputStream, "application/pdf");
        System.out.println(storageService.generateUrl(bucketName, objectName));

        pdfInputStream.close();

        return objectName;
    }

    public String generatePetReport(ReportDTO reportDTO, Pet pet, Anamnesis anamnesis, List<Diagnosis> diagnoses, List<MedicalProcedure> procedures, List<HealthUpdate> healthUpdates, List<Treatment> treatment) throws IOException, URISyntaxException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        float maxWidth = page.getMediaBox().getWidth() - 60;

        PDType0Font font = PDType0Font.load(document, new ByteArrayInputStream(fontBytes));

        PDImageXObject logo = PDImageXObject.createFromByteArray(document, logoBytes, "logo_min.png");
        PDImageXObject stamp = PDImageXObject.createFromByteArray(document, stampBytes, "stamp.png");

        contentStream.drawImage(logo, 40, 700, 70, 70);
        contentStream.beginText();
        contentStream.setFont(font, 16);
        contentStream.newLineAtOffset(180, 750);
        contentStream.showText("Limited Liability Company «VetCare Clinic»");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 16);
        contentStream.newLineAtOffset(250, 730);
        contentStream.showText("Pet Medical Report");
        contentStream.endText();

        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(40, 670);
        contentStream.showText("Unique Number: " + pet.getId());
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText("Name: " + pet.getName());
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText("Type: " + pet.getType());
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText("Breed: " + pet.getBreed());
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText("Age: " + pet.getAge());
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText("Sex: " + pet.getSex());
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText("Weight: " + pet.getWeight());
        contentStream.endText();

        HealthUpdate initialHealthUpdate = healthUpdates.get(0);
        float healthY = showTextWithLineBreak(contentStream, "State of health at the time of treatment: " + initialHealthUpdate.getSymptoms(), 40, 550, maxWidth, font);

        float historyY = showTextWithLineBreak(contentStream, "[History of appeal]", 40, healthY-15, maxWidth, font);
        historyY = showTextWithLineBreak(contentStream, "Date of appeal: " + anamnesis.getAppointment().getSlot().getDate(), 40, historyY, maxWidth, font);
        historyY = showTextWithLineBreak(contentStream, "Complaints: " + anamnesis.getAppointment().getDescription(), 40, historyY, maxWidth, font);

        Diagnosis primaryDiagnosis = diagnoses.get(0);
        historyY = showTextWithLineBreak(contentStream, "Primary Diagnosis: " + primaryDiagnosis.getName(), 40, historyY, maxWidth, font);

        List<Diagnosis> clinicalDiagnoses = diagnoses.subList(1, diagnoses.size());
        historyY = showTextWithLineBreak(contentStream, "Clinical Diagnoses: " + clinicalDiagnoses.stream().map(Diagnosis::getName).collect(Collectors.joining(", ")), 40, historyY, maxWidth, font);

        historyY = showTextWithLineBreak(contentStream, "Treatment: " + treatment.stream().map(Treatment::getPrescribedMedication).collect(Collectors.joining(", ")), 40, historyY, maxWidth, font);
        historyY = showTextWithLineBreak(contentStream, "Procedures: " + procedures.stream().map(MedicalProcedure::getName).collect(Collectors.joining(", ")), 40, historyY, maxWidth, font);

        float effectivenessY = showTextWithLineBreak(contentStream, "[Effectiveness of treatment]", 40, historyY-15, maxWidth, font);
        for (HealthUpdate update : healthUpdates) {
            effectivenessY = showTextWithLineBreak(contentStream, "Dynamics of the condition: " + update.getSymptoms(), 40, effectivenessY, maxWidth, font);
        }

        float conclusionY = showTextWithLineBreak(contentStream, "Conclusion and recommendations:", 40, effectivenessY, maxWidth, font);
        conclusionY = showTextWithLineBreak(contentStream, "Final Diagnosis: " + reportDTO.finalDiagnosis(), 40, conclusionY, maxWidth, font);
        conclusionY = showTextWithLineBreak(contentStream, "Final Condition: " + reportDTO.finalCondition(), 40, conclusionY, maxWidth, font);
        conclusionY = showTextWithLineBreak(contentStream, "Recommendations: " + reportDTO.recommendations(), 40, conclusionY, maxWidth, font);

        float additionalY = showTextWithLineBreak(contentStream, "[Additional Information]", 40, conclusionY-15, maxWidth, font);
        additionalY = showTextWithLineBreak(contentStream, "Additional Observations: " + reportDTO.additionalObservations(), 40, additionalY, maxWidth, font);
        additionalY = showTextWithLineBreak(contentStream, "Owner Remarks: " + reportDTO.ownerRemarks(), 40, additionalY, maxWidth, font);
        additionalY = showTextWithLineBreak(contentStream, "Next Examination Date: " + reportDTO.nextExaminationDate(), 40, additionalY, maxWidth, font);

        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(40, 150);
        contentStream.showText(("Date: " + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText("Veterinarian: " + pet.getActualVet().getName() + " " + pet.getActualVet().getSurname());
        contentStream.endText();

        contentStream.drawImage(stamp, 450, 40, 100, 100);

        contentStream.close();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        document.close();

        InputStream pdfInputStream = new ByteArrayInputStream(outputStream.toByteArray());

        String objectName = "anamnesis_report_" + anamnesis.getId() + ".pdf";
        String bucketName = "vetcare";

        storageService.uploadFile(bucketName, objectName, pdfInputStream, "application/pdf");
        System.out.println(storageService.generateUrl(bucketName, objectName));

        pdfInputStream.close();

        return objectName;
    }

    public String generateReportUrl(String objectName) {
        return storageService.generateUrl("vetcare", objectName);
    }

    private float showTextWithLineBreak(PDPageContentStream contentStream, String text, float x, float y, float maxWidth, PDType0Font font) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, 12);
        contentStream.newLineAtOffset(x, y);

        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        float currentY = y;

        for (String word : words) {
            line.append(word).append(" ");
            if (getTextWidth(line.toString(), font) > maxWidth) {
                contentStream.showText(line.toString().trim());
                contentStream.newLineAtOffset(0, -15);
                currentY -= 15;
                line = new StringBuilder(word + " ");
            }
        }
        contentStream.showText(line.toString().trim());
        contentStream.endText();
        return currentY - 15;
    }

    private float getTextWidth(String text, PDType0Font font) {
        try {
            return font.getStringWidth(text) / 1000 * 16;
        } catch (IOException e) {
            return 0;
        }
    }
}


