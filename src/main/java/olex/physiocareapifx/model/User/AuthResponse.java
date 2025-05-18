package olex.physiocareapifx.model.User;

public class AuthResponse {
    private String token;
    private String rol;
    private String id;


    public String getToken() {
        return token;
    }

    public String getRol() {
        return rol;
    }

    public String getId() {
        return id;
    }

    public boolean isOk() {
        return token != null && !token.isEmpty();
    }
}
