package com.akefirad.wharfie.call;

import com.akefirad.wharfie.RegistryEntity;
import com.akefirad.wharfie.exception.RegistryException;

public interface EntityCallback<T extends RegistryEntity> {
    void succeeded(T entity);

    void failed(RegistryException exception);
}
