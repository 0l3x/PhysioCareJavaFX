package olex.physiocareapifx.model.Patients;

import java.util.List;

public class PatientResponse {
    private boolean ok;
    private List<Patient> resultado;
    private String error;

    public boolean isOk() {
        return ok;
    }

    public List<Patient> getResultado() {
        return resultado;
    }

    public String getError() {
        return error;
    }
}
