package ru.puzpuzpuz.http.handler;

import ru.puzpuzpuz.http.ServerSettings;
import ru.puzpuzpuz.http.fs.AsyncFileReader;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * The main class responsible for HTTP communication. Handles the incoming data, parses it as a HTTP request
 * and sends the response.
 * <p>
 * When serving files it first flushes headers, then registers file read callbacks with an internal instance of
 * {@link AsyncFileReader}. As those callbacks are run on separate threads, necessary concurrency control takes place.
 */
public abstract class HttpRequestHandler {

    public abstract void read(ReadableByteChannel channel) throws IOException;

    public abstract void write(WritableByteChannel channel) throws IOException;

    public abstract boolean hasNothingToWrite();

    public abstract void releaseSilently();

    public static HttpRequestHandler build(ServerSettings settings, int connectionNum) {
        return new HttpRequestHandlerImpl(settings, connectionNum);
    }

}
