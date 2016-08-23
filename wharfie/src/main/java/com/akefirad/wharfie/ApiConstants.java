package com.akefirad.wharfie;

public interface ApiConstants {
    interface Statuses {
        int SUCCESS = 200;
        int CREATED = 201;
        int NOT_FOUND = 404;
        int UNAUTHORIZED = 401;
        int INTERNAL_ERROR = 500;
    }

    interface Headers {
        // Names
        String DOCKER_DISTRIBUTION_API_VERSION = "Docker-Distribution-API-Version".toLowerCase();
        String DOCKER_CONTENT_DIGEST = "Docker-Content-Digest".toLowerCase();
        String WWW_AUTHENTICATE = "Www-Authenticate".toLowerCase();
        String LINK = "Link".toLowerCase();
        String CONTENT_TYPE = "Content-Type".toLowerCase();

        // Values
        String REGISTRY_2_0 = "registry/2.0";
        String INSUFFICIENT_SCOPE = "insufficient_scope";
        String REL_NEXT = " rel=\"next\"";
        String CONTENT_TYPE_JSON_UTF8 = "application/json; charset=utf-8";
    }

    interface Urls {
        String QUERY_LAST = "last";
        String QUERY_NUMBER = "n";
    }

    interface Labels {
        String REPOSITORIES = "repositories";
        String NAME = "name";
        String ERRORS = "errors";
        String CODE = "code";
        String MESSAGE = "message";
        String DETAIL = "detail";
    }

    interface ErrorCodes {
        String UNAUTHORIZED = "UNAUTHORIZED";
        String INVALID_JSON_ERRORS = "INVALID_JSON_ERRORS";
    }
}
