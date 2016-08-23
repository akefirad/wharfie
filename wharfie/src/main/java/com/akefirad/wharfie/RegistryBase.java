package com.akefirad.wharfie;

import com.akefirad.wharfie.ApiConstants.Headers;
import com.akefirad.wharfie.call.RequestHandler;
import com.akefirad.wharfie.exception.*;
import com.akefirad.wharfie.payload.CatalogResponse;
import com.akefirad.wharfie.payload.ErrorsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;

import java.util.*;
import java.util.regex.Pattern;

import static com.akefirad.wharfie.ApiConstants.Headers.INSUFFICIENT_SCOPE;
import static com.akefirad.wharfie.ApiConstants.Headers.WWW_AUTHENTICATE;
import static com.akefirad.wharfie.ApiConstants.Statuses.UNAUTHORIZED;
import static com.akefirad.wharfie.ApiConstants.Urls.QUERY_LAST;
import static com.akefirad.wharfie.ApiConstants.Urls.QUERY_NUMBER;
import static com.akefirad.wharfie.util.Asserts.*;
import static com.akefirad.wharfie.util.WharfieUtils.*;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.*;

public class RegistryBase extends RegistryEntity {
    private static final Logger logger = LoggerFactory.getLogger(RegistryBase.class);

    public static final Pattern LINK_PATTERN =
            compile("<[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]>; rel=\"next\"");

    private final DockerRegistry registry;
    private final String version;

    public RegistryBase (DockerRegistry registry, String version) {
        super(null);

        notNull(registry, "registry");
        notBlank(version, "version");
        this.version = version;
        this.registry = registry;
    }

    //-----------------------------------------------------------------------------------
    public String getVersion () {
        return version;
    }

    //-----------------------------------------------------------------------------------
    public RegistryCatalog getCatalog () throws RegistryException {
        return this.getCatalog(Integer.MAX_VALUE, null);
    }

    //-----------------------------------------------------------------------------------
    public RegistryCatalog getCatalog (int count) throws RegistryException {
        return this.getCatalog(count, null);
    }

    //-----------------------------------------------------------------------------------
    public RegistryCatalog getCatalog (int count, RegistryRepository last)
            throws RegistryException {
        try {
            DockerRegistry registry = getRegistry();
            String name = last == null ? EMPTY : last.getName();
            CatalogResponse response = registry.getRequestHandler()
                    .execute(registry.getRegistryRestApi().getCatalog(count, name));

            // Extracting "next" link information
            Map<String, List<String>> headers = response.getHeaders();
            String link = ofNullable(headers.get(Headers.LINK))
                    .orElse(emptyList()).stream()
                    .filter(value -> LINK_PATTERN.matcher(value).matches())
                    .findAny()
                    .orElse(EMPTY);

            Map<String, String> queries = new LinkedHashMap<>();
            queries.put(QUERY_NUMBER, "0");
            queries.put(QUERY_LAST, null);
            if (!link.isEmpty()) {
                //TODO: Re-implement the following using regex!
                String str = substringBefore(substringAfter(link, "<"), ">");
                tryParseUrl(str.startsWith("/") ? "http://localhost/" + str : str)
                        .ifPresent(u -> stream(split(u.getQuery(), "&"))
                                .forEach(query -> {
                                    String[] parts = split(query, "=");
                                    assertThat(parts.length == 2, "parts.length == 2");
                                    queries.put(parts[0], parts[1]);
                                }));
            }
            return new RegistryCatalog(this,
                    tryParseInt(queries.get(QUERY_NUMBER)).orElse(0),
                    queries.get(QUERY_LAST), response.getRepositories());
        }
        catch (UnauthorizedRequestException e) {
            // See http://goo.gl/J1qAjt
            throw e.getCode() == UNAUTHORIZED && isInsufficientScopeError(e.getErrors()) ?
                    new InsufficientScopeException(registry.toString(), e.getErrors()) : e;
        }
    }

    //-----------------------------------------------------------------------------------
    @Override
    DockerRegistry getRegistry () {
        return registry;
    }

    private boolean isInsufficientScopeError (ErrorsResponse errors) {
        return readHeader(errors.getHeaders(), WWW_AUTHENTICATE).stream()
                .anyMatch(value -> value.contains(INSUFFICIENT_SCOPE));
    }
}

