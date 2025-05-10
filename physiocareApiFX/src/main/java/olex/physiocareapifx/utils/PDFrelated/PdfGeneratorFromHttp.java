package olex.physiocareapifx.utils.PDFrelated;

import com.google.gson.Gson;
import olex.physiocareapifx.model.*;
import olex.physiocareapifx.model.Record;
import olex.physiocareapifx.utils.ServiceUtils;

import java.util.HashMap;
import java.util.Map;

public class PdfGeneratorFromHttp {

    public static void generateRecordPdfById(String recordId) {
        try {
            // 1. Obtener el Record por ID
            String recordJson = ServiceUtils.getResponse(ServiceUtils.API_URL + "/records/" + recordId, null, "GET");
            RecordResponse recordResponse = new Gson().fromJson(recordJson, RecordResponse.class);
            Record record = recordResponse.getRecord();

            if (record == null) {
                System.err.println("No se encontró el historial.");
                return;
            }

            // 2. Obtener el Patient
            String patientJson = ServiceUtils.getResponse(ServiceUtils.API_URL + "/patients/" + record.getPatient(), null, "GET");
            Patient patient = new Gson().fromJson(patientJson, Patient.class);

            // 3. Obtener los Physios implicados y guardarlos en un Map
            Map<String, Physio> physioMap = new HashMap<>();
            for (Appointment ap : record.getAppointments()) {
                String physioId = ap.getPhysio();
                if (!physioMap.containsKey(physioId)) {
                    String physioJson = ServiceUtils.getResponse(ServiceUtils.API_URL + "/physios/" + physioId, null, "GET");
                    Physio physio = new Gson().fromJson(physioJson, Physio.class);
                    physioMap.put(physioId, physio);
                }
            }

            // 4. Generar el HTML y el PDF
            String html = HtmlBuilder.buildHtmlFromRecord(record, null, null);
            System.out.println(html);
            PdfUtils.generatePdfFromHtml(html, "historial_" + patient.getSurname() + ".pdf");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
