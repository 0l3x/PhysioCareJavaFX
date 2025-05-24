package olex.physiocareapifx.utils.pdf;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import olex.physiocareapifx.model.Appointments.Appointment;
import olex.physiocareapifx.model.Patients.Patient;
import olex.physiocareapifx.model.Records.Record;
import olex.physiocareapifx.services.PatientService;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;

public class PdfUtils {
    private static final Paragraph header = new Paragraph("COHMPANY Clinic S.A. - S/ McDonalds, Tenesse")
            .setFontSize(16)
            .setItalic()
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20);

    private static Cell HeaderTop(String text) {
        return new Cell().add(new Paragraph(text))
                .setBackgroundColor(ColorConstants.GREEN)
                .setBold()
                .setBorder(new SolidBorder(ColorConstants.DARK_GRAY, 1))
                .setTextAlignment(TextAlignment.LEFT);
    }

    public static void main(String[] args) {
//        System.out.println("Creating PDF de record...");
//        RecordService.getRecordById("67f3fe3996b49b1892b182f0")
//                .thenAccept(record ->{
//                    if(record.isOk()) {
//                        System.out.println("PDF created");
//                        System.out.println("Record ID: " + record.getRecord().getId());
//                        createMedicalRecordPdf(record.getRecord());
//                    }else{
//                        System.out.println("Error: " + record.getError());
//                    }
//                }).exceptionally(e -> {
//                    System.out.println("Error: " + e.getMessage());
//                    return null;
//                });
//        while (true) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        System.out.println("Creating PDF de patient...");
        PatientService.getAppointmentsOfPatientById("67f3fe3996b49b1892b182dc")
                .thenAccept(patient ->{
                    if(patient != null) {
                        //System.out.println("Record ID: " + patient.getPatient().getId());
                        getPatientAppointmentsPdf(patient);
                        System.out.println("PDF created");
                        //createMedicalRecordPdf(record.getPatient());
                    }else{
                        System.out.println("Error: " + "Patient not found");
                    }
                }).exceptionally(e -> {
                    System.out.println("Error: " + e.getMessage());
                    return null;
                });
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createMedicalRecordPdf(Record record){
        String dest = "resources/records/" + record.getPatient().getInsuranceNumber() + ".pdf";
        Document document;
        try{
            System.out.println("Creating PDF2...");
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            document = new Document(pdf);
            document.add(header);
            Paragraph title = new Paragraph("Medical Record")
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);
            float[] colWidths = {2, 4};
            Table recordInfo = new Table(UnitValue.createPercentArray(colWidths));
            recordInfo.setWidth(UnitValue.createPercentValue(100));
            recordInfo.addCell(HeaderTop("Record ID"));
            recordInfo.addCell(String.valueOf(record.getId()));
            recordInfo.addCell(HeaderTop("Patient's Insurance Number"));
            recordInfo.addCell(String.valueOf(record.getPatient().getInsuranceNumber()));
            recordInfo.addCell(HeaderTop("Patient Name"));
            recordInfo.addCell(record.getPatient().getFullName());
            recordInfo.addCell(HeaderTop("Email"));
            recordInfo.addCell(record.getPatient().getEmail());
            document.add(recordInfo);

            document.add(new Paragraph("\n"));
            Paragraph medTitle = new Paragraph("Description:")
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(10);
            document.add(medTitle);

            Paragraph medRecord = new Paragraph(record.getMedicalRecord())
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.JUSTIFIED);

            document.add(medRecord);

            document.close();
            System.out.println("Medical Record PDF created successfully.");

            File pdfFile = new File(dest);
//            if (pdfFile.exists()) {
//                Sftp.savePDF(pdfFile.getAbsolutePath(), pdfFile.getName());
//            }
            if (pdfFile.exists()) {
                System.out.println("PDF file exists: " + pdfFile.getAbsolutePath());
            } else {
                System.out.println("PDF file does not exist: " + pdfFile.getAbsolutePath());
            }

        }catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }
    }

    public static File getPatientAppointmentsPdf(Patient patient){
        String dest = "resources/patients/" + patient.getInsuranceNumber() + ".pdf";
        File newPdf = null;
        Document document;

        // Verificar si el paciente tiene citas
        if (patient.getAppointments() == null || patient.getAppointments().isEmpty()) {
            System.out.println("No appointments found for patient: " + patient.getFullName());
        }

        try{
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            document = new Document(pdf);

            document.add(header);

            // Title
            Paragraph title = new Paragraph(patient.getFullName())
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            int appointmentsCount = Math.max(10 - patient.getAppointments().size(), 0);
            // Available Appointments
            Paragraph availableAppointments = new Paragraph("Available Appointments: " + appointmentsCount)
                    .setFontSize(14)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(availableAppointments);

            // Table with patient info
            float[] colWidths = {1, 1, 1, 2, 1};
            Table appointmentsTable = new Table(UnitValue.createPercentArray(colWidths));
            appointmentsTable.setWidth(UnitValue.createPercentValue(100));
            appointmentsTable.addHeaderCell(HeaderTop("Date"));
            appointmentsTable.addHeaderCell(HeaderTop("Diagnosis"));
            appointmentsTable.addHeaderCell(HeaderTop("Treatment"));
            appointmentsTable.addHeaderCell(HeaderTop("Observations"));
            appointmentsTable.addHeaderCell(HeaderTop("Physio"));
            //name physio.getFullName()

            System.out.println(patient.getAppointments().size());
            for (Appointment a: patient.getAppointments()) {
                if (a.getStatus().equals("completed")) {
                    appointmentsTable.addCell(new Cell().add(new Paragraph(!a.getDate().isBlank() ? a.getDate() : "Empty Date")).setFontSize(9));
                    appointmentsTable.addCell(new Cell().add(new Paragraph(!a.getDiagnosis().isBlank() ? a.getDiagnosis() : "Empty Diagnosis")).setFontSize(9));
                    appointmentsTable.addCell(new Cell().add(new Paragraph(!a.getTreatment().isBlank() ? a.getTreatment() : "Empty Treatment")).setFontSize(9));
                    appointmentsTable.addCell(new Cell().add(new Paragraph(!a.getObservations().isBlank() ? a.getObservations() : "Empty Observations")).setFontSize(9));
                    appointmentsTable.addCell(new Cell().add(new Paragraph(a.getNamePhysio())).setFontSize(9));
                }
            }

            document.add(appointmentsTable);

            document.close();
            System.out.println("Patient appointments PDF created successfully.");

            newPdf = new File(dest);

        }catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return newPdf;
    }


}