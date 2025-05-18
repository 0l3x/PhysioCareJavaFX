package olex.physiocareapifx.services;

import com.google.gson.Gson;
import javafx.application.Platform;
import olex.physiocareapifx.model.Appointments.Appointment;
import olex.physiocareapifx.model.Appointments.AppointmentListResponse;
import olex.physiocareapifx.model.Appointments.AppointmentResponse;
import olex.physiocareapifx.utils.MessageUtils;
import olex.physiocareapifx.utils.ServiceUtils;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AppointmentService {

    private static final Gson gson = new Gson();
    public static CompletableFuture<List<Appointment>> getAppointments(String url) {
        return ServiceUtils.getResponseAsync(
                url,
                null,
                "GET"
        ).thenApply(
            json-> gson.fromJson(json, AppointmentListResponse.class).getAppointments());
    }

    public static CompletableFuture<AppointmentResponse> createAppointment(String url, Appointment appointment){
        return ServiceUtils.getResponseAsync(
                url,
                gson.toJson(appointment),
                "POST"
        ).thenApply(response -> gson.fromJson(response, AppointmentResponse.class));
    }

    public static CompletableFuture<AppointmentResponse> updateAppointment(String url, Appointment appointment){
        return ServiceUtils.getResponseAsync(
                url,
                gson.toJson(appointment),
                "PUT"
        ).thenApply(response -> gson.fromJson(response, AppointmentResponse.class));
    }


    public static CompletableFuture<Void> deleteAppointment(String url) {
        return ServiceUtils.getResponseAsync(
                url,
                null,
                "DELETE"
        ).thenApply(__->null);
    }
}
