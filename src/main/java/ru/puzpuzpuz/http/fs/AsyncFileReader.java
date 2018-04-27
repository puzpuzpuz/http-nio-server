package ru.puzpuzpuz.http.fs;

import java.io.File;
import java.io.IOException;

/**
 * A simple wrapper on top of {@link java.nio.channels.AsynchronousFileChannel} (with default thread pool
 * configuration). Reads the file sequentially in async fashion. Reading the next chunk of file data can be
 * scheduled by calling {@link #readNextChunk}.
 * <p>
 * Note that only a single read can be pending simultaneously.
 */
public abstract class AsyncFileReader {

    public static AsyncFileReader open(String root, String path) throws IOException {
        String filePath = (root + path).replace("/", File.separator);
        return new AsyncFileReaderImpl(filePath);
    }

    public abstract void readNextChunk(ReadHandler handler);

    public abstract FileMetadata getMetadata();

    public abstract void closeSilently();

    public interface ReadHandler {

        void onRead(byte[] data);

        void onComplete();

        void onError(Throwable e);

    }
}
