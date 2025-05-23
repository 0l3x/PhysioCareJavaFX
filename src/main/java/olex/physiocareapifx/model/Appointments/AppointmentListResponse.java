package olex.physiocareapifx.model.Appointments;

import com.google.gson.annotations.SerializedName;
import olex.physiocareapifx.model.BaseResponse;

import java.util.List;

public class AppointmentListResponse extends BaseResponse {
    @SerializedName("resultado")
    private List<Appointment> appointments;

    public List<Appointment> getAppointments(){return appointments;}
}
