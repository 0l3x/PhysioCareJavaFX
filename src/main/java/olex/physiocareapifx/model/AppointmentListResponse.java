package olex.physiocareapifx.model;

import java.util.List;

public class AppointmentListResponse extends BaseResponse {
    private List<Appointment> resultado;

    public List<Appointment> getAppointments(){return resultado;}
}
