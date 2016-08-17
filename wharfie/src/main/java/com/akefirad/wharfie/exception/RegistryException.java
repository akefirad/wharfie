package com.akefirad.wharfie.exception;

/**
 * Base class of all exception thrown by this library
 */
public class RegistryException extends RuntimeException {
    public RegistryException ( String message ) {
        super(message);
    }

    public RegistryException ( Throwable cause ) {
        super(cause);
    }
}
