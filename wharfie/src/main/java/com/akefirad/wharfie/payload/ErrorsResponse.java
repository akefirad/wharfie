package com.akefirad.wharfie.payload;

import java.util.List;
import java.util.Map;

import static com.akefirad.wharfie.util.Asserts.notBlank;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

/**
 * Errors response payload
 */
public class ErrorsResponse extends EntityResponse {
    public static final ErrorsResponse NO_ERROR = new ErrorsResponse(emptyMap(), emptyList());

    private List<Error> list;

    public ErrorsResponse (Map<String, List<String>> headers, List<Error> list) {
        super(headers);
        this.list = list.stream().map(Error::copy).collect(toList());
    }

    public List<Error> list () {
        return list.stream().map(Error::copy).collect(toList());
    }

    public ErrorsResponse copy () {
        return new ErrorsResponse(getHeaders(), list());
    }

    public static class Error {
        private String code = "";
        private String message = "";
        private String detail = "";

        public Error (String code, String message, String detail) {
            notBlank(code, "code");
            notBlank(message, "message");

            this.code = code;
            this.message = message;
            this.detail = detail;
        }

        public String getCode () {
            return code;
        }

        public String getMessage () {
            return message;
        }

        public String getDetail () {
            return detail;
        }

        public Error copy () {
            return new Error(code, message, detail);
        }
    }
}
