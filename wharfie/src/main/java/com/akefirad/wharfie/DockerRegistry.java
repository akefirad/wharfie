package com.akefirad.wharfie;

import com.akefirad.wharfie.exceptions.*;
import com.akefirad.wharfie.payloads.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;

import static com.akefirad.wharfie.utils.Asserts.notNull;

public class DockerRegistry {
    private static final Logger logger = LoggerFactory.getLogger(DockerRegistry.class);

    private final Retrofit retrofit;
    private final RegistryRestApi registryRestApi;
    private final RequestCaller requestCaller;

    public DockerRegistry ( Retrofit retrofit ) {
        notNull(retrofit, "retrofit");

        this.retrofit = retrofit;
        this.registryRestApi = retrofit.create(RegistryRestApi.class);
        this.requestCaller = new RequestCaller();
    }

    //-----------------------------------------------------------------------------------
    public RegistryBase getBase () throws RegistryException {
        try {
            BaseResponse base = getRequestCaller().execute(registryRestApi.getBase());
            return new RegistryBase(this, base.getVersion());
        }
        catch (FailedRequestException e) {
            throw (e.getCode() != 404) ? e :
                    new IncompatibleApiException(retrofit.baseUrl().url().toString());
        }
    }

    //-----------------------------------------------------------------------------------
    @Override
    public String toString () {
        return retrofit.baseUrl().url().toString();
    }

    //-----------------------------------------------------------------------------------
    RequestCaller getRequestCaller () {
        return requestCaller;
    }

    //-----------------------------------------------------------------------------------
    RegistryRestApi getRegistryRestApi () {
        return registryRestApi;
    }

    //-----------------------------------------------------------------------------------
    public static RegistryBuilder defaultBuilder () {
        return new RegistryBuilder();
    }
}
