package olex.physiocareapifx.services;

import com.google.gson.Gson;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import olex.physiocareapifx.model.BaseResponse;
import olex.physiocareapifx.model.Patient;
import olex.physiocareapifx.utils.ServiceUtils;

/**
 * A JavaFX Service for performing asynchronous CRUD operations on patients.
 * It supports POST, PUT and DELETE methods to communicate with the REST API.
 */
public class PatientService extends Service<BaseResponse> {
    /**
     * Enum representing the HTTP method to use.
     */
    public enum Method { POST, PUT, DELETE }

    private Method method;
    private Patient patient;
    private String id;

    /**
     * Configures the service for a POST or PUT operation.
     *
     * @param method  The HTTP method to use (POST or PUT).
     * @param patient The patient object to send in the request body.
     */
    public void configureFor(Method method, Patient patient) {
        this.method = method;
        this.patient = patient;
    }

    /**
     * Configures the service for a DELETE operation.
     *
     * @param id The ID of the patient to delete.
     */
    public void configureForDelete(String id) {
        this.method = Method.DELETE;
        this.id = id;
    }

    /**
     * Creates the background task that performs the HTTP request.
     *
     * @return A Task that returns a BaseResponse containing the API response.
     */
    @Override
    protected Task<BaseResponse> createTask() {
        return new Task<>() {
            @Override
            protected BaseResponse call() throws Exception {
                Gson gson = new Gson();
                String jsonBody = (patient != null) ? gson.toJson(patient) : null;
                String url = ServiceUtils.API_URL + "/patients";

                switch (method) {
                    case POST:
                        return gson.fromJson(ServiceUtils.getResponse(url, jsonBody, "POST"), BaseResponse.class);
                    case PUT:
                        return gson.fromJson(ServiceUtils.getResponse(url + "/" + patient.getId(), jsonBody, "PUT"), BaseResponse.class);
                    case DELETE:
                        return gson.fromJson(ServiceUtils.getResponse(url + "/" + id, null, "DELETE"), BaseResponse.class);
                    default:
                        throw new IllegalStateException("Método no soportado: " + method);
                }
            }
        };
    }
}

