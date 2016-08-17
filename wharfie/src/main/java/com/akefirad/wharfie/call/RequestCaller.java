package com.akefirad.wharfie.call;

import com.akefirad.wharfie.call.ResponseCallback;
import com.akefirad.wharfie.exception.*;
import com.akefirad.wharfie.payload.EntityResponse;
import com.akefirad.wharfie.payload.ErrorsResponse;
import com.akefirad.wharfie.payload.ErrorsResponse.Error;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.json.*;
import retrofit2.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.StreamSupport;

import static com.akefirad.wharfie.ApiConstants.ErrorCodes.INVALID_JSON_ERRORS;
import static com.akefirad.wharfie.ApiConstants.Labels.*;
import static com.akefirad.wharfie.util.Asserts.validateApiVersion2;
import static com.akefirad.wharfie.util.WharfieUtils.getHeaders;
import static java.util.Collections.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class RequestCaller {
    public <T extends EntityResponse> void execute ( Call<T> call, ResponseCallback<T> callback ) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                Request request = call.request();
                try {
                    callback.succeeded(processResponse(request, response));
                }
                catch (RegistryException e) {
                    callback.failed(e);
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                callback.failed(new RegistryException(t));
            }
        });
    }

    public <T extends EntityResponse> T execute ( Call<T> call ) {
        try {
            Request request = call.request();
            Response<T> response = call.execute();
            return processResponse(request, response);
        }
        catch (IOException e) {
            throw new RegistryIOException(e);
        }
    }

    private <T extends EntityResponse> T processResponse(Request request, Response<T> response) {
        Map<String, List<String>> headers = getHeaders(response);

        if (response.isSuccessful()) {
            validateApiVersion2(headers, request);
            T entity = response.body();
            assert entity != null : "Unexpected NULL body!";

            entity.setHeaders(headers);
            return entity;
        }
        else {
            throw failedRequestException(request, response, headers);
        }
    }

    private FailedRequestException failedRequestException(Request request, Response<?> response,
                                                          Map<String, List<String>> headers ) {
        try {
            ResponseBody body = response.errorBody();
            List<Error> list = readErrorsFromJson(body != null ? body.string() : EMPTY);
            ErrorsResponse errors = new ErrorsResponse(headers, list);

            switch (response.code()) {
                case 401:
                    return new UnauthorizedRequestException(request, errors);
                case 404:
                    return new NotFoundRequestException(request, errors);
                default:
                    return new FailedRequestException(request, response.code(), errors);
            }
        } catch (IOException e) {
            throw new RegistryIOException(e);
        }
    }

    private List<Error> readErrorsFromJson ( String string ) {
        try {
            JSONObject json = new JSONObject(string);
            JSONArray array = json.getJSONArray(ERRORS);
            List<Error> list = new ArrayList<>(array.length());
            StreamSupport.stream(array.spliterator(), false)
                    .map(error -> (JSONObject) error)
                    .forEach(error -> list.add(new Error(
                            error.getString(CODE),
                            error.getString(MESSAGE),
                            String.valueOf(error.get(DETAIL)))));
            return unmodifiableList(list);
        }
        catch (ClassCastException | JSONException e) {
            return singletonList(new Error(INVALID_JSON_ERRORS, e.getMessage(), string));
        }
    }
}
