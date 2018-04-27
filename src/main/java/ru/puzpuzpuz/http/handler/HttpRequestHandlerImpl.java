package ru.puzpuzpuz.http.handler;

import ru.puzpuzpuz.http.ServerSettings;
import ru.puzpuzpuz.http.fs.AsyncFileReader;
import ru.puzpuzpuz.http.request.HttpRequest;
import ru.puzpuzpuz.http.request.HttpRequestParser;
import ru.puzpuzpuz.http.request.HttpRequestValidator;
import ru.puzpuzpuz.http.request.RawRequestReader;
import ru.puzpuzpuz.http.response.HttpResponse;
import ru.puzpuzpuz.http.response.HttpResponseFactory;
import ru.puzpuzpuz.http.response.HttpResponseWriter;
import ru.puzpuzpuz.http.util.Logger;
import ru.puzpuzpuz.http.util.OptimisticLock;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

class HttpRequestHandlerImpl extends HttpRequestHandler {

    private static final Logger LOGGER = new Logger(HttpRequestHandlerImpl.class.getName());

    private HttpRequest request;
    private HttpResponse response;

    private final HttpResponseWriter responseWriter = new HttpResponseWriter();
    private final OptimisticLock writeLock = new OptimisticLock();
    private AsyncFileReader fileReader;
    private FileReadHandler fileReadHandler;

    private final ServerSettings settings;
    private final int sessionTimeoutMillis;
    private final long creationTimeMillis;
    private final long connectionNum;

    HttpRequestHandlerImpl(ServerSettings settings, int connectionNum) {
        this.settings = settings;
        this.sessionTimeoutMillis = settings.getSessionTimeoutSecs() * 1000;
        this.creationTimeMillis = System.currentTimeMillis();
        this.connectionNum = connectionNum;
    }

    @Override
    public void read(ReadableByteChannel channel) throws IOException {
        String raw = new RawRequestReader().readRaw(channel);
        request = new HttpRequestParser().parse(raw);
        LOGGER.info("Parsed incoming HTTP request: " + request);

        HttpRequestValidator validator = new HttpRequestValidator(sessionTimeoutMillis, creationTimeMillis,
            connectionNum, settings.getMaxConnections());
        response = validator.validate(request);
        if (response != null) {
            LOGGER.warn("Invalid incoming HTTP request: " + request + ", response: " + response);
        }
    }

    @Override
    public void write(WritableByteChannel channel) throws IOException {
        if (request == null) {
            throw new IllegalStateException("Request is not initialized");
        }

        initFileResponse();
        responseWriter.writeHeaders(channel, response);
        validateSessionTimeout();
        writePendingContent(channel);
        scheduleFileForRead();
    }

    private void initFileResponse() {
        // an error response may be already there
        if (response != null) {
            return;
        }

        HttpResponseFactory httpResponseFactory = new HttpResponseFactory();
        try {
            fileReader = AsyncFileReader.open(settings.getWwwRoot(), request.getPath());
            fileReadHandler = new FileReadHandler();
            response = httpResponseFactory.buildFileResponse(fileReader.getMetadata());
            LOGGER.info("Started reading file for request: " + request);
        } catch (IOException e) {
            LOGGER.warn("Could not read file for request: " + request);
            response = httpResponseFactory.buildNotFound("Could not read file");
        }
    }

    private void validateSessionTimeout() {
        long time = System.currentTimeMillis();
        if (time - creationTimeMillis > sessionTimeoutMillis) {
            writeLock.lock();

            try {
                LOGGER.warn("Session timeout exceeded for request: " + request);
                response.markAsComplete();
                // get rid of buffered pending data
                response.flushPendingContent();
            } finally {
                writeLock.unlock();
            }
        }
    }

    private void writePendingContent(WritableByteChannel channel) throws IOException {
        // write only if there is an opportunity, otherwise - write on next tick
        boolean responseLocked = writeLock.tryLock();
        if (responseLocked) {
            try {
                responseWriter.writeContent(channel, response);
            } finally {
                writeLock.unlock();
            }
        }
    }

    private void scheduleFileForRead() {
        if (fileReader == null) {
            return;
        }
        fileReader.readNextChunk(fileReadHandler);
    }

    @Override
    public boolean hasNothingToWrite() {
        boolean responseLocked = writeLock.tryLock();
        if (responseLocked) {
            try {
                return response.isComplete() && !response.hasPendingContent();
            } finally {
                writeLock.unlock();
            }
        }
        return false;
    }

    @Override
    public void releaseSilently() {
        if (fileReader != null) {
            fileReader.closeSilently();
        }
    }

    private class FileReadHandler implements AsyncFileReader.ReadHandler {
        @Override
        public void onRead(byte[] data) {
            writeLock.lock();
            try {
                if (!response.isComplete()) {
                    response.addContentChunk(data);
                }
            } finally {
                writeLock.unlock();
            }
        }

        @Override
        public void onComplete() {
            LOGGER.info("Finished reading file for request: " + request);
            writeLock.lock();
            try {
                response.markAsComplete();
            } finally {
                writeLock.unlock();
            }
        }

        @Override
        public void onError(Throwable e) {
            LOGGER.error("Error during reading file for request: " + request, e);
            writeLock.lock();
            try {
                response.markAsComplete();
            } finally {
                writeLock.unlock();
            }
        }
    }
}
