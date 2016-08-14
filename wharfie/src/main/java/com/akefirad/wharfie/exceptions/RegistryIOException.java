package com.akefirad.wharfie.exceptions;

/**
 * The Wrapper exception for IOException while working with
 * the streams of requests and responses.
 */
public class RegistryIOException extends RegistryException {
    public RegistryIOException ( Throwable cause ) {
        super(cause);
    }
}
