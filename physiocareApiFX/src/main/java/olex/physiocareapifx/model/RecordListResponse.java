package olex.physiocareapifx.model;

import java.util.List;

public class RecordListResponse extends BaseResponse {
    private List<Record> resultado;

    public List<Record> getRecords(){return resultado;}
}
