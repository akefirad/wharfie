package com.akefirad.wharfie.exception;

import com.akefirad.wharfie.payload.ErrorsResponse;
import okhttp3.Request;

/**
 * 401 HTTP Error
 * This exception is thrown when the response has 401 status code
 * and the response body indicates "insufficient_scope" as error
 */
public class InsufficientScopeException extends UnauthorizedRequestException {
    public InsufficientScopeException (String message, ErrorsResponse errors) {
        super(message, errors);
    }
}
