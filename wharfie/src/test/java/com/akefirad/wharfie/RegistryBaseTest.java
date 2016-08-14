package com.akefirad.wharfie;

import com.akefirad.wharfie.exceptions.*;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.*;

import static com.akefirad.wharfie.TestUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class RegistryBaseTest {
    private MockWebServer server;
    private DockerRegistry registry;

    @Before
    public void setUp () throws Exception {
        server = newServer();
        registry = newRegistry();
    }

    @After
    public void tearDown () throws Exception {
        server.shutdown();
    }

    @Test
    public void getBase200Test () throws Exception {
        server.enqueue(newResponse("{}"));

        RegistryBase base = registry.getBase();

        assertThat(base, is(notNullValue()));
        assertThat(base.getVersion(), equalTo("v2"));
    }

    @Test
    public void getBase401Test () throws Exception {
        server.enqueue(newResponse(401, "{\"errors\":[{\"code\":\"UNAUTHORIZED\"," +
                "\"message\":\"authentication required\",\"detail\":null}]}"));
        try {
            RegistryBase base = registry.getBase();
        }
        catch (UnauthorizedRequestException e) {
            assertThat(e.getErrors().list().size(), is(1));
            assertThat(e.getErrors().list().get(0).getCode(), equalTo("UNAUTHORIZED"));
            assertThat(e.getErrors().list().get(0).getMessage(), equalTo("authentication required"));
            assertThat(e.getErrors().list().get(0).getDetail(), equalTo("null"));
        }
    }

    @Test
    public void getBase404Test () throws Exception {
        server.enqueue(newResponse(404, "{}"));

        try {
            RegistryBase base = registry.getBase();
        }
        catch (IncompatibleApiException ignored) {
        }
    }

    @Test
    public void getBase500Test () throws Exception {
        server.enqueue(newResponse(500, "INVALID BODY"));

        try {
            RegistryBase base = registry.getBase();
        }
        catch (FailedRequestException e) {
            assertThat(e.getErrors().list().size(), is(1));
        }
    }

    @Test
    public void getBaseInvalidBodyTest () throws Exception {
        server.enqueue(newResponse("INVALID BODY"));

        try {
            RegistryBase base = registry.getBase();
        }
        catch (RegistryIOException e) {
        }
    }
}