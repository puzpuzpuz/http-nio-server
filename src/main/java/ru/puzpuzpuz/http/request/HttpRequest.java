package ru.puzpuzpuz.http.request;

/**
 * A POJO that contains HTTP request data that is necessary for the server.
 */
public final class HttpRequest {

    private final String method;
    private final String path;
    private final String version;

    public HttpRequest(String method, String path, String version) {
        this.method = method;
        this.path = path;
        this.version = version;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
            "method='" + method + '\'' +
            ", path='" + path + '\'' +
            ", version='" + version + '\'' +
            '}';
    }
}
