package com.akefirad.wharfie.util;

import com.akefirad.wharfie.payload.EntityResponse;
import retrofit2.Response;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

public interface WharfieUtils {
    static Optional<URL> tryParseUrl (String string) {
        Asserts.notNull(string, "string");

        try {
            return Optional.of(new URL(string));
        }
        catch (MalformedURLException e) {
            return Optional.empty();
        }
    }

    static Optional<Integer> tryParseInt (String string) {
        Asserts.notNull(string, "string");

        try {
            return Optional.of(Integer.parseInt(string));
        }
        catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    static <T extends EntityResponse> Map<String, List<String>> getHeaders (
            Response<T> response) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        response.headers()
                .toMultimap()
                .entrySet()
                .forEach(entry ->
                        headers.put(entry.getKey().toLowerCase(), entry.getValue()));
        return headers;
    }

    static List<String> readHeader (Map<String, List<String>> headers, String name) {
        return ofNullable(headers.get(name)).orElse(emptyList());
    }
}
