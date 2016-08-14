package com.akefirad.wharfie.exceptions;

import com.akefirad.wharfie.payloads.ErrorsResponse;
import okhttp3.Request;

/**
 * 404 HTTP Error
 */
public class NotFoundRequestException extends FailedRequestException {
    public NotFoundRequestException ( String message, ErrorsResponse errors ) {
        super(message, 404, errors);
    }

    public NotFoundRequestException ( Request request, ErrorsResponse errors ) {
        super("Not found request: " + request.url(), 404, errors);
    }
}
