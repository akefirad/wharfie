package com.akefirad.wharfie.call;

import com.akefirad.wharfie.exception.RegistryException;

public interface ResponseCallback<T> {
    void succeeded (T response);

    void failed (RegistryException exception);
}
