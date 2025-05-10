package olex.physiocareapifx.utils.pdf;

import com.google.gson.Gson;
import olex.physiocareapifx.model.Record;
import olex.physiocareapifx.model.RecordResponse;
import olex.physiocareapifx.utils.ServiceUtils;

public class PdfUtils {
    public static void main(String[] args) {

        boolean ok = ServiceUtils.login("Olex", "1234");

        if (!ok) {
            System.err.println("!! Login fallido");
        }

        try {
            // Paso 2: hacer la consulta GET
            String recordId = "67f3fe3996b49b1892b182f0"; // <-- cambia por uno válido
            String url = ServiceUtils.API_URL + "/records/" + recordId;
            String jsonResponse = ServiceUtils.getResponse(url, null, "GET");

            // Paso 3: parsear con Gson
            RecordResponse response = new Gson().fromJson(jsonResponse, RecordResponse.class);
            Record record = response.getRecord();

            // Paso 4: mostrar por consola
            System.out.println("!! Record ID: " + record.getId());
            System.out.println("Paciente ID: " + record.getPatient());
            System.out.println("Historial: " + record.getMedicalRecord());
            System.out.println("Citas:");
            record.getAppointments().forEach(app -> {
                System.out.println(" - Fecha: " + app.getDate());
                System.out.println("   Fisio ID: " + app.getPhysio());
                System.out.println("   Diagnóstico: " + app.getDiagnosis());
                System.out.println("   Tratamiento: " + app.getTreatment());
                System.out.println("   Observaciones: " + app.getObservations());
                System.out.println("   Estado: " + app.getStatus());
                System.out.println("   ID: " + app.getId());
                System.out.println();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}