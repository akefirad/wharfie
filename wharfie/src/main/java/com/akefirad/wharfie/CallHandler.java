package com.akefirad.wharfie;

public interface CallHandler<T> {
    void succeeded(T response);

    void failed(Throwable exception);
}
