package olex.physiocareapifx.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import olex.physiocareapifx.model.Appointments.Appointment;
import olex.physiocareapifx.model.Appointments.AppointmentResponse;
import olex.physiocareapifx.model.BaseResponse;
import olex.physiocareapifx.model.Physios.Physio;
import olex.physiocareapifx.model.Physios.PhysioResponse;
import olex.physiocareapifx.utils.ServiceUtils;

import java.util.concurrent.CompletableFuture;

/**
 * Service class to handle asynchronous operations (POST, PUT, DELETE) for Physios.
 */
public class PhysioService extends Service<BaseResponse> {
    private static final Gson gson = new Gson();

    public static CompletableFuture<PhysioResponse> getById(String physioId) {
        String url = ServiceUtils.API_URL + "/physios/" + physioId;
        return ServiceUtils
                .getResponseAsync(url, null, "GET")
                .thenApply(jsonStr -> {
                    // parseamos el JSON completo
                    JsonObject root = JsonParser.parseString(jsonStr).getAsJsonObject();
                    // opcional: comprobar ok == true
                    if (!root.get("ok").getAsBoolean()) {
                        throw new RuntimeException("API error: " + root.get("error"));
                    }
                    // obtenemos el objeto dentro de "resultado"
                    JsonObject physioObj = root.getAsJsonObject("resultado");
                    // lo convertimos a nuestro modelo Physio
                    return gson.fromJson(physioObj, PhysioResponse.class);
                });
    }

    public enum Method { POST, PUT, DELETE }

    private Method method;
    private Physio physio;
    private String id;

    /**
     * Configures the service for POST or PUT operations.
     * @param method The HTTP method to use.
     * @param physio The Physio object to send.
     */
    public void configureFor(Method method, Physio physio) {
        this.method = method;
        this.physio = physio;
    }

    /**
     * Configures the service for a DELETE operation.
     * @param id The ID of the Physio to delete.
     */
    public void configureForDelete(String id) {
        this.method = Method.DELETE;
        this.id = id;
    }

    /**
     * Creates the asynchronous task to execute the selected HTTP operation.
     *
     * @return A Task that returns a BaseResponse from the API.
     */
    @Override
    protected Task<BaseResponse> createTask() {
        return new Task<>() {
            @Override
            protected BaseResponse call() throws Exception {
                Gson gson = new Gson();
                String jsonBody = (physio != null) ? gson.toJson(physio) : null;
                String url = ServiceUtils.API_URL + "/physios";

                return switch (method) {
                    case POST -> gson.fromJson(ServiceUtils.getResponse(url, jsonBody, "POST"), BaseResponse.class);
                    case PUT -> gson.fromJson(ServiceUtils.getResponse(url + "/" + physio.getId(), jsonBody, "PUT"), BaseResponse.class);
                    case DELETE -> gson.fromJson(ServiceUtils.getResponse(url + "/" + id, null, "DELETE"), BaseResponse.class);
                };
            }
        };
    }

    public static CompletableFuture<PhysioResponse> getPhysio(String id) {
        CompletableFuture<PhysioResponse> future = new CompletableFuture<>();
       future = ServiceUtils.getResponseAsync(
                ServiceUtils.API_URL + "/physios/" + id,
                null,
                "GET"
        ).thenApply(json -> new Gson().fromJson(json, PhysioResponse.class));
       while (!future.isDone()) {
            try {
                Thread.sleep(100); // Polling delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                future.completeExceptionally(e);
            }
        }
        return future;
    }
}
