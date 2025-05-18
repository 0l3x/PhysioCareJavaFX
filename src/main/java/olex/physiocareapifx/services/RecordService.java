package olex.physiocareapifx.services;

import com.google.gson.Gson;
import olex.physiocareapifx.model.Records.RecordResponse;
import olex.physiocareapifx.utils.ServiceUtils;

import java.util.concurrent.CompletableFuture;

public class RecordService {
    private static final Gson gson = new Gson();

    public static CompletableFuture<RecordResponse> getRecordById(String id) {
        return ServiceUtils.getResponseAsync(
                ServiceUtils.API_URL + "records/" + id,
                null,
                "GET"
        ).thenApply(response -> gson.fromJson(response, RecordResponse.class));
    }
}
