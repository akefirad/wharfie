package com.akefirad.wharfie;

import com.akefirad.wharfie.call.*;
import com.akefirad.wharfie.exception.*;
import com.akefirad.wharfie.payload.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;

import static com.akefirad.wharfie.util.Asserts.notNull;

public class DockerRegistry {
    private static final Logger logger = LoggerFactory.getLogger(DockerRegistry.class);

    private final Retrofit retrofit;
    private final RegistryRestApi registryRestApi;
    private final RequestHandler requestHandler;

    public DockerRegistry (Retrofit retrofit) {
        notNull(retrofit, "retrofit");

        this.retrofit = retrofit;
        this.registryRestApi = retrofit.create(RegistryRestApi.class);
        this.requestHandler = new RequestHandler();
    }

    //-----------------------------------------------------------------------------------
    public RegistryBase getBase () throws RegistryException {
        try {
            return processResponse(this, getRequestHandler().execute(registryRestApi.getBase()));
        }
        catch (FailedRequestException e) {
            throw (e.getCode() != 404) ? e :
                    new IncompatibleApiException(retrofit.baseUrl().url().toString());
        }
    }

    //-----------------------------------------------------------------------------------
    public <T extends RegistryBase> void getBase (BaseCallback callback) throws RegistryException {
        DockerRegistry registry = this;
        getRequestHandler().execute(registryRestApi.getBase(), new ResponseCallback<BaseResponse>() {
            @Override
            public void succeeded (BaseResponse response) {
                callback.succeeded(processResponse(registry, getRequestHandler().execute(registryRestApi.getBase())));
            }

            @Override
            public void failed (RegistryException exception) {
                callback.failed(exception);
            }
        });
    }

    //-----------------------------------------------------------------------------------
    private RegistryBase processResponse (DockerRegistry registry, BaseResponse base) {
        return new RegistryBase(registry, base.getVersion());
    }

    //-----------------------------------------------------------------------------------
    @Override
    public String toString () {
        return retrofit.baseUrl().url().toString();
    }

    //-----------------------------------------------------------------------------------
    RequestHandler getRequestHandler () {
        return requestHandler;
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
