package com.akefirad.wharfie.payload;

import static com.akefirad.wharfie.util.Asserts.notBlank;

/**
 * Payload of the base entity
 */
public class BaseResponse extends EntityResponse {
    private String version;

    public BaseResponse () {
        this.version = "v2";
    }

    public String getVersion () {
        return version;
    }

    public void setVersion (String version) {
        notBlank(version, "version");

        this.version = version;
    }
}
