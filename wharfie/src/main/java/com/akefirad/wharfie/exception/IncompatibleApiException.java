package com.akefirad.wharfie.exception;

/**
 * 404 HTTP Error
 * Incompatible Docker Registry API exception. This exception is thrown
 * when the registry is not supporting version 2 (supported by this library).
 */
public class IncompatibleApiException extends RegistryException {
    public IncompatibleApiException ( String baseUrl ) {
        super(baseUrl + " does not support REST API Version 2!");
    }
}
