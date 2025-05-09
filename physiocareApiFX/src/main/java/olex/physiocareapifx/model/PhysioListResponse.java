package olex.physiocareapifx.model;

import java.util.List;

public class PhysioListResponse extends BaseResponse {
    private List<Physio> resultado;

    public List<Physio> getPhysios() {
        return resultado;
    }
}