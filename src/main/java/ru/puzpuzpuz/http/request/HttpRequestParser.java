package ru.puzpuzpuz.http.request;

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * A simple parser for HTTP requests. During parsing it tries to tokenize incoming data and
 * parses it as an HTTP request.
 */
public final class HttpRequestParser {

    public HttpRequest parse(String raw) throws IOException {
        try {
            StringTokenizer tokenizer = new StringTokenizer(raw);
            String method = tokenizer.nextToken().toUpperCase();
            String path = tokenizer.nextToken();
            String version = tokenizer.nextToken();

            return new HttpRequest(method, path, version);
        } catch (Exception e) {
            throw new IOException("Malformed request", e);
        }
    }

}
