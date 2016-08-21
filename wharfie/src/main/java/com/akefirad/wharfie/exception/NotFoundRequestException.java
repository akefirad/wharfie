package com.akefirad.wharfie.exception;

import com.akefirad.wharfie.payload.ErrorsResponse;
import okhttp3.Request;

/**
 * 404 HTTP Error
 */
public class NotFoundRequestException extends FailedRequestException {
    public NotFoundRequestException (String message, ErrorsResponse errors) {
        super(message, 404, errors);
    }

    public NotFoundRequestException (Request request, ErrorsResponse errors) {
        super("Not found request: " + request.url(), 404, errors);
    }
}
