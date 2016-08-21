package com.akefirad.wharfie.payload;

import java.util.*;

import static java.util.Collections.*;
import static java.util.Optional.ofNullable;

/**
 * Payload of catalog request
 */
public class CatalogResponse extends EntityResponse {
    public static final CatalogResponse NO_REPOSITRY = new CatalogResponse(emptyMap(), emptyList());

    private int number;
    private String last;
    private List<String> repositories;

    public CatalogResponse (Map<String, List<String>> headers, List<String> repositories) {
        super(headers);
        init(0, null, repositories);
    }

    public CatalogResponse (Map<String, List<String>> headers, List<String> repositories, int number) {
        super(headers);
        init(number, null, repositories);
    }

    public CatalogResponse (Map<String, List<String>> headers, List<String> repositories, int number, String last) {
        super(headers);
        init(number, last, repositories);
    }

    public List<String> getRepositories () {
        return unmodifiableList(ofNullable(repositories).orElse(emptyList()));
    }

    private void init (int number, String last, List<String> list) {
        this.number = number;
        this.last = last;
        this.repositories = new ArrayList<>(list);
    }
}
