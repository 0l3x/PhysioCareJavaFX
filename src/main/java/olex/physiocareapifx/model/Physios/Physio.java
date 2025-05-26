package olex.physiocareapifx.model.Physios;

import com.google.gson.annotations.SerializedName;
import olex.physiocareapifx.model.Appointments.Appointment;

import java.util.ArrayList;
import java.util.List;

public class Physio {
    @SerializedName("_id")
    private String id;
    private String name;
    private String surname;
    private String specialty;
    private String licenseNumber;
    private String email;
    private String avatar;
    private List<Appointment> appointments;

    // Constructor vacío, constructor completo, getters y setters
    public Physio() {
    }

    public Physio(String id, String name, String surname, String specialty, String licenseNumber, String email,String avatar) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.specialty = specialty;
        this.licenseNumber = licenseNumber;
        this.email = email;
        this.avatar = avatar;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public List<Appointment> getAppointments() {
        if(appointments == null) {
            System.out.println("appointments is null");
            return new ArrayList<>();
        }else{
            return appointments;
        }
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    public String getFullName() {
        return name + " " + surname;
    }
}
