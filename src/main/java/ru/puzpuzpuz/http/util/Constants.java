package ru.puzpuzpuz.http.util;

/**
 * A simple container for various constants.
 */
public interface Constants {

    String SUPPORTED_HTTP_METHOD = "GET";
    String SUPPORTED_HTTP_VERSION = "HTTP/1.1";

    long SHUTDOWN_TIMEOUT_MILLIS = 10000L;

    int SOCKET_READ_DATA_LIMIT_BYTES = 32768;
    int SOCKET_READ_BUFFER_SIZE_BYTES = 8192;
    int FILE_READ_BUFFER_SIZE_BYTES = 8192;

    String SETTINGS_FILE_DEFAULT = "settings.properties";

    String SETTINGS_PORT = "port";
    String SETTINGS_WWW_ROOT = "www_root";
    String SETTINGS_SESSION_TIMEOUT_SECS = "session_timeout_secs";
    String SETTINGS_MAX_CONNECTIONS = "max_connections";

    int SETTINGS_PORT_DEFAULT = 8080;
    String SETTINGS_WWW_ROOT_DEFAULT = "";
    int SETTINGS_SESSION_TIMEOUT_SECS_DEFAULT = 30;
    int SETTINGS_MAX_CONNECTIONS_DEFAULT = 10000;

    enum HttpStatus {

        SUCCESS(200, "OK"),
        BAD_REQUEST(400, "Bad Request"),
        REQUEST_TIMEOUT(408, "Request Timeout"),
        TOO_MANY_REQUESTS(429, "Too Many Requests"),
        NOT_FOUND(404, "Not Found");

        public final int code;
        public final String reason;

        HttpStatus(int code, String reason) {
            this.code = code;
            this.reason = reason;
        }

    }

}
