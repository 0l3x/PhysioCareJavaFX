package olex.physiocareapifx.services;

import com.google.gson.Gson;
import javafx.application.Platform;
import olex.physiocareapifx.model.Appointments.Appointment;
import olex.physiocareapifx.model.Appointments.AppointmentListResponse;
import olex.physiocareapifx.utils.MessageUtils;
import olex.physiocareapifx.utils.ServiceUtils;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AppointmentService {

    private final Gson gson = new Gson();
    public CompletableFuture<List<Appointment>> getAppointments(String url) {
        /*CompletableFuture<List<Appointment>> future = new CompletableFuture<>();
        ServiceUtils.getResponseAsync(url,null,"GET")
                .thenApply(json-> gson.fromJson(json, AppointmentListResponse.class))
                .thenAccept(response->{
                    if(response.isOk()){
                        future.complete(response.getAppointments());
                    }else{
                        future.complete(new ArrayList<>());
                    }
                }).exceptionally(ex->{
                    ex.printStackTrace();
                    return null;
                });
        while (!future.isDone()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        return future;*/
        return ServiceUtils.getResponseAsync(
                url,
                null,
                "GET"
        ).thenApply(
            json-> gson.fromJson(json, AppointmentListResponse.class).getAppointments());
    }
}
