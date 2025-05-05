package olex.physiocareapifx.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Appointment {
    @SerializedName("_id")
    private String id;

    private Date date;
    private String physio;
    private String diagnosis;
    private String treatment;
    private String observations;
    private String status;

    public Appointment(Date date, String physio, String diagnosis, String treatment, String observations,String status) {
        this.date = date;
        this.physio = physio;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.observations = observations;
        this.status = status;
    }

    public Appointment(String id, Date date, String physio, String diagnosis, String treatment, String observations,String status) {
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
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
