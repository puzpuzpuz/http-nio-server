package ru.puzpuzpuz.http.response;

import java.text.SimpleDateFormat;
import java.util.*;

import static ru.puzpuzpuz.http.util.Constants.SUPPORTED_HTTP_VERSION;

/**
 * A POJO that contains all data that will be sent in a HTTP response. Also contains fields that indicate
 * state of transitioning the response over the wire.
 */
public final class HttpResponse {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
        "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private int code;
    private String reason;
    private final Map<String, String> headers = new HashMap<>();

    private List<byte[]> pendingContent = new LinkedList<>();
    private int pendingContentLength;
    private long contentLength;

    private boolean complete;
    private boolean wroteHeaders;

    public void addDefaultHeaders() {
        Calendar calendar = Calendar.getInstance();
        this.headers.put("Date", dateFormat.format(calendar.getTime()));
        this.headers.put("Server", "Simple NIO HTTP Server v1.0.0");
        this.headers.put("Connection", "closeSilently");
        this.headers.put("Content-Length", Long.toString(contentLength));
    }

    public String generatePrefix() {
        return SUPPORTED_HTTP_VERSION + " " + code + " " + reason;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public boolean hasPendingContent() {
        return pendingContentLength > 0;
    }

    public byte[] flushPendingContent() {
        byte[] result = new byte[pendingContentLength];
        int pos = 0;
        for (byte[] chunk : pendingContent) {
            System.arraycopy(chunk, 0, result, pos, chunk.length);
            pos += chunk.length;
        }

        pendingContent = new LinkedList<>();
        pendingContentLength = 0;

        return result;
    }

    public void addContentChunk(byte[] chunk) {
        pendingContent.add(chunk);
        pendingContentLength += chunk.length;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public boolean isComplete() {
        return complete;
    }

    public void markAsComplete() {
        this.complete = true;
    }

    public boolean isWroteHeaders() {
        return wroteHeaders;
    }

    public void markAsWroteHeaders() {
        this.wroteHeaders = true;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
            "code=" + code +
            ", reason='" + reason + '\'' +
            '}';
    }
}
