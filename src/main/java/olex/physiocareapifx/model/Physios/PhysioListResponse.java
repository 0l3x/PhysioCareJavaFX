package olex.physiocareapifx.model.Physios;

import olex.physiocareapifx.model.BaseResponse;

import java.util.List;

public class PhysioListResponse extends BaseResponse {
    private List<Physio> resultado;

    public List<Physio> getPhysios() {
        return resultado;
    }
}