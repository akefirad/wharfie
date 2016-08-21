package com.akefirad.wharfie.call;

import com.akefirad.wharfie.RegistryBase;
import com.akefirad.wharfie.exception.RegistryException;

public interface BaseCallback extends EntityCallback<RegistryBase> {
    void succeeded (RegistryBase base);

    void failed (RegistryException exception);
}
