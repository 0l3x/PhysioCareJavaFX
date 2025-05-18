package olex.physiocareapifx.utils.pdf;

import olex.physiocareapifx.model.Patients.Patient;
import olex.physiocareapifx.model.Records.Record;

public class HtmlCreator {

    private PdfUtils pdfUtils = new PdfUtils();

    // Método para generar HTML simple con datos en párrafos
    public String createSimpleHtml(Record record, Patient patient) {
        StringBuilder html = new StringBuilder();

        html.append("<html><body>");
        html.append("<h1>Historial Médico</h1>");

        html.append("<h2>Datos del Paciente</h2>");
        html.append("<p>Nombre: ").append(patient.getName()).append(" ").append(patient.getSurname()).append("</p>");
        html.append("<p>Fecha de nacimiento: ").append(patient.getBirthDate()).append("</p>");
        html.append("<p>Dirección: ").append(patient.getAddress()).append("</p>");
        html.append("<p>Email: ").append(patient.getEmail()).append("</p>");
        html.append("<p>Número de seguro: ").append(patient.getInsuranceNumber()).append("</p>");

        html.append("<h2>Información Médica</h2>");
        html.append("<p>").append(record.getMedicalRecord()).append("</p>");

        html.append("<h2>Citas</h2>");
        record.getAppointments().forEach(app -> {
            html.append("<p>Fecha: ").append(app.getDate()).append("</p>");
            html.append("<p>Fisioterapeuta ID: ").append(app.getPhysio()).append("</p>");
            html.append("<p>Diagnóstico: ").append(app.getDiagnosis()).append("</p>");
            html.append("<p>Tratamiento: ").append(app.getTreatment()).append("</p>");
            html.append("<p>Observaciones: ").append(app.getObservations() != null ? app.getObservations() : "-").append("</p>");
            html.append("<p>Estado: ").append(app.getStatus()).append("</p>");
            html.append("<hr>"); // Línea horizontal para separar citas
        });

        html.append("</body></html>");

        return html.toString();
    }

    // Método para cargar datos y crear HTML
    public String generateHtmlFromApi(String recordId) {
        if (pdfUtils.loadRecordAndPatient(recordId)) {
            Record record = pdfUtils.getRecord();
            Patient patient = pdfUtils.getPatient();

            return createSimpleHtml(record, patient);
        }
        return "<html><body><p>Error cargando datos</p></body></html>";
    }

    // Ejemplo main para probar
    public static void main(String[] args) {
        HtmlCreator creator = new HtmlCreator();
        String html = creator.generateHtmlFromApi("67f3fe3996b49b1892b182f0");
        System.out.println(html);
        //creator.generatePdfFromRecord("67f3fe3996b49b1892b182f0");
    }

    public void generatePdfFromRecord(String recordId) {
        String html = generateHtmlFromApi(recordId);
        if (!html.contains("Error cargando datos")) {
            String filename = "historial_" + recordId + ".pdf";
            //PdfUtils.generatePdfFromHtml(html, filename);
        } else {
            System.err.println("No se pudo generar el PDF, hubo un error cargando los datos");
        }
    }

}
