package olex.physiocareapifx.services;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import olex.physiocareapifx.model.Appointments.Appointment;
import olex.physiocareapifx.model.Appointments.AppointmentListResponse;
import olex.physiocareapifx.model.Appointments.AppointmentResponse;
import olex.physiocareapifx.model.Records.Record;
import olex.physiocareapifx.model.Records.RecordResponse;
import olex.physiocareapifx.utils.MessageUtils;
import olex.physiocareapifx.utils.ServiceUtils;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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

    public static CompletableFuture<Appointment> getAppointment(String url) {
        return ServiceUtils.getResponseAsync(
                url,
                null,
                "GET"
        ).thenApply(
                json-> gson.fromJson(json, AppointmentResponse.class).getAppointment());
    }

    public static CompletableFuture<AppointmentListResponse> getByPhysioId(String physioId) {
        String url = ServiceUtils.API_URL + "/records/appointments/physio/" + physioId;
        return ServiceUtils
                .getResponseAsync(url, null, "GET")
                .thenApply(json -> gson.fromJson(json, AppointmentListResponse.class));
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

    public static CompletableFuture<RecordResponse>  getRecords(String url){
       CompletableFuture<RecordResponse> future = ServiceUtils.getResponseAsync(url, null, "GET")
                .thenApply(response -> gson.fromJson(response, RecordResponse.class));

       while (!future.isDone()) {
            try {
                Thread.sleep(100); // Wait for 100 milliseconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
                break;
            }
        }
         return future;


    }

}
