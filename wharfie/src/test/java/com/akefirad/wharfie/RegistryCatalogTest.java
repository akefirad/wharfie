package com.akefirad.wharfie;

import com.akefirad.wharfie.ApiConstants.Headers;
import com.akefirad.wharfie.exception.InsufficientScopeException;
import okhttp3.mockwebserver.MockWebServer;
import org.json.JSONArray;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

import static com.akefirad.wharfie.TestUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class RegistryCatalogTest {
    private static final Logger logger = LoggerFactory.getLogger(RegistryCatalogTest.class);

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
    public void getCatalog200Test () throws Exception {
        server.enqueue(newResponse("{}")); // Add base
        server.enqueue(newResponse("{\"repositories\":[\"foo\",\"bar\"]}"));

        RegistryCatalog catalog = registry.getBase().getCatalog();

        assertThat(catalog.getRepositories().size(), equalTo(2));
        assertThat(catalog.getRepositories().get(0).getName(), anyOf(equalTo("foo"), equalTo("bar")));
        assertThat(catalog.getRepositories().get(1).getName(), anyOf(equalTo("foo"), equalTo("bar")));
    }

    @Test
    public void getCatalog200Empty1Test () throws Exception {
        server.enqueue(newResponse("{}")); // Add base
        server.enqueue(newResponse("{}")); // Add catalog

        RegistryCatalog catalog = registry.getBase().getCatalog();

        assertThat(catalog.getRepositories(), equalTo(Collections.emptyList()));
    }

    @Test
    public void getCatalog200Empty2Test () throws Exception {
        server.enqueue(newResponse("{}")); // Add base
        server.enqueue(newResponse("{\"repositories\":[]}"));

        RegistryCatalog catalog = registry.getBase().getCatalog();

        assertThat(catalog.getRepositories(), equalTo(Collections.emptyList()));
    }

    @Test
    public void getCatalog200WithLinkRelNextTest () throws Exception {
        server.enqueue(newResponse("{}")); // Add base
        server.enqueue(newResponse("{\"repositories\":[\"foo\"]}")
                .setHeader(Headers.LINK, "</v2/_catalog?last=foo&n=1>; rel=\"next\""));

        RegistryCatalog catalog = registry.getBase().getCatalog(1);

        assertThat(catalog.getNumber(), is(1));
        assertThat(catalog.getLast().getName(), equalTo("foo"));
        assertThat(catalog.getRepositories().size(), equalTo(1));
        assertThat(catalog.getRepositories().get(0).getName(), equalTo("foo"));
    }

    @Test
    public void getCatalog401InsufficientTest () throws Exception {
        server.enqueue(newResponse("{}")); // Add base
        server.enqueue(newResponse(401, "{\"errors\":[{\"code\":\"UNAUTHORIZED\",\"message\":\"authentication required\"," +
                "\"detail\":[{\"Type\":\"registry\",\"Name\":\"catalog\",\"Action\":\"*\"}]}]}")
                .addHeader(Headers.WWW_AUTHENTICATE, "Www-Authenticate: Bearer realm=\"https://auth.docker.io/token\"," +
                        "service=\"registry.docker.io\",scope=\"registry:catalog:*\",error=\"insufficient_scope\""));

        try {
            RegistryCatalog catalog = registry.getBase().getCatalog();
        }
        catch (InsufficientScopeException e) {
            assertThat(e.getErrors().list().size(), is(1));
            assertThat(e.getErrors().list().get(0).getCode(), equalTo("UNAUTHORIZED"));
            assertThat(e.getErrors().list().get(0).getMessage(), equalTo("authentication required"));
            assertThat(new JSONArray("[{\"Type\":\"registry\",\"Name\":\"catalog\",\"Action\":\"*\"}]")
                    .similar(new JSONArray(e.getErrors().list().get(0).getDetail())), is(true));
        }
    }

    @Test
    public void getCatalogIterationTest () throws Exception {
        server.enqueue(newResponse("{}")); // Add base
        server.enqueue(newResponse("{\"repositories\":[\"foo\", \"bar\"]}")
                .setHeader(Headers.LINK, "</v2/_catalog?last=bar&n=2>; rel=\"next\""));
        server.enqueue(newResponse("{\"repositories\":[\"another-foo\", \"another-bar\"]}")
                .setHeader(Headers.LINK, "</v2/_catalog?last=another-bar&n=2>; rel=\"next\""));
        server.enqueue(newResponse("{\"repositories\":[\"another-another-foo\", \"another-another-bar\"]}"));

        RegistryCatalog catalog = registry.getBase().getCatalog();
        for (RegistryRepository repository : catalog) {
            logger.debug("Repository: {}", repository.getName());
        }
    }
}