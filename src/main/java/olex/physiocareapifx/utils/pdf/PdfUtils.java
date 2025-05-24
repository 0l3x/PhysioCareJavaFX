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
import olex.physiocareapifx.model.Appointments.AppointmentListResponse;
import olex.physiocareapifx.model.Patients.Patient;
import olex.physiocareapifx.model.Physios.Physio;
import olex.physiocareapifx.model.Physios.PhysioResponse;
import olex.physiocareapifx.model.Records.Record;
import olex.physiocareapifx.services.AppointmentService;
import olex.physiocareapifx.services.PatientService;
import olex.physiocareapifx.services.PhysioService;

import java.io.File;
import java.io.FileNotFoundException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
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

//        System.out.println("Creating PDF de patient...");
//        PatientService.getAppointmentsOfPatientById("67f3fe3996b49b1892b182dc")
//                .thenAccept(patient ->{
//                    if(patient != null) {
//                        //System.out.println("Record ID: " + patient.getPatient().getId());
//                        getPatientAppointmentsPdf(patient);
//                        System.out.println("PDF created");
//                        //createMedicalRecordPdf(record.getPatient());
//                    }else{
//                        System.out.println("Error: " + "Patient not found");
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

//        AppointmentService appointmentService = new AppointmentService();
//
//        String id = "67f3fe3996b49b1892b182e4";
//
//        CompletableFuture<PhysioResponse> fut1 = PhysioService.getById(id);
//        CompletableFuture<AppointmentListResponse> fut2 = AppointmentService.getByPhysioId(id);
//
//        CompletableFuture.allOf(fut1, fut2).thenRun(() -> {
//            Physio p    = fut1.join().getPhysio();
//            List<Appointment> apps = fut2.join().getAppointments();
//            p.setAppointments(apps);
//            PdfUtils.createPhysioPdf(p);
//        });

        // El ID del fisioterapeuta que quieres probar
        String physioId = "67f3fe3996b49b1892b182e4";

        // Lanzamos ambas llamadas asíncronas
        CompletableFuture<PhysioResponse> physioFut = PhysioService.getById(physioId);
        CompletableFuture<AppointmentListResponse> appsFut = AppointmentService.getByPhysioId(physioId);

        // Cuando ambas terminen, las unimos
        CompletableFuture<Void> combined = CompletableFuture
                .allOf(physioFut, appsFut)
                .thenRun(() -> {
                    PhysioResponse physioResp = physioFut.join();
                    AppointmentListResponse appsResp = appsFut.join();

                    if (!physioResp.isOk()) {
                        System.err.println("Error al obtener el fisio: " + physioResp.getError());
                        return;
                    }
                    if (!appsResp.isOk()) {
                        System.err.println("Error al obtener citas: " + appsResp.getError());
                        return;
                    }

                    Physio p = physioResp.getPhysio();              // tu PhysioResponse debería exponer getResultado()
                    List<Appointment> citas = appsResp.getAppointments();

                    p.setAppointments(citas);                          // le inyectamos las citas
                    PdfUtils.createPhysioPdf(p);                       // y generamos el PDF

                    System.out.println("✅ PDF de nómina creado para " + p.getFullName());
                })
                .exceptionally(err -> {
                    err.printStackTrace();
                    return null;
                });

        // Esperamos a que todo termine antes de salir
        combined.join();

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


    public static File createPhysioPdf(Physio physio){
        LocalDate now = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(now);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);

        String dest = "resources/physios/" + physio.getLicenseNumber() + "_" + currentMonth + ".pdf";
        File newPdf = null;
        Document document;
        try{
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            document = new Document(pdf);

            document.add(header);

            // Title
            Paragraph title = new Paragraph("- PAYROLL -")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            // Count last month confirmed appointments
            List<Appointment> confirmed = physio.getAppointments()
                    .stream()
                    .filter(a -> {
                        if(Objects.equals(a.getStatus(), "pending") && Objects.equals(a.getStatus(), "completed") && !a.getDate().isBlank()) {
                            LocalDate appointmentDate = LocalDate.parse(a.getDate(), formatter);
                            YearMonth appointmentYM = YearMonth.from(appointmentDate);
                            return appointmentYM.equals(currentMonth);
                        }
                        return false;
                    }).toList();

            // Calcular salario total
            double totalSalary = (confirmed.size() * 100);

            // Crear tabla resumen
            float[] summaryColWidths = {3, 3};
            Table salarySummary = new Table(UnitValue.createPercentArray(summaryColWidths));
            salarySummary.setWidth(UnitValue.createPercentValue(50));
            salarySummary.addCell(new Cell().add(new Paragraph("Physiotherapist")).setFontSize(12).setBold());
            salarySummary.addCell(new Cell().add(new Paragraph(physio.getFullName())).setFontSize(12));
            salarySummary.addCell(new Cell().add(new Paragraph("Month")).setFontSize(12).setBold());
            salarySummary.addCell(new Cell().add(new Paragraph(currentMonth.toString())).setFontSize(10));
            salarySummary.addCell(new Cell().add(new Paragraph("Licence Number")).setFontSize(10).setBold());
            salarySummary.addCell(new Cell().add(new Paragraph(physio.getLicenseNumber())).setFontSize(10));
            salarySummary.addCell(new Cell().add(new Paragraph("Confirmed Appointments")).setFontSize(10).setBold());
            salarySummary.addCell(new Cell().add(new Paragraph(String.valueOf(confirmed.size())).setFontSize(10)));
            salarySummary.addCell(new Cell().add(new Paragraph("Total Salary")).setFontSize(12).setBold());
            salarySummary.addCell(new Cell().add(new Paragraph(String.format("$%.2f", totalSalary))).setFontSize(12).setBold());

            document.add(salarySummary);
            document.add(new Paragraph("\n"));

            // Confimed Appointments
            Paragraph confirmedAppointments = new Paragraph("Confirmed Appointments")
                    .setFontSize(14)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(confirmedAppointments);



            if(!confirmed.isEmpty()){
                // Table with patient info
                float[] colWidths = {1, 1, 4, 1};
                Table appointmentsTable = new Table(UnitValue.createPercentArray(colWidths));
                appointmentsTable.setWidth(UnitValue.createPercentValue(100));
                appointmentsTable.addHeaderCell(getHeaderCell("Date"));
                appointmentsTable.addHeaderCell(getHeaderCell("PatientId"));
                appointmentsTable.addHeaderCell(getHeaderCell("Treatment"));
                appointmentsTable.addHeaderCell(getHeaderCell("Price"));

                for (Appointment a: confirmed) {
                    if (Objects.equals(a.getStatus(), "pending") && Objects.equals(a.getStatus(), "completed") ) {
                        appointmentsTable.addCell(new Cell().add(new Paragraph(!a.getDate().isBlank() ? a.getDate() : "Empty Date")).setFontSize(9));
                        appointmentsTable.addCell(new Cell().add(new Paragraph(a.getPatientId())).setFontSize(9));
                        appointmentsTable.addCell(new Cell().add(new Paragraph(!a.getTreatment().isBlank() ? a.getTreatment() : "Empty Treatment")).setFontSize(9));
                        appointmentsTable.addCell(new Cell().add(new Paragraph("$100").setFontSize(9)));
                    }
                }
                document.add(appointmentsTable);
            }else{
                Paragraph noAppointmentsMessage = new Paragraph("You need work hard!")
                        .setFontSize(9)
                        .setItalic()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(20);
                document.add(confirmedAppointments);

                document.add(noAppointmentsMessage);
            }


            document.close();
            System.out.println("Physio salary PDF created successfully.");

            newPdf = new File(dest);

        }catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }
        return newPdf;
    }

    private static Cell getHeaderCell(String text) {
        return new Cell().add(new Paragraph(text))
                .setBackgroundColor(ColorConstants.CYAN)
                .setBold()
                .setBorder(new SolidBorder(ColorConstants.BLUE, 1))
                .setTextAlignment(TextAlignment.LEFT);
    }
}