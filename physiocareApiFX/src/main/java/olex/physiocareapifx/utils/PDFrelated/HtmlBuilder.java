package olex.physiocareapifx.utils.PDFrelated;

import olex.physiocareapifx.model.Appointment;
import olex.physiocareapifx.model.Patient;
import olex.physiocareapifx.model.Physio;
import olex.physiocareapifx.model.Record;

import java.util.Map;

public class HtmlBuilder {

    public static String buildHtmlFromRecord(Record record, Patient patient, Map<String, Physio> physioMap) {
        StringBuilder html = new StringBuilder();

        html.append("<html><head><style>")
                .append("body { font-family: Arial, sans-serif; }")
                .append("h1 { color: #2F855A; }")
                .append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }")
                .append("th, td { border: 1px solid #ccc; padding: 8px; }")
                .append("th { background-color: #f2f2f2; }")
                .append("</style></head><body>");

        html.append("<h1>Historial Médico</h1>");

        // Datos del paciente si se proporciona
        if (patient != null) {
            html.append("<p><strong>Paciente:</strong> ")
                    .append(patient.getName()).append(" ").append(patient.getSurname()).append("</p>")
                    .append("<p><strong>Email:</strong> ").append(patient.getEmail()).append("</p>")
                    .append("<p><strong>Fecha de nacimiento:</strong> ").append(patient.getBirthDate()).append("</p>");
        } else {
            html.append("<p><strong>Paciente ID:</strong> ").append(record.getPatient()).append("</p>");
        }

        html.append("<p><strong>Record ID:</strong> ").append(record.getId()).append("</p>");
        html.append("<p><strong>Historial:</strong> ").append(record.getMedicalRecord()).append("</p>");

        html.append("<h2>Citas</h2>");
        html.append("<table><tr><th>Fecha</th><th>Fisioterapeuta</th><th>Diagnóstico</th><th>Tratamiento</th><th>Observaciones</th><th>Estado</th><th>ID</th></tr>");

        for (Appointment app : record.getAppointments()) {
            String physioName = app.getPhysio();
            if (physioMap != null && physioMap.containsKey(app.getPhysio())) {
                Physio p = physioMap.get(app.getPhysio());
                physioName = p.getName() + " " + p.getSurname();
            }

            html.append("<tr>")
                    .append("<td>").append(app.getDate().split("T")[0]).append("</td>")
                    .append("<td>").append(physioName).append("</td>")
                    .append("<td>").append(app.getDiagnosis()).append("</td>")
                    .append("<td>").append(app.getTreatment()).append("</td>")
                    .append("<td>").append(app.getObservations() != null ? app.getObservations() : "-").append("</td>")
                    .append("<td>").append(app.getStatus()).append("</td>")
                    .append("<td>").append(app.getId()).append("</td>")
                    .append("</tr>");
        }

        html.append("</table></body></html>");
        return html.toString();
    }
}
