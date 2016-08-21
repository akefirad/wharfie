package com.akefirad.wharfie;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.slf4j.bridge.SLF4JBridgeHandler;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.*;
import java.util.logging.*;

import static com.akefirad.wharfie.ApiConstants.Headers.*;

public final class TestUtils {
    public static final String EMPTY_JSON = "{}";
    public static final String BASE = "{}";

    static {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        Logger.getLogger("global").setLevel(Level.FINEST);
    }

    private static final Gson GSON = new Gson();
    private static final int PORT = 5555;
    private static final String HOST = "localhost";
    private static final String PROTOCOL = "http";
    private static final String BASE_URL = PROTOCOL + "://" + HOST + ":" + PORT + "/";

    private TestUtils () {
    }

    public static MockWebServer newServer () throws IOException {
        return newServer(PORT);
    }

    public static MockWebServer newServer (int port) throws IOException {
        MockWebServer server = new MockWebServer();
        server.start(port);

        return server;
    }

    public static MockResponse newResponse (String body) {
        return newResponse(200, body);
    }

    public static MockResponse newResponse (int code, String body) {
        return new MockResponse()
                .setHeader(CONTENT_TYPE, CONTENT_TYPE_JSON_UTF8)
                .setHeader(DOCKER_DISTRIBUTION_API_VERSION, REGISTRY_2_0)
                .setResponseCode(code)
                .setBody(body);
    }

    public static DockerRegistry newRegistry () {
        return DockerRegistry.defaultBuilder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static DockerRegistry newRegistry (OkHttpClient client) {
        return DockerRegistry.defaultBuilder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static Repositories newRepositories (Collection<String> names) {
        return new Repositories(names);
    }

    public static String toJson (Object object) {
        return GSON.toJson(object);
    }

    public static class Repositories {
        private final List<String> repositories;

        private Repositories (Collection<String> repositories) {
            this.repositories = new ArrayList<>(repositories);
        }
    }
}
