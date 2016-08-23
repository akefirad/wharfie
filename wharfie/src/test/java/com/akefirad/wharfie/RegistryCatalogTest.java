package com.akefirad.wharfie;

import com.akefirad.wharfie.ApiConstants.ErrorCodes;
import com.akefirad.wharfie.exception.InsufficientScopeException;
import com.akefirad.wharfie.payload.ErrorsResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.json.JSONArray;
import org.junit.*;
import org.slf4j.Logger;

import java.util.*;

import static com.akefirad.wharfie.ApiConstants.Headers.LINK;
import static com.akefirad.wharfie.ApiConstants.Headers.WWW_AUTHENTICATE;
import static com.akefirad.wharfie.ApiConstants.Statuses.UNAUTHORIZED;
import static com.akefirad.wharfie.TestUtils.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.collections4.IteratorUtils.toList;
import static org.apache.commons.lang3.StringUtils.split;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.slf4j.LoggerFactory.getLogger;

public class RegistryCatalogTest {
    private static final Logger logger = getLogger(RegistryCatalogTest.class);

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
        Set<String> names = new HashSet<>(asList(split("foo bar")));
        Repositories repositories = newRepositories(names);

        server.enqueue(newResponse(BASE));
        server.enqueue(newResponse(toJson(repositories)));

        RegistryCatalog catalog = registry.getBase().getCatalog();
        Set<String> actual = catalog.getRepositories().stream()
                .map(RegistryRepository::getName)
                .collect(toSet());

        assertEquals(names, actual);
    }

    @Test
    public void getCatalog200EmptyBodyTest () throws Exception {
        server.enqueue(newResponse(BASE));
        server.enqueue(newResponse(EMPTY_JSON));

        RegistryCatalog catalog = registry.getBase().getCatalog();
        assertEquals(catalog.getRepositories(), emptyList());
    }

    @Test
    public void getCatalog200EmptyListTest () throws Exception {
        Set<String> names = new HashSet<>();
        Repositories repositories = newRepositories(names);

        server.enqueue(newResponse(BASE));
        server.enqueue(newResponse(toJson(repositories)));

        RegistryCatalog catalog = registry.getBase().getCatalog();
        assertEquals(catalog.getRepositories(), emptyList());
    }

    @Test
    public void getCatalog200WithLinkRelNextTest () throws Exception {
        Set<String> names = new HashSet<>(asList(split("foo")));

        server.enqueue(newResponse(BASE));
        server.enqueue(newResponse(toJson(newRepositories(names)))
                .setHeader(LINK, "</v2/_catalog?last=foo&n=1>; rel=\"next\""));

        RegistryCatalog catalog = registry.getBase().getCatalog(1);
        List<String> repositories = catalog.getRepositories().stream()
                .map(RegistryRepository::getName)
                .collect(toList());
        assertEquals(repositories, Collections.singletonList("foo"));
    }

    @Test
    public void getCatalog401InsufficientTest () throws Exception {
        server.enqueue(newResponse(BASE));
        server.enqueue(newResponse(UNAUTHORIZED, "{\"errors\":[{\"code\":" +
                "\"UNAUTHORIZED\",\"message\":\"authentication required\"," +
                "\"detail\":[{\"Type\":\"registry\",\"Name\":\"catalog\"," +
                "\"Action\":\"*\"}]}]}")
                .addHeader(WWW_AUTHENTICATE, "Www-Authenticate: Bearer realm=" +
                        "\"https://auth.docker.io/token\"," +
                        "service=\"registry.docker.io\",scope=\"registry:catalog:*" +
                        "\",error=\"insufficient_scope\""));

        try {
            RegistryCatalog catalog = registry.getBase().getCatalog();
            fail("Expecting InsufficientScopeException!");
        }
        catch (InsufficientScopeException e) {
            List<ErrorsResponse.Error> errors = e.getErrors().list();
            assertThat(errors.size(), is(1));
            assertThat(errors.get(0).getCode(), equalTo(ErrorCodes.UNAUTHORIZED));
            assertThat(errors.get(0).getMessage(), equalTo("authentication required"));

            JSONArray actual = new JSONArray(errors.get(0).getDetail());
            JSONArray expected =
                    new JSONArray("[{\"Type\":\"registry\",\"Name\":" +
                            "\"catalog\",\"Action\":\"*\"}]");
            assertThat(expected.similar(actual), is(true));
        }
    }

    @Test
    public void getCatalogIterationTest () throws Exception {
        List<Set<String>> names = asList(new HashSet<>(asList(split("foo bar"))),
                new HashSet<>(asList(split("new-foo new-bar"))),
                new HashSet<>(asList(split("another-foo another-bar"))));
        server.enqueue(newResponse(BASE));
        server.enqueue(newResponse(toJson(newRepositories(names.get(0))))
                .setHeader(LINK, "</v2/_catalog?last=bar&n=2>; rel=\"next\""));
        server.enqueue(newResponse(toJson(newRepositories(names.get(1))))
                .setHeader(LINK, "</v2/_catalog?last=another-bar&n=2>; rel=\"next\""));
        server.enqueue(newResponse(toJson(newRepositories(names.get(2)))));

        RegistryCatalog catalog = registry.getBase().getCatalog();
        Set<String> actual = toList(catalog.iterator()).stream()
                .map(RegistryRepository::getName)
                .collect(toSet());
        Set<String> expected = new HashSet<>();
        names.forEach(expected::addAll);

        assertEquals(actual, expected);
    }

}