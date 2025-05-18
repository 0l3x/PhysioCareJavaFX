package olex.physiocareapifx.utils.pdf;

import com.google.gson.Gson;
import com.itextpdf.html2pdf.HtmlConverter;
import olex.physiocareapifx.model.Patients.Patient;
import olex.physiocareapifx.model.Records.Record;
import olex.physiocareapifx.model.Records.RecordResponse;
import olex.physiocareapifx.utils.ServiceUtils;

import java.io.File;
import java.io.FileOutputStream;

public class PdfUtils {

    private Record record;
    private Patient patient;

    public boolean loadRecordAndPatient(String recordId) {
        try {
            boolean ok = ServiceUtils.login("Olex", "1234");
            if (!ok) {
                System.err.println("!! Login fallido");
                return false;
            }

            // Obtener el Record
            String recordJson = ServiceUtils.getResponse(ServiceUtils.API_URL + "/records/" + recordId, null, "GET");
            RecordResponse response = new Gson().fromJson(recordJson, RecordResponse.class);
            this.record = response.getRecord();

            if (record == null) {
                System.err.println("No se encontró el historial.");
                return false;
            }

            // Obtener el Patient relacionado
            String patientJson = ServiceUtils.getResponse(ServiceUtils.API_URL + "/patients/" + record.getPatient(), null, "GET");
            this.patient = new Gson().fromJson(patientJson, Patient.class);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Record getRecord() {
        return record;
    }

    public Patient getPatient() {
        return patient;
    }

    public String buildSimpleHtml(Record record, Patient patient) {
        StringBuilder html = new StringBuilder();

        html.append("<html><body>");
        html.append("<h1>Historial Médico</h1>");
        html.append("<p><b>Paciente:</b> ").append(patient.getName()).append(" ").append(patient.getSurname()).append("</p>");
        html.append("<p><b>Historial:</b> ").append(record.getMedicalRecord()).append("</p>");
        html.append("<h2>Citas</h2>");
        html.append("<ul>");
        for (var app : record.getAppointments()) {
            html.append("<li>");
            html.append("Fecha: ").append(app.getDate()).append(", ");
            html.append("Diagnóstico: ").append(app.getDiagnosis()).append(", ");
            html.append("Tratamiento: ").append(app.getTreatment());
            html.append("</li>");
        }
        html.append("</ul>");
        html.append("</body></html>");

        return html.toString();
    }



    public static void generatePdfFromHtml(String htmlContent, String filename) {
        try {
            File outputDir = new File("output");
            if (!outputDir.exists()) outputDir.mkdirs();

            String dest = "output/" + filename;
            HtmlConverter.convertToPdf(htmlContent, new FileOutputStream(dest));
            System.out.println("✅ PDF generado en: " + dest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {

        PdfUtils pdfUtils = new PdfUtils();

            boolean ok = ServiceUtils.login("Olex", "1234");

            if (!ok) {
                System.err.println("!! Login fallido");
            }

        if (!pdfUtils.loadRecordAndPatient("67f3fe3996b49b1892b182f0")) {
            System.err.println("!! Login fallido o datos no encontrados");
            return;
        }

        Record record = pdfUtils.getRecord();
        Patient patient = pdfUtils.getPatient();
        System.out.println("Generando Html");
        // Generar HTML desde los datos
        String html = pdfUtils.buildSimpleHtml(record, patient);
        System.out.println("Generado HTML");

        System.out.println("Generando PDF");
        // Generar PDF
        String filename = "historial_" + (patient.getSurname() != null ? patient.getSurname().replace(" ", "_") : "desconocido") + ".pdf";
        generatePdfFromHtml(html, filename);
        System.out.println("Generado PDF");


//        boolean ok = ServiceUtils.login("Olex", "1234");
//
//        if (!ok) {
//            System.err.println("!! Login fallido");
//        }
//
//        try {
//            // Paso 2: hacer la consulta GET
//            String recordId = "67f3fe3996b49b1892b182f0"; // <-- cambia por uno válido
//            String url = ServiceUtils.API_URL + "/records/" + recordId;
//            String jsonResponse = ServiceUtils.getResponse(url, null, "GET");
//
//            // Paso 3: parsear con Gson
//            RecordResponse response = new Gson().fromJson(jsonResponse, RecordResponse.class);
//            Record record = response.getRecord();
//
//            // Paso 4: mostrar por consola
//            System.out.println("!! Record ID: " + record.getId());
//            System.out.println("Paciente ID: " + record.getPatient());
//            System.out.println("Historial: " + record.getMedicalRecord());
//            System.out.println("Citas:");
//            record.getAppointments().forEach(app -> {
//                System.out.println(" - Fecha: " + app.getDate());
//                System.out.println("   Fisio ID: " + app.getPhysio());
//                System.out.println("   Diagnóstico: " + app.getDiagnosis());
//                System.out.println("   Tratamiento: " + app.getTreatment());
//                System.out.println("   Observaciones: " + app.getObservations());
//                System.out.println("   Estado: " + app.getStatus());
//                System.out.println("   ID: " + app.getId());
//                System.out.println();
//            });
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }


}