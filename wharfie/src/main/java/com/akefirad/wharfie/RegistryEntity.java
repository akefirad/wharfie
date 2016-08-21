package com.akefirad.wharfie;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public abstract class RegistryEntity {
    private final RegistryEntity parent;

    public RegistryEntity (RegistryEntity parent) {
        this.parent = parent;
    }

    RegistryEntity getParent () {
        return parent;
    }

    DockerRegistry getRegistry () {
        return getParent().getRegistry();
    }

    @Override
    public String toString () {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
