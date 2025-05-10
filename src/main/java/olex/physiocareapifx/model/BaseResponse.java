package olex.physiocareapifx.model;

public class BaseResponse {
    private boolean ok;
    private String error;

    public boolean isOk() {
        return ok;
    }

    public String getError() {
        return error;
    }
}
