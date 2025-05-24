package olex.physiocareapifx.utils;

import com.google.gson.Gson;
import olex.physiocareapifx.model.User.AuthResponse;
import olex.physiocareapifx.model.User.LoginRequest;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.zip.GZIPInputStream;

/**
 * Utility class that handles HTTP communication with the REST API.
 * Provides a static method to send requests and retrieve responses.
 */
public class ServiceUtils {
    /**
     * Base URL of the API. Can be changed to point to different environments (e.g., localhost, VPS).
     */
    //public static final String API_URL = "http://olexanderg.net:8080";
    public static final String API_URL = "https://hectorrp.com/api";
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


    // Get charset encoding (UTF-8, ISO,...)
    public static String getCharset(String contentType) {
        for (String param : contentType.replace(" ", "").split(";")) {
            if (param.startsWith("charset=")) {
                return param.split("=", 2)[1];
            }
        }

        return null; // Probably binary content
    }

    public static String getResponseCompletable(String url, String data, String method) {
        BufferedReader bufInput = null;
        StringJoiner result = new StringJoiner("\n");
        System.out.println("URL enviada: " + url);
        try {
            URL urlConn = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlConn.openConnection();
            conn.setReadTimeout(20000 /*milliseconds*/);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod(method);


            //conn.setRequestProperty("Host", "localhost");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Accept-Encoding", "gzip,deflate,sdch");
            conn.setRequestProperty("Accept-Language", "es-ES,es;q=0.8");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36");

            // token para testing // PDF Utils 18/05
           //TokenManager.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJsb2dpbiI6ImhlY3RvcjIiLCJyb2wiOiJhZG1pbiIsImlkIjoiNjdmM2ZlMzg5NmI0OWIxODkyYjE4MmQ2IiwiaWF0IjoxNzQ4MDg2MzA0LCJleHAiOjE3NDgwOTM1MDR9.RtnMec_Nu0u55mBy-QsOzDJqSUgnEBlvL5Bs_83EzOo");


            // If set, send the authentication token
            if(TokenManager.getToken() != null) {
                System.out.println("Token: " + TokenManager.getToken());
                conn.setRequestProperty("Authorization", "Bearer " + TokenManager.getToken());
            }

            if (data != null) {
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
                conn.setDoOutput(true);
                //Send request
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.write(data.getBytes());
                wr.flush();
                wr.close();
            }

            String charset = getCharset(conn.getHeaderField("Content-Type"));

            if (charset != null) {
                int responseCode = conn.getResponseCode();
                InputStream input;
                if (responseCode >= 200 && responseCode < 399) {
                    input = conn.getInputStream(); // respuesta correcta
                } else {
                    input = conn.getErrorStream(); // respuesta con error
                }
                if ("gzip".equals(conn.getContentEncoding())) {
                    input = new GZIPInputStream(input);
                }

                bufInput = new BufferedReader(
                        new InputStreamReader(input));

                String line;
                while((line = bufInput.readLine()) != null) {
                    result.add(line);
                }
                System.out.println("Response code: " + responseCode);
                System.out.println("Response: " + result);
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (bufInput != null) {
                try {
                    bufInput.close();
                } catch (IOException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }

        return result.toString();
    }

    public static CompletableFuture<String> getResponseAsync(String url, String data, String method) {
        return CompletableFuture.supplyAsync(() -> getResponseCompletable(url, data, method));
    }

    public static boolean login(String user, String password){
        try{
            String credentials = new Gson().toJson(new LoginRequest(user, password));
            System.out.println(credentials);
            String jsonResponse = getResponse(API_URL+"/auth/login", credentials,"POST");

            AuthResponse authResponse = new Gson().fromJson(jsonResponse, AuthResponse.class);
            if(authResponse != null && authResponse.isOk()){
                TokenManager.setToken(authResponse.getToken());
                return true;

            }
            return false;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
