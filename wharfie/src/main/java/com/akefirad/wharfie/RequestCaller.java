package com.akefirad.wharfie;

import com.akefirad.wharfie.exceptions.*;
import com.akefirad.wharfie.payloads.EntityResponse;
import com.akefirad.wharfie.payloads.ErrorsResponse;
import com.akefirad.wharfie.payloads.ErrorsResponse.Error;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.json.*;
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
    public <T extends EntityResponse> T execute ( Call<T> call ) {
        try {
            Request request = call.request();
            Response<T> response = call.execute();
            Map<String, List<String>> headers = getHeaders(response);

            if (response.isSuccessful()) {
                validateApiVersion2(headers, request);
                T entity = response.body();
                assert entity != null : "Unexpected NULL body!";

                entity.setHeaders(headers);
                return entity;
            }
            else {
                throw createFailedRequestException(request, response, headers);
            }
        }
        catch (IOException e) {
            throw new RegistryIOException(e);
        }
    }

    private FailedRequestException createFailedRequestException ( Request request, Response<?> response,
                                                                  Map<String, List<String>> headers )
            throws IOException {
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
