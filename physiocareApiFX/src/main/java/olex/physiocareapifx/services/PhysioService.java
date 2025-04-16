package olex.physiocareapifx.services;

import com.google.gson.Gson;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import olex.physiocareapifx.model.BaseResponse;
import olex.physiocareapifx.model.Physio;
import olex.physiocareapifx.utils.ServiceUtils;

/**
 * Service class to handle asynchronous operations (POST, PUT, DELETE) for Physios.
 */
public class PhysioService extends Service<BaseResponse> {
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
}
