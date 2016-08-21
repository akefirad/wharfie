package com.akefirad.wharfie.payload;

import com.akefirad.wharfie.util.Asserts;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.*;

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

/**
 * Base class for all response entity (payload)
 */
public class EntityResponse {
    private Map<String, List<String>> headers;

    public EntityResponse () {
        this(emptyMap());
    }

    public EntityResponse (Map<String, List<String>> headers) {
        Asserts.notNull(headers, "headers");

        this.headers = new LinkedHashMap<>();
        headers.forEach((key, values) -> this.headers.put(key, new ArrayList<>(values)));
    }

    public Map<String, List<String>> getHeaders () {
        Map<String, List<String>> map = new LinkedHashMap<>();
        ofNullable(headers).orElse(emptyMap())
                .forEach((key, values) -> map.put(key, new ArrayList<>(values)));
        return map;
    }

    public void setHeaders (Map<String, List<String>> headers) {
        this.headers = new LinkedHashMap<>();
        ofNullable(headers).orElse(emptyMap())
                .forEach((key, values) -> this.headers.put(key, new ArrayList<>(values)));
    }

    @Override
    public String toString () {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }
}
