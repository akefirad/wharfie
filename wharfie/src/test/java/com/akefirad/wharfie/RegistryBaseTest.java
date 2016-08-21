package com.akefirad.wharfie;

import com.akefirad.wharfie.exception.*;
import com.akefirad.wharfie.payload.ErrorsResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.*;

import java.util.List;

import static com.akefirad.wharfie.ApiConstants.Statuses.*;
import static com.akefirad.wharfie.TestUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
        server.enqueue(newResponse(BASE));

        RegistryBase base = registry.getBase();

        assertThat(base, is(notNullValue()));
        assertThat(base.getVersion(), equalTo("v2"));
    }

    @Test
    public void getBase401Test () throws Exception {
        server.enqueue(newResponse(UNAUTHORIZED, "{\"errors\":[{\"code\":" +
                "\"UNAUTHORIZED\",\"message\":\"authentication required\"," +
                "\"detail\":null}]}"));
        try {
            RegistryBase base = registry.getBase();
            fail("Expecting UnauthorizedRequestException!");
        }
        catch (UnauthorizedRequestException e) {
            List<ErrorsResponse.Error> errors = e.getErrors().list();
            assertThat(errors.size(), is(1));
            assertThat(errors.get(0).getCode(), equalTo("UNAUTHORIZED"));
            assertThat(errors.get(0).getMessage(), equalTo("authentication required"));
            assertThat(errors.get(0).getDetail(), equalTo("null"));
        }
    }

    @Test
    public void getBase404Test () throws Exception {
        server.enqueue(newResponse(NOT_FOUND, EMPTY_JSON));

        try {
            RegistryBase base = registry.getBase();
            fail("Expecting IncompatibleApiException!");
        }
        catch (IncompatibleApiException e) {
            assertThat(e.getMessage(),
                    containsString("does not support REST API Version 2!"));
        }
    }

    @Test
    public void getBase500Test () throws Exception {
        server.enqueue(newResponse(INTERNAL_ERROR, "Internal Error"));

        try {
            RegistryBase base = registry.getBase();
            fail("Expecting FailedRequestException!");
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
            fail("Expecting RegistryIOException!");
        }
        catch (RegistryIOException ignored) {
        }
    }
}