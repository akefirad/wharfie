package com.akefirad.wharfie;

import com.akefirad.wharfie.ApiConstants.Headers;
import com.akefirad.wharfie.exceptions.*;
import com.akefirad.wharfie.payloads.CatalogResponse;
import com.akefirad.wharfie.payloads.ErrorsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;

import java.util.*;
import java.util.regex.Pattern;

import static com.akefirad.wharfie.ApiConstants.Headers.INSUFFICIENT_SCOPE;
import static com.akefirad.wharfie.ApiConstants.Headers.WWW_AUTHENTICATE;
import static com.akefirad.wharfie.ApiConstants.Urls.QUERY_LAST;
import static com.akefirad.wharfie.ApiConstants.Urls.QUERY_NUMBER;
import static com.akefirad.wharfie.utils.Asserts.notBlank;
import static com.akefirad.wharfie.utils.Asserts.assertThat;
import static com.akefirad.wharfie.utils.Asserts.notNull;
import static com.akefirad.wharfie.utils.WharfieUtils.readHeader;
import static com.akefirad.wharfie.utils.WharfieUtils.tryParseInt;
import static com.akefirad.wharfie.utils.WharfieUtils.tryParseUrl;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.*;

public class RegistryBase extends RegistryEntity {
    private static final Logger logger = LoggerFactory.getLogger(RegistryBase.class);

    public static final Pattern LINK_PATTERN =
            Pattern.compile("<[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]>; rel=\"next\"");

    private final DockerRegistry registry;
    private final String version;

    public RegistryBase ( DockerRegistry registry, String version ) {
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
    public RegistryCatalog getCatalog ( int count ) throws RegistryException {
        return this.getCatalog(count, null);
    }

    //-----------------------------------------------------------------------------------
    public RegistryCatalog getCatalog ( int count, RegistryRepository last ) throws RegistryException {
        DockerRegistry registry = getRegistry();
        try {
            String lastRepository = ofNullable(last).map(RegistryRepository::getName).orElse(EMPTY);
            Call<CatalogResponse> catalog = registry.getRegistryRestApi().getCatalog(count, lastRepository);
            CatalogResponse response = registry.getRequestCaller().execute(catalog);

            // Extracting "next" link information
            Map<String, List<String>> headers = response.getHeaders();
            String link = ofNullable(headers.get(Headers.LINK)).orElse(emptyList()).stream()
                    .filter(value -> LINK_PATTERN.matcher(value).matches())
                    .findAny().orElse(EMPTY);

            Map<String, String> queries = new LinkedHashMap<>();
            queries.put(QUERY_NUMBER, "0");
            queries.put(QUERY_LAST, null);
            if (!link.isEmpty()) {
                //TODO: Re-implement the following using regex!
                String string = substringBefore(substringAfter(link, "<"), ">");
                tryParseUrl(string.startsWith("/") ? "http://localhost/" + string : string)
                        .ifPresent(u -> stream(split(u.getQuery(), "&"))
                                .forEach(query -> {
                                    String[] parts = split(query, "=");
                                    assertThat(parts.length == 2, "parts.length == 2");
                                    queries.put(parts[0], parts[1]);
                                }));
            }
            return new RegistryCatalog(this, tryParseInt(queries.get(QUERY_NUMBER)).orElse(0),
                    queries.get(QUERY_LAST), response.getRepositories());
        }
        catch (UnauthorizedRequestException e) {
            // See http://goo.gl/J1qAjt
            throw e.getCode() == 401 && isInsufficientScopeError(e.getErrors()) ?
                    new InsufficientScopeException(registry.toString(), e.getErrors()) : e;
        }
    }

    //-----------------------------------------------------------------------------------
    @Override
    DockerRegistry getRegistry () {
        return registry;
    }

    private boolean isInsufficientScopeError ( ErrorsResponse errors ) {
        return readHeader(errors.getHeaders(), WWW_AUTHENTICATE).stream()
                .anyMatch(value -> value.contains(INSUFFICIENT_SCOPE));
    }
}

