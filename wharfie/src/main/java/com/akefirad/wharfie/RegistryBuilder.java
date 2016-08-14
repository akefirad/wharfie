package com.akefirad.wharfie;

import okhttp3.OkHttpClient;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import static com.akefirad.wharfie.utils.Asserts.notNull;

public class RegistryBuilder {
    private String baseUrl;
    private OkHttpClient client;
    private Executor executor;
    private List<Converter.Factory> factories;

    public RegistryBuilder () {
        factories = new ArrayList<>();
    }

    public RegistryBuilder baseUrl ( String baseUrl ) {
        this.baseUrl = baseUrl;
        return this;
    }

    public RegistryBuilder client ( OkHttpClient client ) {
        this.client = client;
        return this;
    }

    public RegistryBuilder executor ( Executor executor ) {
        this.executor = executor;
        return this;
    }

    public RegistryBuilder addConverterFactory ( Converter.Factory factory ) {
        this.factories.add(factory);
        return this;
    }

    public DockerRegistry build () {
        notNull(baseUrl, baseUrl);

        Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl(baseUrl);

        if (client != null)
            builder.client(client);

        if (executor != null)
            builder.callbackExecutor(executor);

        factories.forEach(builder::addConverterFactory);
        return new DockerRegistry(builder.build());
    }
}
