package ru.puzpuzpuz.http.response;

import ru.puzpuzpuz.http.fs.FileMetadata;
import ru.puzpuzpuz.http.util.Constants.HttpStatus;

/**
 * A simple factory for {@link HttpResponse}.
 */
public final class HttpResponseFactory {

    public HttpResponse buildBadRequest(String msg) {
        return buildImmediateResponse(HttpStatus.BAD_REQUEST, msg);
    }

    public HttpResponse buildRequestTimeout() {
        return buildImmediateResponse(HttpStatus.REQUEST_TIMEOUT,
            "Session timeout exceeded");
    }

    public HttpResponse buildTooManyRequests() {
        return buildImmediateResponse(HttpStatus.TOO_MANY_REQUESTS,
            "Connections number exceeded the limit");
    }

    public HttpResponse buildNotFound(String msg) {
        return buildImmediateResponse(HttpStatus.NOT_FOUND, msg);
    }

    private HttpResponse buildImmediateResponse(HttpStatus status, String msg) {
        HttpResponse response = new HttpResponse();
        response.setCode(status.code);
        response.setReason(status.reason);
        byte[] content = msg.getBytes();
        response.addContentChunk(content);
        response.setContentLength(content.length);
        response.markAsComplete();
        response.addDefaultHeaders();
        return response;
    }

    public HttpResponse buildFileResponse(FileMetadata metadata) {
        HttpResponse response = new HttpResponse();
        response.setCode(HttpStatus.SUCCESS.code);
        response.setReason(HttpStatus.SUCCESS.reason);
        response.setContentLength(metadata.getSize());
        response.addDefaultHeaders();
        return response;
    }

}
