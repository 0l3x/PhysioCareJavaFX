package olex.physiocareapifx.model.Records;

import olex.physiocareapifx.model.BaseResponse;

import java.util.List;

public class RecordListResponse extends BaseResponse {
    private List<Record> resultado;

    public List<Record> getRecords(){return resultado;}
}
