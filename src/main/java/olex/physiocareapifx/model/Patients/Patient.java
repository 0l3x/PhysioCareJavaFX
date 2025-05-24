package olex.physiocareapifx.model.Patients;

import com.google.gson.annotations.SerializedName;
import com.itextpdf.kernel.pdf.PdfArray;
import olex.physiocareapifx.model.Appointments.Appointment;

import java.util.ArrayList;
import java.util.List;

/**
 * Patient model class to match the API structure.
 */
public class Patient {
    @SerializedName("_id")
    private String id;
    private String name;
    private String surname;
    private String birthDate;
    private String address;
    private String insuranceNumber;
    private String email;
    private List<Appointment> appointments;
    private String password;

    public Patient() {}

    public Patient(String id, String name, String surname, String birthDate, String address, String insuranceNumber, String email) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.birthDate = birthDate;
        this.address = address;
        this.insuranceNumber = insuranceNumber;
        this.email = email;
    }
    public Patient(String id, String name, String surname, String birthDate, String address, String insuranceNumber, String email,String password) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.birthDate = birthDate;
        this.address = address;
        this.insuranceNumber = insuranceNumber;
        this.email = email;
        this.password = password;
    }


    // Getters and setters

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

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getInsuranceNumber() {
        return insuranceNumber;
    }

    public void setInsuranceNumber(String insuranceNumber) {
        this.insuranceNumber = insuranceNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return name + " " + surname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Appointment> getAppointments() {
        if(appointments == null) {
            return new ArrayList<>();
        }else{
            return appointments;
        }
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }
}
