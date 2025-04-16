package olex.physiocareapifx.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Utility class that handles HTTP communication with the REST API.
 * Provides a static method to send requests and retrieve responses.
 */
public class ServiceUtils {
    /**
     * Base URL of the API. Can be changed to point to different environments (e.g., localhost, VPS).
     */
    public static final String API_URL = "http://olexanderg.net:8080";

    /**
     * Sends an HTTP request to the specified URL with the given data and method,
     * and returns the response as a String.
     *
     * Automatically adds "Content-Type: application/json" and the Authorization
     * header if a token is present via TokenManager.
     *
     * @param url    The full URL to which the request is sent.
     * @param data   The JSON string to send as request body (optional; null for GET/DELETE).
     * @param method The HTTP method to use ("GET", "POST", "PUT", "DELETE").
     * @return The response from the API as a JSON-formatted string.
     * @throws IOException If the request fails due to network or stream errors.
     */
    public static String getResponse(String url, String data, String method) throws IOException {
        System.out.println("URL enviada: " + url);
        System.out.println("DATA ENVIADA: " + data);
        System.out.println("METHOD ENVIADO: " + method);
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

        conn.setRequestMethod(method);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");

        // Add Authorization header if token is available
        if (TokenManager.getToken() != null) {
            conn.setRequestProperty("Authorization", "Bearer " + TokenManager.getToken());
        }

        // Send request body if present
        if (data != null && !data.isEmpty()) {
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = data.getBytes("UTF-8");
                os.write(input, 0, input.length);
            }
        }

        // Choose input stream based on response code
        int status = conn.getResponseCode();
        InputStream input = (status >= 200 && status < 400) ? conn.getInputStream() : conn.getErrorStream();

        // Read and return the response
        BufferedReader br = new BufferedReader(new InputStreamReader(input, "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            response.append(line.trim());
        }

        return response.toString();
    }
}
