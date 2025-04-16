package olex.physiocareapifx.model;

import com.google.gson.annotations.SerializedName;

public class Physio {
    @SerializedName("_id")
    private String id;
    private String name;
    private String surname;
    private String specialty;
    private String licenseNumber;
    private String email;

    // Constructor vacío, constructor completo, getters y setters
    public Physio() {
    }

    public Physio(String id, String name, String surname, String specialty, String licenseNumber, String email) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.specialty = specialty;
        this.licenseNumber = licenseNumber;
        this.email = email;
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
}
