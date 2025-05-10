package olex.physiocareapifx.utils.PDFrelated;

import com.google.gson.Gson;
import com.itextpdf.html2pdf.HtmlConverter;
import olex.physiocareapifx.model.*;
import olex.physiocareapifx.model.Record;
import olex.physiocareapifx.utils.ServiceUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class PdfUtils {

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
        boolean ok = ServiceUtils.login("hector2", "1234");
        if (!ok) {
            System.err.println("❌ Login fallido");
            return;
        }

        try {
            // 1. Obtener el Record
            String recordId = "67f3fe3996b49b1892b182f0";
            String jsonRecord = ServiceUtils.getResponse(ServiceUtils.API_URL + "/records/" + recordId, null, "GET");
            RecordResponse recordResponse = new Gson().fromJson(jsonRecord, RecordResponse.class);
            Record record = recordResponse.getRecord();

            // 2. Obtener el Patient
            String jsonPatient = ServiceUtils.getResponse(ServiceUtils.API_URL + "/patients/" + record.getPatient(), null, "GET");
            Patient patient = new Gson().fromJson(jsonPatient, Patient.class);

            // 3. Obtener los Physios implicados
            Map<String, Physio> physioMap = new HashMap<>();
            for (Appointment ap : record.getAppointments()) {
                String physioId = ap.getPhysio();
                if (!physioMap.containsKey(physioId)) {
                    String physioJson = ServiceUtils.getResponse(ServiceUtils.API_URL + "/physios/" + physioId, null, "GET");
                    Physio physio = new Gson().fromJson(physioJson, Physio.class);
                    physioMap.put(physioId, physio);
                }
            }

            // 4. Construir HTML con HtmlBuilder
            String html = HtmlBuilder.buildHtmlFromRecord(record, patient, physioMap);

            // 5. Generar PDF
            String filename = "historial_" + patient.getSurname().replaceAll(" ", "_") + ".pdf";
            generatePdfFromHtml(html, filename);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
