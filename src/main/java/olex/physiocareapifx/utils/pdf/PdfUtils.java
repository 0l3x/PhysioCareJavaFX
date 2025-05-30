package olex.physiocareapifx.utils.pdf;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.font.FontProvider;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import io.github.cdimascio.dotenv.Dotenv;
import olex.physiocareapifx.model.Appointments.Appointment;
import olex.physiocareapifx.model.Appointments.AppointmentListResponse;
import olex.physiocareapifx.model.Patients.Patient;
import olex.physiocareapifx.model.Physios.Physio;
import olex.physiocareapifx.model.Physios.PhysioResponse;
import olex.physiocareapifx.model.Records.Record;
import olex.physiocareapifx.services.AppointmentService;
import olex.physiocareapifx.services.PatientService;
import olex.physiocareapifx.services.PhysioService;
import olex.physiocareapifx.services.RecordService;

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
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();


    private static final Paragraph header = new Paragraph("COHMPANY Clinic S.A. - S/ Lillo Juan, 128 - 03690 Alicante")
            .setFontSize(14)
            .setUnderline()
            .setTextAlignment(TextAlignment.RIGHT)
            .setMarginBottom(16);


    // Header for the tables
    private static Cell TableHeader(String text) {
        return new Cell().add(new Paragraph(text))
                .setBackgroundColor(ColorConstants.CYAN)
                .setBold()
                .setBorder(new SolidBorder(ColorConstants.DARK_GRAY, 1))
                .setTextAlignment(TextAlignment.LEFT);
    }

    //TODO:
    // 1) Crear PDF de record del paciente sin citas (q envie el id correcto)*!
    // -enviarlo por sftp (funcionando)
    // 2) Crear PDF de citas del paciente con mas de 8 citas, q notifique que les quedan 2 citas (en sentido de almacenar en el record?),
    // -enviarlo por email si cumplen con la condicion de tener 8 citas (funcionando)
    // 3) Crear PDF de salario del fisio, con las citas confirmadas y pendientes del mes, y el total a pagar
    // -enviarlo por email (funcionando)



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
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(20);
            document.add(title);
            float[] colWidths = {3, 5};
            Table recordInfo = new Table(UnitValue.createPercentArray(colWidths));
            recordInfo.setWidth(UnitValue.createPercentValue(100));
            recordInfo.addCell(TableHeader("Record ID"));
            recordInfo.addCell(String.valueOf(record.getId()));
            recordInfo.addCell(TableHeader("Patient's Insurance Number"));
            recordInfo.addCell(String.valueOf(record.getPatient().getInsuranceNumber()));
            recordInfo.addCell(TableHeader("Name"));
            recordInfo.addCell(record.getPatient().getName());
            recordInfo.addCell(TableHeader("Surname"));
            recordInfo.addCell(record.getPatient().getSurname());

            recordInfo.addCell(TableHeader("Birth Date"));
            recordInfo.addCell(LocalDate.parse(record.getPatient().getBirthDate(), DateTimeFormatter.ISO_DATE_TIME)
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

            recordInfo.addCell(TableHeader("Address"));
            recordInfo.addCell(record.getPatient().getAddress());

            recordInfo.addCell(TableHeader("Email"));
            recordInfo.addCell(record.getPatient().getEmail());
            document.add(recordInfo);

            document.add(new Paragraph("\n"));
            Paragraph medTitle = new Paragraph("Observations:")
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
            if (pdfFile.exists()) {
                SftpUpload.uploadFile(
                        dotenv.get("SFTP_USERNAME"),
                        dotenv.get("SFTP_PASSWORD"),
                        dotenv.get("SFTP_HOST"),
                        pdfFile.getAbsolutePath(),
                        dotenv.get("SFTP_PATH") + pdfFile.getName()
                );
                System.out.println("PDF uploaded to SFTP server: " + pdfFile.getName());
            }
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
            appointmentsTable.addHeaderCell(TableHeader("Date"));
            appointmentsTable.addHeaderCell(TableHeader("Diagnosis"));
            appointmentsTable.addHeaderCell(TableHeader("Treatment"));
            appointmentsTable.addHeaderCell(TableHeader("Observations"));
            appointmentsTable.addHeaderCell(TableHeader("Physio"));
            //name physio.getFullName()

            for (Appointment a: patient.getAppointments()) {
                if (a.getStatus().equals("completed")) {
                    appointmentsTable.addCell(new Cell().add(new Paragraph(!a.getDate().isBlank() ? LocalDate.parse(a.getDate(), DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Empty Date")).setFontSize(9));
                    appointmentsTable.addCell(new Cell().add(new Paragraph(!a.getDiagnosis().isBlank() ? a.getDiagnosis() : "Empty Diagnosis")).setFontSize(9));
                    appointmentsTable.addCell(new Cell().add(new Paragraph(!a.getTreatment().isBlank() ? a.getTreatment() : "Empty Treatment")).setFontSize(9));
                    appointmentsTable.addCell(new Cell().add(new Paragraph(!a.getObservations().isBlank() ? a.getObservations() : "Empty Observations")).setFontSize(9));
                    appointmentsTable.addCell(new Cell().add(new Paragraph(a.getNamePhysio())).setFontSize(9));
                }
            }

            document.add(appointmentsTable);

            document.close();

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

        //LocalDate.parse(record.getPatient().getBirthDate(), DateTimeFormatter.ISO_DATE_TIME
        //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);

        String dest = "resources/physios/" + physio.getLicenseNumber() + "_" + currentMonth + ".pdf";
        File newPdf = null;
        Document document;
        try{
            PdfWriter writer = new PdfWriter(dest);
            PdfDocument pdf = new PdfDocument(writer);
            document = new Document(pdf);

            document.add(header);

            // Title
            Paragraph title = new Paragraph("PAYSHEET")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            document.add(title);

            System.out.println(now + " - " + currentMonth);

            // Count last month confirmed appointments
            List<Appointment> confirmed = physio.getAppointments()
                    .stream()
                    .filter(a -> {
                        if(Objects.equals(a.getStatus(), "completed") && !a.getDate().isBlank()) {
                            LocalDate appointmentDate = LocalDate.parse(a.getDate(), DateTimeFormatter.ISO_DATE_TIME);
                            YearMonth appointmentYM = YearMonth.from(appointmentDate);
                            return appointmentYM.equals(currentMonth);
                        }
                        return false;
                    }).toList();

            System.out.println("apointments confirmados: " + confirmed);

            // Calcular salario total
            double totalSalary = (confirmed.size() * 50);

            // Crear tabla resumen
            float[] summaryColWidths = {3, 5};
            Table salarySummary = new Table(UnitValue.createPercentArray(summaryColWidths));
            salarySummary.setWidth(UnitValue.createPercentValue(100));
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
                appointmentsTable.addHeaderCell(TableHeader("Date"));
                appointmentsTable.addHeaderCell(TableHeader("PatientId"));
                appointmentsTable.addHeaderCell(TableHeader("Treatment"));
                appointmentsTable.addHeaderCell(TableHeader("Price"));

                for (Appointment a: confirmed) {
                    if (Objects.equals(a.getStatus(), "pending") || Objects.equals(a.getStatus(), "completed") ) {
                        appointmentsTable.addCell(new Cell().add(new Paragraph(!a.getDate().isBlank() ? LocalDate.parse(a.getDate(), DateTimeFormatter.ISO_DATE_TIME).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Empty Date")).setFontSize(9));
                        // TODO: get patientId que funcione
                        appointmentsTable.addCell(new Cell().add(new Paragraph(a.getPatientId())).setFontSize(9));
                        appointmentsTable.addCell(new Cell().add(new Paragraph(!a.getTreatment().isBlank() ? a.getTreatment() : "Empty Treatment")).setFontSize(9));
                        appointmentsTable.addCell(new Cell().add(new Paragraph("$50").setFontSize(9)));
                    }
                }
                document.add(appointmentsTable);
            }else{
                Paragraph noAppointmentsMessage = new Paragraph("You need to work hard!")
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


}