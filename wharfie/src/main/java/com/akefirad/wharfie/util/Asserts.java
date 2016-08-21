package com.akefirad.wharfie.util;

import com.akefirad.wharfie.exception.IncompatibleApiException;
import okhttp3.Request;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.akefirad.wharfie.ApiConstants.Headers.DOCKER_DISTRIBUTION_API_VERSION;
import static com.akefirad.wharfie.ApiConstants.Headers.REGISTRY_2_0;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

public interface Asserts {
    static void notNull (Object value, String name) {
        if (value == null)
            throw new IllegalArgumentException(name + " is null!");
    }

    static void assertMatch (Pattern pattern, String value, String name) {
        if (!pattern.matcher(value).matches())
            throw new IllegalArgumentException(name + " does not match! value: "
                    + value + " pattern: " + pattern.pattern());
    }

    static void notEmpty (String value, String name) {
        if (value == null || value.isEmpty())
            throw new IllegalArgumentException(name + " is null or empty!");
    }

    static void notEmpty (String[] value, String name) {
        if (value == null || value.length == 0)
            throw new IllegalArgumentException(name + " is empty!");
    }

    static void notBlank (String value, String name) {
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException(name + " is null or empty!");
    }

    static void assertThat (boolean value, String name) {
        if (!value)
            throw new IllegalArgumentException(name + " is false!");
    }

    static void validateApiVersion2 (Map<String, List<String>> headers,
                                     Request request) {
        if (!ofNullable(headers.get(DOCKER_DISTRIBUTION_API_VERSION))
                .orElse(emptyList()).contains(REGISTRY_2_0))
            throw new IncompatibleApiException(request.url().url().toString());
    }
}
