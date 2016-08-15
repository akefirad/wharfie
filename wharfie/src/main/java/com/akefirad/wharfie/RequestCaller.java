package com.akefirad.wharfie;

import com.akefirad.wharfie.exceptions.*;
import com.akefirad.wharfie.payloads.EntityResponse;
import com.akefirad.wharfie.payloads.ErrorsResponse;
import com.akefirad.wharfie.payloads.ErrorsResponse.Error;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.StreamSupport;

import static com.akefirad.wharfie.ApiConstants.ErrorCodes.INVALID_JSON_ERRORS;
import static com.akefirad.wharfie.ApiConstants.Labels.*;
import static com.akefirad.wharfie.utils.Asserts.validateApiVersion2;
import static com.akefirad.wharfie.utils.WharfieUtils.getHeaders;
import static java.util.Collections.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;

class RequestCaller {
    public <T extends EntityResponse> void execute ( Call<T> call, CallHandler<T> handler ) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                Request request = call.request();
                try {
                    handler.succeeded(process(request, response));
                }
                catch (RegistryException e) {
                    handler.failed(e);
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                handler.failed(t);
            }
        });
    }

    public <T extends EntityResponse> T execute ( Call<T> call ) {
        try {
            Request request = call.request();
            Response<T> response = call.execute();
            return process(request, response);
        }
        catch (IOException e) {
            throw new RegistryIOException(e);
        }
    }

    private <T extends EntityResponse> T process(Request request, Response<T> response) {
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
