package com.akefirad.wharfie.exceptions;

import com.akefirad.wharfie.payloads.ErrorsResponse;
import okhttp3.Request;

/**
 * 401 HTTP Error
 */
public class UnauthorizedRequestException extends FailedRequestException {
    public UnauthorizedRequestException ( String message, ErrorsResponse errors ) {
        super(message, 401, errors);
    }

    public UnauthorizedRequestException ( Request request, ErrorsResponse errors ) {
        super("Unauthorized request: " + request.url(), 401, errors);
    }
}
