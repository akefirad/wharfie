package com.akefirad.wharfie.exceptions;

import com.akefirad.wharfie.payloads.ErrorsResponse;
import okhttp3.Request;

/**
 * 500 HTTP Error
 * Base class of all failed request exceptions
 */
public class FailedRequestException extends RegistryException {
    private final int code;
    private final ErrorsResponse errors;

    public FailedRequestException ( String message, int code, ErrorsResponse errors ) {
        super(message);

        this.code = code;
        this.errors = errors.copy();
    }

    public FailedRequestException ( Request request, int code, ErrorsResponse errors ) {
        super("Failed request: " + request.url());

        this.code = code;
        this.errors = errors.copy();
    }

    public int getCode () {
        return code;
    }

    public ErrorsResponse getErrors () {
        return errors;
    }
}
