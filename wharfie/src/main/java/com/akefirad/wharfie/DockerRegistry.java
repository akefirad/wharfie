package com.akefirad.wharfie;

import com.akefirad.wharfie.call.*;
import com.akefirad.wharfie.exception.*;
import com.akefirad.wharfie.payload.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;

import static com.akefirad.wharfie.ApiConstants.Statuses.NOT_FOUND;
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
            BaseResponse base = getRequestHandler().execute(registryRestApi.getBase());
            return processResponse(base);
        }
        catch (FailedRequestException e) {
            throw (e.getCode() != NOT_FOUND) ? e :
                    new IncompatibleApiException(retrofit.baseUrl().url().toString());
        }
    }

    //-----------------------------------------------------------------------------------
    public <T extends RegistryBase> void getBase (EntityCallback<RegistryBase> callback)
            throws RegistryException {
        getRequestHandler().execute(registryRestApi.getBase(),
                new ResponseCallback<BaseResponse>() {
                    @Override
                    public void succeeded (BaseResponse response) {
                        callback.succeeded(processResponse(response));
                    }

                    @Override
                    public void failed (RegistryException exception) {
                        callback.failed(exception);
                    }
                });
    }

    //-----------------------------------------------------------------------------------
    private RegistryBase processResponse (BaseResponse base) {
        return new RegistryBase(this, base.getVersion());
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
