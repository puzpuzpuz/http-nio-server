package ru.puzpuzpuz.http.request;

import ru.puzpuzpuz.http.response.HttpResponse;
import ru.puzpuzpuz.http.response.HttpResponseFactory;

import static ru.puzpuzpuz.http.util.Constants.SUPPORTED_HTTP_METHOD;
import static ru.puzpuzpuz.http.util.Constants.SUPPORTED_HTTP_VERSION;

/**
 * A simple validator for {@link HttpRequest}. Validates the request against supported HTTP version and methods,
 * also checks session timeout and connections limit.
 */
public final class HttpRequestValidator {

    private final int sessionTimeoutMillis;
    private final long creationTimeMillis;
    private final long connectionNum;
    private final long maxConnectionsNum;

    private final HttpResponseFactory httpResponseFactory = new HttpResponseFactory();

    public HttpRequestValidator(int sessionTimeoutMillis, long creationTimeMillis,
                                long connectionNum, long maxConnectionsNum) {
        this.sessionTimeoutMillis = sessionTimeoutMillis;
        this.creationTimeMillis = creationTimeMillis;
        this.connectionNum = connectionNum;
        this.maxConnectionsNum = maxConnectionsNum;
    }

    public HttpResponse validate(HttpRequest request) {
        // validateSupported request (method, etc.)
        String invalidReason = validateSupported(request);
        if (invalidReason != null) {
            return httpResponseFactory.buildBadRequest(invalidReason);
        }
        // check session timeout before starting write
        long time = System.currentTimeMillis();
        if (time - creationTimeMillis > sessionTimeoutMillis) {
            return httpResponseFactory.buildRequestTimeout();
        }
        // check if connection number exceeds the limit
        if (connectionNum > maxConnectionsNum) {
            return httpResponseFactory.buildTooManyRequests();
        }
        return null;
    }

    private String validateSupported(HttpRequest request) {
        String method = request.getMethod();
        if (method == null || !method.equals(SUPPORTED_HTTP_METHOD)) {
            return "Unsupported method";
        }
        String version = request.getVersion();
        if (version == null || !version.equals(SUPPORTED_HTTP_VERSION)) {
            return "Unsupported HTTP version";
        }
        return null;
    }

}
