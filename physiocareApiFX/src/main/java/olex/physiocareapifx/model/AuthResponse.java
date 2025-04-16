package olex.physiocareapifx.model;

public class AuthResponse {
    private String token;
    private String rol;

    public String getToken() {
        return token;
    }

    public String getRol() {
        return rol;
    }

    public boolean isOk() {
        return token != null && !token.isEmpty();
    }
}
