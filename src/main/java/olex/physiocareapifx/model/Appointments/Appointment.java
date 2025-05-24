package olex.physiocareapifx.model.Appointments;

import com.google.gson.annotations.SerializedName;
import olex.physiocareapifx.model.Physios.Physio;
import olex.physiocareapifx.model.Physios.PhysioResponse;
import olex.physiocareapifx.services.PhysioService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Appointment {
    @SerializedName("_id")
    private String id;

    private String date;
    private String physio;
    private String diagnosis;
    private String treatment;
    private String observations;
    private String status;
    @SerializedName("patient")
    private String patientId;

    public Appointment(String date, String physio, String diagnosis, String treatment, String observations,String status) {
        this.date = date;
        this.physio = physio;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.observations = observations;
        this.status = status;
    }

    public Appointment(String id, String date, String physio, String diagnosis, String treatment, String observations,String status) {
        this.id = id;
        this.date = date;
        this.physio = physio;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.observations = observations;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPhysio() {
        return physio;
    }

    public void setPhysio(String physio) {
        this.physio = physio;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNamePhysio() throws ExecutionException, InterruptedException {
        CompletableFuture<String> namePhysioFuture =
                PhysioService.getPhysio(physio)
                        .thenApply(physioResponse -> {
                            if (physioResponse.isOk()) {
                                return physioResponse.getPhysio().getName();
                            } else {
                                return "Unknown Physio";
                            }
                        })
                        .exceptionally(ex -> {
                            return "Unknown Physio";
                        });

        return namePhysioFuture.get();
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id='" + id + '\'' +
                ", date=" + date +
                ", physio='" + physio + '\'' +
                ", diagnosis='" + diagnosis + '\'' +
                ", treatment='" + treatment + '\'' +
                ", observations='" + observations + '\'' +
                '}';
    }
}
